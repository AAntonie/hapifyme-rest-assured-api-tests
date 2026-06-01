package com.hapifyme.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse {

    private String status;
    private String message;
    private User user;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String id;
        private String first_name;
        private String last_name;
        private String username;
        private String email;
        private String signup_date;
        private String profile_pic;

        public String getId() {
            return id;
        }

        public String getFirst_name() {
            return first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getSignup_date() {
            return signup_date;
        }

        public String getProfile_pic() {
            return profile_pic;
        }
    }
}