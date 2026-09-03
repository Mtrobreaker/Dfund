package com.dfund.app.ui.voice;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.dfund.app.DFundApplication;
import com.dfund.app.R;
import com.dfund.app.data.remote.VoiceAction;
import com.dfund.app.data.remote.VoiceResponse;
import com.dfund.app.ui.navigation.AiActionNavigator;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceActiveDialogFragment extends DialogFragment {
    private static final String TAG = "DFund.VoiceDialog";
    private VoiceInteractionManager voiceManager;
    private View viewMicPulse;
    private TextView tvStatus;
    private EditText etPrompt;
    private View layoutAiResponse;
    private TextView tvTamilTranslation;
    private TextView tvAiSuggestion;
    private View btnExecuteAction;
    private ObjectAnimator pulseAnimator;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    public static VoiceActiveDialogFragment newInstance() {
        return new VoiceActiveDialogFragment();
    }

    public void setVoiceInteractionManager(VoiceInteractionManager manager) {
        this.voiceManager = manager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_DFund);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_voice_active, container, false);

        viewMicPulse = view.findViewById(R.id.view_mic_pulse);
        tvStatus = view.findViewById(R.id.tv_voice_status);
        etPrompt = view.findViewById(R.id.et_voice_prompt);
        layoutAiResponse = view.findViewById(R.id.layout_ai_response);
        tvTamilTranslation = view.findViewById(R.id.tv_tamil_translation);
        tvAiSuggestion = view.findViewById(R.id.tv_ai_suggestion);
        btnExecuteAction = view.findViewById(R.id.btn_execute_action);

        startPulseAnimation();

        view.findViewById(R.id.btn_voice_close).setOnClickListener(v -> dismiss());

        // Tapping the big mic icon restarts listening
        viewMicPulse.setOnClickListener(v -> startListeningSpeech());
        view.findViewById(R.id.iv_voice_mic_icon).setOnClickListener(v -> startListeningSpeech());

        view.findViewById(R.id.btn_voice_submit).setOnClickListener(v -> {
            String text = etPrompt.getText().toString().trim();
            if (!text.isEmpty()) {
                submitPrompt(text);
            } else {
                Toast.makeText(getContext(), "Please speak or enter a prompt.", Toast.LENGTH_SHORT).show();
            }
        });

        // Quick Preset Prompt Chips
        view.findViewById(R.id.chip_prompt_sip).setOnClickListener(v -> submitPrompt("Calculate SIP for 500 rupees for 3 years"));
        view.findViewById(R.id.chip_prompt_tamil).setOnClickListener(v -> submitPrompt("மாதம் 500 ரூபாய்க்கு SIP கணக்கீடு செய்"));
        view.findViewById(R.id.chip_prompt_emi).setOnClickListener(v -> submitPrompt("Check my upcoming EMIs"));
        view.findViewById(R.id.chip_prompt_food).setOnClickListener(v -> submitPrompt("Show my food expenses"));

        initSpeechRecognizer();
        startListeningSpeech();

        return view;
    }

    private void initSpeechRecognizer() {
        if (getContext() == null) return;
        if (SpeechRecognizer.isRecognitionAvailable(getContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    if (isAdded()) {
                        tvStatus.setText("🎤 Listening... Speak now!");
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                    if (isAdded()) {
                        tvStatus.setText("Hearing you...");
                    }
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    if (viewMicPulse != null && rmsdB > 0) {
                        float scale = 1.0f + Math.min(rmsdB / 15f, 0.4f);
                        viewMicPulse.setScaleX(scale);
                        viewMicPulse.setScaleY(scale);
                    }
                }

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                    if (isAdded()) {
                        tvStatus.setText(R.string.voice_processing);
                    }
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    Log.w(TAG, "Speech recognition error code: " + error);
                    if (isAdded()) {
                        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            tvStatus.setText("Didn't catch that. Tap mic to speak, or tap a preset chip.");
                        } else {
                            tvStatus.setText("Tap mic to speak, or type your prompt below.");
                        }
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spoken = matches.get(0);
                        if (isAdded()) {
                            etPrompt.setText(spoken);
                            submitPrompt(spoken);
                        }
                    } else {
                        if (isAdded()) {
                            tvStatus.setText("Didn't catch that. Tap mic to speak again.");
                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (partial != null && !partial.isEmpty() && isAdded()) {
                        tvStatus.setText(partial.get(0) + "...");
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        } else {
            Log.w(TAG, "Speech recognition not available on device");
            tvStatus.setText("Tap a prompt chip or type below");
        }
    }

    private void startListeningSpeech() {
        if (speechRecognizer == null || isListening) return;

        String lang = "en-IN";
        if (DFundApplication.getInstance() != null) {
            String appLang = DFundApplication.getInstance().getSecurityManager().getLanguage();
            switch (appLang != null ? appLang : "en") {
                case "ta":
                    lang = "ta-IN";
                    break;
                case "te":
                    lang = "te-IN";
                    break;
                case "ml":
                    lang = "ml-IN";
                    break;
                default:
                    lang = "en-IN";
                    break;
            }
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext() != null ? getContext().getPackageName() : "");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        try {
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start speech listening", e);
        }
    }

    private void startPulseAnimation() {
        pulseAnimator = ObjectAnimator.ofFloat(viewMicPulse, "scaleX", 1f, 1.35f, 1f);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setDuration(1200);
        pulseAnimator.start();

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(viewMicPulse, "scaleY", 1f, 1.35f, 1f);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setDuration(1200);
        pulseY.start();
    }

    private void submitPrompt(String prompt) {
        tvStatus.setText(R.string.voice_processing);
        if (voiceManager != null) {
            voiceManager.submitTextVoiceCommand(prompt, new VoiceInteractionManager.VoiceCallback() {
                @Override
                public void onProcessing() {
                    tvStatus.setText(R.string.voice_processing);
                }

                @Override
                public void onSuccess(VoiceResponse response) {
                    if (isAdded() && getActivity() != null) {
                        tvStatus.setText("⚡ Automatically Executing...");

                        if (response.getTamilTranslation() != null && !response.getTamilTranslation().isEmpty()) {
                            tvTamilTranslation.setText(response.getTamilTranslation());
                        } else {
                            tvTamilTranslation.setText(response.getTranscription());
                        }

                        String suggestion = response.getFinancialSuggestion();
                        if (suggestion == null || suggestion.isEmpty()) {
                            suggestion = response.getAction() != null ? response.getAction().getSpokenResponse() : "Action executed.";
                        }
                        tvAiSuggestion.setText(suggestion);

                        if (layoutAiResponse != null) {
                            layoutAiResponse.setVisibility(View.VISIBLE);
                        }

                        // Automatically execute action without asking permission
                        if (response.getAction() != null && getActivity() != null) {
                            if (btnExecuteAction != null) {
                                btnExecuteAction.setVisibility(View.GONE);
                            }

                            // Give user 1.2 seconds to see what was done & result, then transition automatically
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                if (isAdded() && getActivity() != null) {
                                    AiActionNavigator.executeAction(getActivity(), response.getAction(), newLang -> {
                                        if (getActivity() != null) {
                                            getActivity().recreate();
                                        }
                                    });
                                    dismissAllowingStateLoss();
                                }
                            }, 1200);
                        } else {
                            if (btnExecuteAction != null) {
                                btnExecuteAction.setOnClickListener(v -> dismiss());
                            }
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (isAdded()) {
                        tvStatus.setText("Error: " + errorMessage);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}
