package com.example.control3.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class RecordItem {
    private String rCode;

    private String iCode;

    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date rStartTime;

    private Integer rLastTime;

    private Integer rStrong;

    private String rMode;

    public String getrCode() {
        return rCode;
    }

    public void setrCode(String rCode) {
        this.rCode = rCode;
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

    public String getiCode() {
        return iCode;
    }

    public void setiCode(String iCode) {
        this.iCode = iCode;
    }

    @Override
    public String toString() {
        return "RecordItem{" +
                "rCode='" + rCode + '\'' +
                ", iCode='" + iCode + '\'' +
                ", rStartTime=" + rStartTime +
                ", rLastTime=" + rLastTime +
                ", rStrong=" + rStrong +
                ", rMode='" + rMode + '\'' +
                '}';
    }
}
