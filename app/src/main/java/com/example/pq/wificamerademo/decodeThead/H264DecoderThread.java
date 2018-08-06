package com.example.pq.wificamerademo.decodeThead;

import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.camera.sdkApi.PreviewStream;
import com.example.pq.wificamerademo.camera.sdkApi.VideoPlayback;
import com.example.pq.wificamerademo.constants.GlobalInfo;
import com.example.pq.wificamerademo.widget.MPreview;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchPbStreamPausedException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static com.example.pq.wificamerademo.widget.MPreview.RT_PREVIEW_MODE;
import static com.example.pq.wificamerademo.widget.MPreview.VIDEO_PB_MODE;

public class H264DecoderThread {
    private static final String TAG = "H264DecoderThread--";
    private final ICatchWificamPreview previewStreamControl;
    private ICatchWificamVideoPlayback videoPbControl;
    private final MPreview mPreview;

    private PreviewStream previewStream = PreviewStream.getInstance();
    private VideoPlayback videoPlayback = VideoPlayback.getInstance();
    private SurfaceHolder surfaceHolder;
    private VideoThread videoThread;
    private AudioThread audioThread;
    private boolean audioPlayFlag = false;
    private int BUFFER_LENGTH = 1280 * 720 * 4;
    //    private int timeout = 60000;// us
    private int timeout = 20000;// us
    private MediaCodec decoder;
    private int previewLaunchMode;
    private MPreview.VideoFramePtsChangedListener videoPbUpdateBarLitener;
    private ICatchVideoFormat videoFormat;
    private int frameWidth;
    private int frameHeight;
    private MPreview.OnDecodeTimeListener onDecodeTimeListener;
    private int fps;

    public H264DecoderThread(MyCamera mCamera, SurfaceHolder holder, MPreview mPreview,
                             int previewLaunchMode, ICatchVideoFormat iCatchVideoFormat,
                             MPreview.VideoFramePtsChangedListener videoPbUpdateBarLitener) {
        this.surfaceHolder = holder;
        this.mPreview = mPreview;
        this.previewLaunchMode = previewLaunchMode;
        previewStreamControl = mCamera.getPreviewStream();
        videoPbControl = mCamera.getVideoPlayback();
        this.videoFormat = iCatchVideoFormat;
        this.videoPbUpdateBarLitener = videoPbUpdateBarLitener;
        holder.setFormat(PixelFormat.RGBA_8888);
        if (videoFormat != null) {
            frameWidth = videoFormat.getVideoW();
            frameHeight = videoFormat.getVideoH();
            fps = videoFormat.getFps();
        }
        if (fps > 30) {
            timeout = 30000;
        } else {
            timeout = 60000;
        }
    }

    public void setOnDecodeTimeListener(MPreview.OnDecodeTimeListener onDecodeTimeListener) {
        this.onDecodeTimeListener = onDecodeTimeListener;
    }


    public void start(boolean enableAudio, boolean enableVideo) {
        setFormat();
        if (enableAudio) {
            audioThread = new AudioThread();
            audioThread.start();
        }
        if (enableVideo) {
            videoThread = new VideoThread();
            videoThread.start();
        }
    }

    public boolean isAlive() {
        if (videoThread != null && videoThread.isAlive()) {
            return true;
        }
        if (audioThread != null && audioThread.isAlive()) {
            return true;
        }
        return false;
    }

    public void stop() {
        if (audioThread != null) {
            audioThread.requestExitAndWait();
        }
        if (videoThread != null) {
            videoThread.requestExitAndWait();
        }
        audioPlayFlag = false;
    }

    private long videoShowtime = 0;
    private double curVideoPts = 0;

    private class VideoThread extends Thread {

        private boolean done = false;
        private MediaCodec.BufferInfo info;
        long startTime = 0;
        int frameSize = 0;

        VideoThread() {
            super();
            done = false;
        }

