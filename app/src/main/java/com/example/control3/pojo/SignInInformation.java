package com.example.control3.pojo;

public class SignInInformation {
    private String userId;
    private String token;
    private int code;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "SignInInformation{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                ", code=" + code +
                '}';
    }
}
