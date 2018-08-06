package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchPreviewMode;
import com.icatch.wificam.customer.type.ICatchStreamParam;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 16:15
 * @description
 */
public class PreviewStream {

    private PreviewStream() {}

    public static PreviewStream getInstance() {
        return PreviewStreamHolder.sInstance;
    }

    private static class PreviewStreamHolder {
        private static final PreviewStream sInstance = new PreviewStream();
    }

    public ICatchVideoFormat getVideoFormat(ICatchWificamPreview previewStreamControl) {
        return ExceptionHelper.invoke(previewStreamControl::getVideoFormat);
    }

    public ICatchAudioFormat getAudioFormat(ICatchWificamPreview previewStreamControl) {
        return ExceptionHelper.invoke(previewStreamControl::getAudioFormat);
    }

    public boolean supportAudio(ICatchWificamPreview previewStreamControl) {
        return ExceptionHelper.invokeBool(previewStreamControl::containsAudioStream);
    }

    public boolean changePreviewMode(ICatchWificamPreview previewStreamControl, ICatchPreviewMode previewMode) {
        return ExceptionHelper.invokeBool(() -> previewStreamControl.changePreviewMode(previewMode));
    }

    public boolean startMediaStream(ICatchWificamPreview previewStreamControl, ICatchStreamParam param, ICatchPreviewMode previewMode, boolean disableAudio) {
        return ExceptionHelper.invokeBool(() -> previewStreamControl.start(param, previewMode, disableAudio));
    }

    public boolean stopMediaStream(ICatchWificamPreview previewStreamControl) {
        return ExceptionHelper.invokeBool(previewStreamControl::stop);
    }
}
