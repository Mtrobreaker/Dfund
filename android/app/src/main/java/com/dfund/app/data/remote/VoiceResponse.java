package com.dfund.app.data.remote;

import com.google.gson.annotations.SerializedName;

public class VoiceResponse {
    @SerializedName("transcription")
    private String transcription;

    @SerializedName("detected_language")
    private String detectedLanguage;

    @SerializedName("tamil_translation")
    private String tamilTranslation;

    @SerializedName("financial_suggestion")
    private String financialSuggestion;

    @SerializedName("action")
    private VoiceAction action;

    @SerializedName("audio_base64")
    private String audioBase64;

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public String getDetectedLanguage() { return detectedLanguage; }
    public void setDetectedLanguage(String detectedLanguage) { this.detectedLanguage = detectedLanguage; }

    public String getTamilTranslation() { return tamilTranslation; }
    public void setTamilTranslation(String tamilTranslation) { this.tamilTranslation = tamilTranslation; }

    public String getFinancialSuggestion() { return financialSuggestion; }
    public void setFinancialSuggestion(String financialSuggestion) { this.financialSuggestion = financialSuggestion; }

    public VoiceAction getAction() { return action; }
    public void setAction(VoiceAction action) { this.action = action; }

    public String getAudioBase64() { return audioBase64; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }
}
