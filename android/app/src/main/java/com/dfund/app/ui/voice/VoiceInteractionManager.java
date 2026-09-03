package com.dfund.app.ui.voice;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import com.dfund.app.DFundApplication;
import com.dfund.app.data.remote.ApiClient;
import com.dfund.app.data.remote.VoiceAction;
import com.dfund.app.data.remote.VoiceResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceInteractionManager {
    private static final String TAG = "DFund.VoiceMgr";
    private final Context context;
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private boolean isTtsReady = false;

    public interface VoiceCallback {
        void onProcessing();
        void onSuccess(VoiceResponse response);
        void onError(String errorMessage);
    }

    public VoiceInteractionManager(Context context) {
        this.context = context;
        initTts();
    }

    private void initTts() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true;
                updateTtsLocale(DFundApplication.getInstance().getSecurityManager().getLanguage());
            }
        });
    }

    public void updateTtsLocale(String langCode) {
        if (!isTtsReady || tts == null) return;

        Locale targetLocale;
        switch (langCode != null ? langCode : "en") {
            case "ta":
                targetLocale = new Locale("ta", "IN");
                break;
            case "te":
                targetLocale = new Locale("te", "IN");
                break;
            case "ml":
                targetLocale = new Locale("ml", "IN");
                break;
            default:
                targetLocale = Locale.ENGLISH;
                break;
        }

        int result = tts.setLanguage(targetLocale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.ENGLISH); // Graceful fallback
        }
    }

    public void speak(String text) {
        if (isTtsReady && tts != null && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dfund_voice_tts");
        }
    }

    public void playAudioBase64(String base64Audio, String fallbackText) {
        try {
            byte[] decoded = Base64.decode(base64Audio, Base64.DEFAULT);
            File tempFile = File.createTempFile("sarvam_tts_", ".wav", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decoded);
            fos.close();

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> tempFile.delete());
        } catch (Exception e) {
            Log.w(TAG, "Failed playing neural TTS audio, using Android TTS fallback: " + e.getMessage());
            if (fallbackText != null) {
                speak(fallbackText);
            }
        }
    }

    public void submitTextVoiceCommand(String prompt, VoiceCallback callback) {
        if (callback != null) callback.onProcessing();

        String deviceId = DFundApplication.getInstance().getSecurityManager().getDeviceId();
        String lang = DFundApplication.getInstance().getSecurityManager().getLanguage();

        RequestBody promptBody = RequestBody.create(MediaType.parse("text/plain"), prompt);
        RequestBody deviceIdBody = RequestBody.create(MediaType.parse("text/plain"), deviceId);
        RequestBody langBody = RequestBody.create(MediaType.parse("text/plain"), lang);

        ApiClient.getApiService().processVoiceInteraction(null, promptBody, deviceIdBody, langBody)
            .enqueue(new Callback<VoiceResponse>() {
                @Override
                public void onResponse(Call<VoiceResponse> call, Response<VoiceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        VoiceResponse vr = response.body();
                        String spoken = (vr.getAction() != null) ? vr.getAction().getSpokenResponse() : vr.getFinancialSuggestion();

                        if (vr.getAudioBase64() != null && !vr.getAudioBase64().trim().isEmpty()) {
                            playAudioBase64(vr.getAudioBase64(), spoken);
                        } else if (spoken != null) {
                            speak(spoken);
                        }

                        if (callback != null) {
                            callback.onSuccess(vr);
                        }
                    } else {
                        // Fallback to on-device AI intent parsing
                        fallbackToLocalAi(prompt, lang, callback);
                    }
                }

                @Override
                public void onFailure(Call<VoiceResponse> call, Throwable t) {
                    Log.w(TAG, "Network voice interaction failure, falling back to on-device AI: " + t.getMessage());
                    fallbackToLocalAi(prompt, lang, callback);
                }
            });
    }

    private void fallbackToLocalAi(String prompt, String lang, VoiceCallback callback) {
        try {
            VoiceAction action = com.dfund.app.domain.LocalAiIntentParser.parse(prompt, lang);
            if (action != null && action.getSpokenResponse() != null) {
                speak(action.getSpokenResponse());
            }

            VoiceResponse localVr = new VoiceResponse();
            localVr.setTranscription(prompt);
            localVr.setDetectedLanguage(lang);
            localVr.setAction(action);
            localVr.setTamilTranslation(prompt);
            localVr.setFinancialSuggestion(action != null ? action.getSpokenResponse() : "");

            if (callback != null) {
                callback.onSuccess(localVr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in local AI fallback", e);
            if (callback != null) {
                callback.onError("Processing error: " + e.getMessage());
            }
        }
    }

    public void destroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
