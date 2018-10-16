package com.example.control3.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class RecordInformation {
    private int code;
    //状态码 0表示未登录 1表示成功登录

    private List<RecordItem> records;

    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    public List<RecordItem> getRecords() {
        return records;
    }

    public void setRecords(List<RecordItem> records) {
        this.records = records;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "RecordInformation{" +
                "code=" + code +
                ", records=" + records +
                ", date=" + date +
                '}';
    }
}
