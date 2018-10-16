package com.example.control3.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Record {
    private Integer rId;

    private Integer uId;

    private Integer iId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date rStartTime;

    private Integer rLastTime;

    private Integer rStrong;

    private String rMode;

    private String rDel;

    private String rCode;

    private String token;

    public Integer getrId() {
        return rId;
    }

    public void setrId(Integer rId) {
        this.rId = rId;
    }

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public Integer getiId() {
        return iId;
    }

    public void setiId(Integer iId) {
        this.iId = iId;
    }

    public Date getrStartTime() {
        return rStartTime;
    }

    public void setrStartTime(Date rStartTime) {
        this.rStartTime = rStartTime;
    }

    public Integer getrLastTime() {
        return rLastTime;
    }

    public void setrLastTime(Integer rLastTime) {
        this.rLastTime = rLastTime;
    }

    public Integer getrStrong() {
        return rStrong;
    }

    public void setrStrong(Integer rStrong) {
        this.rStrong = rStrong;
    }

    public String getrMode() {
        return rMode;
    }

    public void setrMode(String rMode) {
        this.rMode = rMode;
    }

    public String getrDel() {
        return rDel;
    }

    public void setrDel(String rDel) {
        this.rDel = rDel == null ? null : rDel.trim();
    }

    public String getrCode() {
        return rCode;
    }

    public void setrCode(String rCode) {
        this.rCode = rCode == null ? null : rCode.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}