        @Override
        public void run() {
            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            info = new MediaCodec.BufferInfo();
//            byte[] mPixel = new byte[BUFFER_LENGTH];
            byte[] mPixel = new byte[frameWidth * frameHeight * 4];
            ICatchFrameBuffer frameBuffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            frameBuffer.setBuffer(mPixel);
            int inIndex = -1;
            int sampleSize = 0;
            long pts = 0;
            boolean retvalue = true;
            boolean isFirst = true;
            long lastTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            long currentTime;
            while (!done) {
                retvalue = false;
                curVideoPts = -1;

                try {
//                    AppLog.d(TAG, "end time=" + (System.currentTimeMillis() - endTime));
//                    endTime = System.currentTimeMillis();
                    if (previewLaunchMode == RT_PREVIEW_MODE) {
                        retvalue = previewStreamControl.getNextVideoFrame(frameBuffer);
                    } else {
                        retvalue = videoPbControl.getNextVideoFrame(frameBuffer);
                    }
                    if (!retvalue) {
                        continue;
                    }
                } catch (IchTryAgainException ex) {
                    ex.printStackTrace();
                    retvalue = false;
//                    AppLog.e(TAG, "getNextVideoFrame " + ex.getClass().getSimpleName());
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    retvalue = false;
                    break;
                }
                if (frameBuffer.getFrameSize() <= 0 || frameBuffer == null) {
                    retvalue = false;
                    continue;
                }
                if (!retvalue) {
                    continue;
                }
                inIndex = decoder.dequeueInputBuffer(timeout);
                curVideoPts = frameBuffer.getPresentationTime();

                frameSize++;
                if (isFirst) {
                    isFirst = false;
                    startTime = System.currentTimeMillis();
                }
                if (inIndex >= 0) {
                    sampleSize = frameBuffer.getFrameSize();
                    pts = (long) (frameBuffer.getPresentationTime() * 1000 * 1000); // (seconds
                    ByteBuffer buffer = inputBuffers[inIndex];
                    buffer.clear();
                    buffer.rewind();
                    buffer.put(frameBuffer.getBuffer(), 0, sampleSize);
                    decoder.queueInputBuffer(inIndex, 0, sampleSize, pts, 0);
                }
                int outBufId = decoder.dequeueOutputBuffer(info, timeout);
                if (outBufId >= 0) {
                    //AppLog.d( TAG, "do decoder and display....." );
                    decoder.releaseOutputBuffer(outBufId, true);
                    if (!audioPlayFlag) {
                        audioPlayFlag = true;
                        GlobalInfo.videoCacheNum = frameSize;
                        videoShowtime = System.currentTimeMillis();
                        Log.d(TAG, "ok show image!.....................startTime= " + (System.currentTimeMillis() - startTime) + " frameSize=" + frameSize
                                + " curVideoPts=" + curVideoPts);
                    }
                    if (previewLaunchMode == VIDEO_PB_MODE && videoPbUpdateBarLitener != null) {
                        videoPbUpdateBarLitener.onFramePtsChanged(frameBuffer.getPresentationTime());
                    }
                }
                if (previewLaunchMode == RT_PREVIEW_MODE && onDecodeTimeListener != null && frameBuffer != null) {
                    if (System.currentTimeMillis() - lastTime > 500) {
                        lastTime = System.currentTimeMillis();
                        long decodeTime = frameBuffer.getDecodeTime();
                        onDecodeTimeListener.decodeTime(decodeTime);
                    }
                }
            }
            try {
                decoder.stop();
                decoder.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
            if (this.isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setFormat() {
        /* create & config android.media.MediaFormat */
        ICatchVideoFormat videoFormat = this.videoFormat;
        int w = videoFormat.getVideoW();
        int h = videoFormat.getVideoH();
        String type = videoFormat.getMineType();
        MediaFormat format = MediaFormat.createVideoFormat(type, w, h);

        if (previewLaunchMode == RT_PREVIEW_MODE) {
            format.setByteBuffer("csd-0", ByteBuffer.wrap(videoFormat.getCsd_0(), 0, videoFormat.getCsd_0_size()));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(videoFormat.getCsd_1(), 0, videoFormat.getCsd_0_size()));
            format.setInteger("durationUs", videoFormat.getDurationUs());
            format.setInteger("max-input-size", videoFormat.getMaxInputSize());
        }

		/* create & config android.media.MediaCodec */
        String ret = videoFormat.getMineType();
        Log.i(TAG, "h264 videoFormat.getMineType()=" + ret);
        decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder.configure(format, surfaceHolder.getSurface(), null, 0);
        decoder.start();
    }

    private class AudioThread extends Thread {

        private boolean done = false;
        private LinkedList<ICatchFrameBuffer> audioQueue;

        private AudioTrack audioTrack;
        boolean isFirstShow = true;


        public void run() {
            ICatchFrameBuffer temp;
            ICatchAudioFormat audioFormat;
            if (previewLaunchMode == RT_PREVIEW_MODE) {
                audioFormat = previewStream.getAudioFormat(previewStreamControl);
            } else {
                audioFormat = videoPlayback.getAudioFormat();
            }
            if (audioFormat == null) {
                return;
            }

            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(),
                    audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_LEFT,
                    audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(),
                    audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_LEFT,
                    audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, bufferSize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioQueue = new LinkedList<ICatchFrameBuffer>();
            boolean ret = false;

            ICatchFrameBuffer tempBuffer = new ICatchFrameBuffer(1024 * 50);
            byte[] testaudioBuffer = new byte[1024 * 50];
            tempBuffer.setBuffer(testaudioBuffer);
            while (!done) {
                ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer(1024 * 50);
                byte[] audioBuffer = new byte[1024 * 50];
                icatchBuffer.setBuffer(audioBuffer);

                Boolean b = ExceptionHelper.invokeWithExceptionHandler(() -> {
                    if (previewLaunchMode == RT_PREVIEW_MODE) {
                        return previewStreamControl.getNextAudioFrame(icatchBuffer);
                    } else {
                        return videoPbControl.getNextAudioFrame(icatchBuffer);
                    }
                }, (Throwable t) -> {
                    if (t instanceof IchAudioStreamClosedException) {
                        if (audioQueue != null && audioQueue.size() > 0) {
                            while (audioQueue.size() > 0) {
                                ICatchFrameBuffer audioTemp = audioQueue.poll();
                                if (audioTemp != null) {
                                    audioTrack.write(audioTemp.getBuffer(), 0, audioTemp.getFrameSize());
                                }
                            }
                        }
                    }
                });
                ret = b == null ? false : b;

                if (!ret) {
                    continue;
                } else {
                    audioQueue.offer(icatchBuffer);
                }
                if (audioPlayFlag) {
                    temp = audioQueue.poll();
                    if (temp == null) {
                        continue;
                    }
                    double tempPts = curVideoPts;
                    double delayTime = 0;
                    if (isFirstShow) {
                        delayTime = (1 / GlobalInfo.curFps) * GlobalInfo.videoCacheNum;
                        Log.d(TAG, "delayTime=" + delayTime + " AppInfo.videoCacheNum=" + GlobalInfo.videoCacheNum + " AppInfo.curFps=" + GlobalInfo.curFps);
                        isFirstShow = false;
                    }
                    if (curVideoPts == -1) {
//                        AppLog.d(TAG,"tempPts == -1");
                    } else {
                        if (temp.getPresentationTime() - (tempPts - delayTime) > GlobalInfo.THRESHOLD_TIME) {
                            audioQueue.addFirst(temp);
                            Log.d(TAG, "audioQueue.addFirst(temp);");
                            continue;
                        }
                        if (temp.getPresentationTime() - (tempPts - delayTime) < -GlobalInfo.THRESHOLD_TIME && audioQueue.size() > 0) {
                            //JIRA BUG ICOM-3618 Begin modefy by b.jiang 20160825
                            while (temp.getPresentationTime() - (tempPts - delayTime) < 0 && audioQueue.size() > 0) {
                                temp = audioQueue.poll();
                                if (temp != null) {
                                    Log.d(TAG, "audioQueue.poll()----tempPts=" + tempPts + " curVideoPts=" + curVideoPts + " curPts=" + temp
                                            .getPresentationTime() + " audioQueue size=" + audioQueue.size());
                                }
                            }
                        }
                    }
                    audioTrack.write(temp.getBuffer(), 0, temp.getFrameSize());
                }
            }
            audioTrack.stop();
            audioTrack.release();
        }

        void requestExitAndWait() {
            done = true;
            try {
                join();
            } catch (InterruptedException ignored) {}
        }
    }
}
