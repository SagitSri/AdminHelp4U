package com.apk.sagitsri.adminhelp4u;

import java.util.Date;

/**
 * Created by sagitsri on 29/10/19.
 */

public class DataModel {

    private double lat,lng;
    private String dt;
    private String tm;
    private String mob;

    public DataModel() {
    }

    public DataModel(double lt, double lg, String dt, String tm, String mob) {
        this.lat = lt;
        this.lng = lg;
        this.dt = dt;
        this.tm = tm;
        this.mob = mob;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTm() {
        return tm;
    }

    public void setTm(String tm) {
        this.tm = tm;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getMB() {
        return mob;
    }

    public void setMB(String dt) {
        this.mob = mob;
    }
}
