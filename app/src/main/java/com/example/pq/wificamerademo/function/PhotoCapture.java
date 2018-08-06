package com.example.pq.wificamerademo.function;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.sdkApi.CameraAction;
import com.example.pq.wificamerademo.camera.sdkApi.CameraProperties;
import com.icatch.wificam.customer.type.ICatchCameraProperty;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhang yanhu C001012 on 2015/12/30 13:22.
 */
public class PhotoCapture {
    private static final String TAG = "PhotoCapture";
    private MediaPlayer stillCaptureStartBeep;
    private MediaPlayer delayBeep;
    private MediaPlayer continuousCaptureBeep;
    private static PhotoCapture photoCapture;
    private OnStopPreviewListener onStopPreviewListener;
    private static final int TYPE_BURST_CAPTURE = 1;
    private static final int TYPE_NORMAL_CAPTURE = 2;

    public static PhotoCapture getInstance() {
        if (photoCapture == null) {
            photoCapture = new PhotoCapture();
        }
        return photoCapture;
    }

    private PhotoCapture() {
        Context context = MyApplication.instance;
        // stillCaptureStartBeep = MediaPlayer.create(context, R.raw.captureshutter);
        // delayBeep = MediaPlayer.create(context, R.raw.delay_beep);
        // continuousCaptureBeep = MediaPlayer.create(context, R.raw.captureburst);
    }
    public void startCapture(){
        new CaptureThread().run();
    }

    class CaptureThread implements Runnable {
        @Override
        public void run() {
            int delayTime;

            if(CameraProperties.getInstance().hasProperty(ICatchCameraProperty.ICH_CAP_CAPTURE_DELAY)){
                delayTime = CameraProperties.getInstance().getCurrentCaptureDelay();
            }else {
                delayTime = 0;
            }

            if (delayTime < 1000) {//ms
                onStopPreviewListener.onStop();
            } else if (CameraProperties.getInstance().hasProperty(0xD7F0)) {//do not stopMPreview media stream and preview right now
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        onStopPreviewListener.onStop();
                    }
                };
                Timer timer = new Timer(true);
                timer.schedule(task, delayTime - 500);
            } else {
                onStopPreviewListener.onStop();
            }

            //start capture audio
            int needCaptureCount = 1;
            if (CameraProperties.getInstance().hasProperty(ICatchCameraProperty.ICH_CAP_BURST_NUMBER)) {
                needCaptureCount = CameraProperties.getInstance().getCurrentAppBurstNum();
            }
            if (needCaptureCount == 1) {
                CaptureAudioTask captureAudioTask = new CaptureAudioTask(needCaptureCount, TYPE_NORMAL_CAPTURE);
                Timer captureAudioTimer = new Timer(true);

                captureAudioTimer.schedule(captureAudioTask, delayTime);
            } else {
                CaptureAudioTask captureAudioTask = new CaptureAudioTask(needCaptureCount, TYPE_BURST_CAPTURE);
                Timer captureAudioTimer = new Timer(true);
                captureAudioTimer.schedule(captureAudioTask, delayTime, 280);
            }

            //start delay audio
            int count = delayTime / 1000;
            int timerDelay = 0;
            if (delayTime >= 5000) {
                Timer delayTimer = new Timer(true);
                DelayTimerTask delayTimerTask = new DelayTimerTask(count / 2, delayTimer);
                delayTimer.schedule(delayTimerTask, 0, 1000);
                timerDelay = delayTime;
            } else {
                timerDelay = 0;
                count = delayTime / 500;
            }
            if (delayTime >= 3000) {
                Timer delayTimer1 = new Timer(true);
                DelayTimerTask delayTimerTask1 = new DelayTimerTask(count / 2, delayTimer1);
                delayTimer1.schedule(delayTimerTask1, timerDelay / 2, 500);
                timerDelay = delayTime;
            } else {
                timerDelay = 0;
                count = delayTime / 250;
            }
            Timer delayTimer2 = new Timer(true);
            DelayTimerTask delayTimerTask2 = new DelayTimerTask(count, delayTimer2);
            delayTimer2.schedule(delayTimerTask2, timerDelay - timerDelay / 4, 250);
            CameraAction.getInstance().triggerCapturePhoto();
        }
    }

    public void addOnStopPreviewListener(OnStopPreviewListener onStopPreviewListener) {
        this.onStopPreviewListener = onStopPreviewListener;
    }

    public interface OnStopPreviewListener {
        void onStop();
    }

    private class CaptureAudioTask extends TimerTask {
        private int burstNumber;
        private int type = TYPE_NORMAL_CAPTURE;

        public CaptureAudioTask(int burstNumber, int type) {
            this.burstNumber = burstNumber;
            this.type = type;
        }

        @Override
        public void run() {
            if (type == TYPE_NORMAL_CAPTURE) {
                if (burstNumber > 0) {
                    Log.i(TAG, "CaptureAudioTask remainBurstNumer =" + burstNumber);
                    stillCaptureStartBeep.start();
                    burstNumber--;
                } else {
                    cancel();
                }
            } else {
                if (burstNumber > 0) {
                    Log.i(TAG, "CaptureAudioTask remainBurstNumer =" + burstNumber);
                    continuousCaptureBeep.start();
                    burstNumber--;
                } else {
                    cancel();
                }
            }

        }
    }

    private class DelayTimerTask extends TimerTask {
        private int count;
        private Timer timer;

        public DelayTimerTask(int count, Timer timer) {
            this.count = count;
            this.timer = timer;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (count-- > 0) {
                delayBeep.start();
            } else {
                if (timer != null) {
                    timer.cancel();
                }
            }
        }
    }

}
