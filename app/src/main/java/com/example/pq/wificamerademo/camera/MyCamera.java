package com.example.pq.wificamerademo.camera;

import com.example.pq.wificamerademo.camera.sdkApi.CameraAction;
import com.example.pq.wificamerademo.camera.sdkApi.CameraFile;
import com.example.pq.wificamerademo.camera.sdkApi.CameraFixedInfo;
import com.example.pq.wificamerademo.camera.sdkApi.CameraProperties;
import com.example.pq.wificamerademo.camera.sdkApi.CameraSession;
import com.example.pq.wificamerademo.camera.sdkApi.CameraState;
import com.example.pq.wificamerademo.camera.sdkApi.VideoPlayback;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.ICatchWificamState;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 11:22
 * @description
 */
public class MyCamera {

    public boolean isStreaming = false;

    private CameraSession mCameraSession;

    private ICatchWificamPlayback photoPlayback;
    private ICatchWificamControl cameraControl;
    private ICatchWificamVideoPlayback videoPlayback;
    private ICatchWificamPreview previewStream;
    private ICatchWificamInfo cameraInfo;
    private ICatchWificamProperty cameraProperty;
    private ICatchWificamState cameraState;
    private ICatchWificamAssist cameraAssist;

    public MyCamera() {
        this.mCameraSession = new CameraSession();
    }

    /** 在 prepareSession 后且 wifi 连接正常后初始化 */
    public void initCamera() {
        try {
            photoPlayback = mCameraSession.getCameraSession().getPlaybackClient();
            cameraControl = mCameraSession.getCameraSession().getControlClient();
            previewStream = mCameraSession.getCameraSession().getPreviewClient();
            videoPlayback = mCameraSession.getCameraSession().getVideoPlaybackClient();
            cameraProperty = mCameraSession.getCameraSession().getPropertyClient();
            cameraInfo = mCameraSession.getCameraSession().getInfoClient();
            cameraState = mCameraSession.getCameraSession().getStateClient();
            cameraAssist = ICatchWificamAssist.getInstance();

            CameraAction.getInstance().init();
            CameraFile.getInstance().init();
            CameraFixedInfo.getInstance().init();
            CameraProperties.getInstance().init();
            CameraState.getInstance().init();
            VideoPlayback.getInstance().init();

        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
    }

    public CameraSession getCameraSession() {
        return mCameraSession;
    }

    public ICatchWificamPreview getPreviewStream() {
        return previewStream;
    }

    public ICatchWificamControl getCameraControl() {
        return cameraControl;
    }

    public ICatchWificamAssist getCameraAssist() {
        return cameraAssist;
    }

    public ICatchWificamInfo getCameraInfo() {
        return cameraInfo;
    }

    public ICatchWificamProperty getCameraProperty() {
        return cameraProperty;
    }

    public ICatchWificamState getCameraState() {
        return cameraState;
    }

    public ICatchWificamPlayback getCameraPlayback() {
        return photoPlayback;
    }

    public ICatchWificamVideoPlayback getVideoPlayback() {
        return videoPlayback;
    }

    public Boolean destroyCamera() {
        return mCameraSession.destroySession();
    }
}
