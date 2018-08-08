package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.example.pq.wificamerademo.constants.PropertyId;
import com.example.pq.wificamerademo.util.DataConvertUtils;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.type.ICatchCodec;
import com.icatch.wificam.customer.type.ICatchMode;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

import java.util.List;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 17:45
 * @description
 */
public class CameraProperties {
    private List<Integer> supportedPropertyList;
    private List<ICatchMode> modeList;

    private ICatchWificamProperty cameraProperty;
    private ICatchWificamControl cameraControl;

    private CameraProperties() {
        init();
    }

    private static class CameraPropertiesHolder {
        private static final CameraProperties sInstance = new CameraProperties();
    }

    public static CameraProperties getInstance() {
        return CameraPropertiesHolder.sInstance;
    }

    public void init() {
        cameraProperty = MyApplication.getMyCamera().getCameraProperty();
        cameraControl = MyApplication.getMyCamera().getCameraControl();
        supportedPropertyList = null;
    }

    public List<String> getSupportedImageSizes() {
        return ExceptionHelper.invoke(cameraProperty::getSupportedImageSizes);
    }

    public List<String> getSupportedVideoSizes() {
        return ExceptionHelper.invoke(cameraProperty::getSupportedVideoSizes);
    }

    public boolean isSDCardExist() {
        return ExceptionHelper.invokeBool(cameraControl::isSDCardExist);
    }

    public int getBatteryElectric() {
        return ExceptionHelper.invokeInt(cameraControl::getCurrentBatteryLevel, 0);
    }

    public int getPreviewCacheTime() {
        return ExceptionHelper.invokeInt(cameraProperty::getPreviewCacheTime, -1);
    }

    public int getRemainImageNum() {
        return ExceptionHelper.invokeInt(cameraControl::getFreeSpaceInImages, -1);
    }

    public int getCurrentCaptureDelay() {
        return ExceptionHelper.invokeInt(cameraProperty::getCurrentCaptureDelay, 0);
    }

    public int getCurrentAppBurstNum() {
        int number = ExceptionHelper.invokeInt(cameraProperty::getCurrentBurstNumber, 0xff);
        return DataConvertUtils.getBurstConverFromFw(number);
    }

    public boolean setCaptureDelayMode(int value) {
        return ExceptionHelper.invokeBool(() -> cameraProperty.setPropertyValue(PropertyId.CAPTURE_DELAY_MODE, value));
    }

    public String getCurrentStreamInfo() {
        ICatchVideoFormat videoFormat = ExceptionHelper.invoke(cameraProperty::getCurrentStreamingInfo);
        if (videoFormat == null) return null;
        String bestResolution = null;
        String temp = "W=" + videoFormat.getVideoW() + "&H=" + videoFormat.getVideoH() + "&BR=" + videoFormat.getBitrate();
        if (hasProperty(0xd7ae)) {
            if (videoFormat.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                bestResolution = "H264?" + temp + "&FPS=" + videoFormat.getFps() + "&";
            } else if (videoFormat.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                bestResolution = "MJPG?" + temp + "&FPS=" + videoFormat.getFps() + "&";
            }
        } else {
            if (videoFormat.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                bestResolution = "H264?" + temp;
            } else if (videoFormat.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                bestResolution = "MJPG?" + temp;
            }
        }

        return bestResolution;
    }

    public boolean hasProperty(int property) {
        if (supportedPropertyList == null) {
            supportedPropertyList = getSupportedProperties();
        }

        return supportedPropertyList.contains(property);
    }

    private List<Integer> getSupportedProperties() {
        return ExceptionHelper.invoke(cameraProperty::getSupportedProperties);
    }
}
