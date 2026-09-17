package com.govscheme.auth.dto;

import com.govscheme.auth.entity.User;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUser(UserInfo.from(user));
        return response;
    }

    public static class UserInfo {
        private String id;
        private String email;
        private String phone;
        private String fullName;
        private String role;
        private boolean emailVerified;
        private boolean phoneVerified;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
        }

        public boolean isPhoneVerified() {
            return phoneVerified;
        }

        public void setPhoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
        }

        public static UserInfo from(User user) {
            UserInfo info = new UserInfo();
            info.setId(user.getId().toString());
            info.setEmail(user.getEmail());
            info.setPhone(user.getPhone());
            info.setFullName(user.getFullName());
            info.setRole(user.getRole().name());
            info.setEmailVerified(user.getEmailVerified());
            info.setPhoneVerified(user.getPhoneVerified());
            return info;
        }
    }
}