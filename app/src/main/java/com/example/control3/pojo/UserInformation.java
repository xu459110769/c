package com.example.control3.pojo;

import java.io.Serializable;

public class UserInformation implements Serializable {
    private String uAccount;
    private String uName;
    private String token;
    private Integer id;
    private String uWords;
    private int code;
    //状态码 0表示未登录 1表示成功登录

    public String getuAccount() {
        return uAccount;
    }

    public void setuAccount(String uAccount) {
        this.uAccount = uAccount;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getuWords() {
        return uWords;
    }

    public void setuWords(String uWords) {
        this.uWords = uWords;
    }

    @Override
    public String toString() {
        return "UserInformation{" +
                "uAccount='" + uAccount + '\'' +
                ", uName='" + uName + '\'' +
                ", token='" + token + '\'' +
                ", id=" + id +
                ", uWords='" + uWords + '\'' +
                ", code=" + code +
                '}';
    }
}
