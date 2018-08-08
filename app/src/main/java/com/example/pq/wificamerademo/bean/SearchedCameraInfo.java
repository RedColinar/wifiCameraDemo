package com.example.pq.wificamerademo.bean;

public class SearchedCameraInfo {
    public String cameraName;
    public String cameraIp;
    public int cameraMode;
    public String uid;

    public SearchedCameraInfo(String cameraName, String cameraIp, int cameraMode, String uid) {
        this.cameraName = cameraName;
        this.cameraIp = cameraIp;
        this.cameraMode = cameraMode;
        this.uid = uid;
    }
}
