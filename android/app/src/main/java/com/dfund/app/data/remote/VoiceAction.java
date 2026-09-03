package com.dfund.app.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class VoiceAction {
    @SerializedName("action_type")
    private String actionType;

    @SerializedName("parameters")
    private Map<String, Object> parameters;

    @SerializedName("spoken_response")
    private String spokenResponse;

    @SerializedName("action_target_screen")
    private String actionTargetScreen;

    public VoiceAction() {}

    public VoiceAction(String actionType, Map<String, Object> parameters, String spokenResponse) {
        this.actionType = actionType;
        this.parameters = parameters;
        this.spokenResponse = spokenResponse;
    }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getSpokenResponse() { return spokenResponse; }
    public void setSpokenResponse(String spokenResponse) { this.spokenResponse = spokenResponse; }

    public String getActionTargetScreen() { return actionTargetScreen; }
    public void setActionTargetScreen(String actionTargetScreen) { this.actionTargetScreen = actionTargetScreen; }
}
