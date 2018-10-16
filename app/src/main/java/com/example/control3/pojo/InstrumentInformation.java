package com.example.control3.pojo;

public class InstrumentInformation {
    private Integer iId;

    private String iType;

    private String iDel;

    private String iSupplier;

    private String iCode;

    private Integer iTimes;

    private Integer iAddtimes;

    private Integer iAlltimes;

    private String iIson;

    public Integer getiId() {
        return iId;
    }

    public void setiId(Integer iId) {
        this.iId = iId;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType == null ? null : iType.trim();
    }

    public String getiDel() {
        return iDel;
    }

    public void setiDel(String iDel) {
        this.iDel = iDel == null ? null : iDel.trim();
    }

    public String getiSupplier() {
        return iSupplier;
    }

    public void setiSupplier(String iSupplier) {
        this.iSupplier = iSupplier == null ? null : iSupplier.trim();
    }

    public String getiCode() {
        return iCode;
    }

    public void setiCode(String iCode) {
        this.iCode = iCode == null ? null : iCode.trim();
    }

    public Integer getiTimes() {
        return iTimes;
    }

    public void setiTimes(Integer iTimes) {
        this.iTimes = iTimes;
    }

    public Integer getiAddtimes() {
        return iAddtimes;
    }

    public void setiAddtimes(Integer iAddtimes) {
        this.iAddtimes = iAddtimes;
    }

    public Integer getiAlltimes() {
        return iAlltimes;
    }

    public void setiAlltimes(Integer iAlltimes) {
        this.iAlltimes = iAlltimes;
    }

    public String getiIson() {
        return iIson;
    }

    public void setiIson(String iIson) {
        this.iIson = iIson == null ? null : iIson.trim();
    }

    @Override
    public String toString() {
        return "InstrumentInformation{" +
                "iId=" + iId +
                ", iType='" + iType + '\'' +
                ", iDel='" + iDel + '\'' +
                ", iSupplier='" + iSupplier + '\'' +
                ", iCode='" + iCode + '\'' +
                ", iTimes=" + iTimes +
                ", iAddtimes=" + iAddtimes +
                ", iAlltimes=" + iAlltimes +
                ", iIson='" + iIson + '\'' +
                '}';
    }
}
