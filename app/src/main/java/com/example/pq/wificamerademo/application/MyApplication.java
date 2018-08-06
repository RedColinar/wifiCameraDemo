package com.example.pq.wificamerademo.application;

import android.app.Application;

import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.camera.SdkEvent;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 10:59
 * @description
 */
public class MyApplication extends Application {
    private static MyCamera myCamera;
    private SdkEvent sdkEvent;

    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sdkEvent = new SdkEvent();
    }

    public static MyCamera getMyCamera() {
        return myCamera;
    }

    public static void setMyCamera(MyCamera myCamera) {
        MyApplication.myCamera = myCamera;
    }

    public SdkEvent getSdkEvent() {
        return sdkEvent;
    }

    public void setSdkEvent(SdkEvent sdkEvent) {
        this.sdkEvent = sdkEvent;
    }

}
