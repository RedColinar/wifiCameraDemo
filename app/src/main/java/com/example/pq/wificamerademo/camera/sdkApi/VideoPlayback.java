package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.type.ICatchAudioFormat;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/4 17:43
 * @description
 */
public class VideoPlayback {

    private ICatchWificamVideoPlayback videoPlayback;

    private VideoPlayback() {
        init();
    }

    private static class VideoPlaybackHolder {
        private static final VideoPlayback sInstance = new VideoPlayback();
    }

    public static VideoPlayback getInstance() {
        return VideoPlaybackHolder.sInstance;
    }

    public void init() {
        videoPlayback = MyApplication.getMyCamera().getVideoPlayback();
    }

    public ICatchAudioFormat getAudioFormat() {
        return ExceptionHelper.invoke(videoPlayback::getAudioFormat);
    }
}
