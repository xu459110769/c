package com.example.control3.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class User {
    private Integer uId;

    private String uAccount;

    private String uPassword;

    private String uName;

    private String uDel;

    private String uWords;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date uDate;

    private String uSex;

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public String getuAccount() {
        return uAccount;
    }

    public void setuAccount(String uAccount) {
        this.uAccount = uAccount == null ? null : uAccount.trim();
    }

    public String getuPassword() {
        return uPassword;
    }

    public void setuPassword(String uPassword) {
        this.uPassword = uPassword == null ? null : uPassword.trim();
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName == null ? null : uName.trim();
    }

    public String getuDel() {
        return uDel;
    }

    public void setuDel(String uDel) {
        this.uDel = uDel == null ? null : uDel.trim();
    }

    public String getuWords() {
        return uWords;
    }

    public void setuWords(String uWords) {
        this.uWords = uWords == null ? null : uWords.trim();
    }

    public Date getuDate() {
        return uDate;
    }

    public void setuDate(Date uDate) {
        this.uDate = uDate;
    }

    public String getuSex() {
        return uSex;
    }

    public void setuSex(String uSex) {
        this.uSex = uSex == null ? null : uSex.trim();
    }

    public void setNull() {
        if (uAccount.equals(""))uAccount = null;
        if (uPassword.equals(""))uPassword = null;
        if (uName.equals(""))uName = null;
        if (uWords.equals(""))uWords = null;
    }


    @Override
    public String toString() {
        return "User{" +
                "uId=" + uId +
                ", uAccount='" + uAccount + '\'' +
                ", uPassword='" + uPassword + '\'' +
                ", uName='" + uName + '\'' +
                ", uDel='" + uDel + '\'' +
                ", uWords='" + uWords + '\'' +
                ", uDate=" + uDate +
                '}';
    }

}