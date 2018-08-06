package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 16:52
 * @description
 */
public class CameraFixedInfo {

    private ICatchWificamInfo cameraFixedInfo;
    private ICatchWificamControl cameraControl;

    private CameraFixedInfo() {
        cameraFixedInfo = MyApplication.getMyCamera().getCameraInfo();
        cameraControl = MyApplication.getMyCamera().getCameraControl();
    }

    private static class CameraFixedInfoHolder {
        private static final CameraFixedInfo sInstance = new CameraFixedInfo();
    }

    public static CameraFixedInfo getInstance() {
        return CameraFixedInfoHolder.sInstance;
    }

    public String getCameraName() {
        String name = "";
        try {
            name = cameraFixedInfo.getCameraProductName();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getCameraVersion() {
        String version = "";
        try {
            version = cameraFixedInfo.getCameraFWVersion();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        return version;
    }

    public String getCameraMacAddress() {
        return cameraControl.getMacAddress();
    }
}
