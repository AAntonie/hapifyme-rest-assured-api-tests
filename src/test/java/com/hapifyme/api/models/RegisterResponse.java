package com.hapifyme.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponse {

    private String status;
    private String message;

    private String username;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("confirmation_token")
    private String confirmationToken;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }
}