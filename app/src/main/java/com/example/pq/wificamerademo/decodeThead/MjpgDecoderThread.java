package com.example.pq.wificamerademo.decodeThead;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.camera.sdkApi.PreviewStream;
import com.example.pq.wificamerademo.util.ScaleUtils;
import com.example.pq.wificamerademo.widget.MPreview;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

import java.nio.ByteBuffer;

public class MjpgDecoderThread {
    private static final String TAG = "MjpgDecoderThread";
    private final ICatchWificamPreview previewStreamControl;
    // private ICatchWificamVideoPlayback videoPbControl;
    private final MPreview mPreview;
    private Bitmap videoFrameBitmap;
    private int frameWidth;
    private int frameHeight;
    private PreviewStream previewStream = PreviewStream.getInstance();
    // private VideoPlayback videoPlayback = VideoPlayback.getInstance();
    private SurfaceHolder surfaceHolder;
    private AudioThread audioThread;
    private VideoThread videoThread;
    private int previewLaunchMode;
    private MPreview.VideoFramePtsChangedListener videoPbUpdateBarListener;
    private Rect drawFrameRect;
    private ICatchVideoFormat videoFormat;
    private MPreview.OnDecodeTimeListener onDecodeTimeListener;

    public void setOnDecodeTimeListener(MPreview.OnDecodeTimeListener onDecodeTimeListener) {
        this.onDecodeTimeListener = onDecodeTimeListener;
    }

    public MjpgDecoderThread(MyCamera mCamera, SurfaceHolder holder, MPreview mPreview,
                             int previewLaunchMode, ICatchVideoFormat iCatchVideoFormat,
                             MPreview.VideoFramePtsChangedListener videoPbUpdateBarListener) {
        this.surfaceHolder = holder;
        this.mPreview = mPreview;
        this.previewLaunchMode = previewLaunchMode;
        previewStreamControl = mCamera.getPreviewStream();
        // videoPbControl = mCamera.getVideoPlaybackClint();
        this.videoPbUpdateBarListener = videoPbUpdateBarListener;
        this.videoFormat = iCatchVideoFormat;
        if (videoFormat != null) {
            frameWidth = videoFormat.getVideoW();
            frameHeight = videoFormat.getVideoH();
        }
        holder.setFormat(PixelFormat.RGBA_8888);
    }

    public void start(boolean enableAudio, boolean enableVideo) {
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
        return (videoThread != null && videoThread.isAlive())
                || (audioThread != null && audioThread.isAlive());
    }

    public void stop() {
        if (audioThread != null) {
            audioThread.requestExitAndWait();
        }
        if (videoThread != null) {
            videoThread.requestExitAndWait();
        }
    }

    private class VideoThread extends Thread {
        private boolean done;
        private ByteBuffer bmpBuf;
        private byte[] pixelBuf;

        VideoThread() {
            super();
            done = false;
            pixelBuf = new byte[frameWidth * frameHeight * 4];
            bmpBuf = ByteBuffer.wrap(pixelBuf);
            // Trigger onDraw with those initialize parameters
            videoFrameBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
            drawFrameRect = new Rect(0, 0, frameWidth, frameHeight);
        }

        @Override
        public void run() {
            ICatchFrameBuffer buffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            buffer.setBuffer(pixelBuf);
            boolean temp;
            boolean isSaveBitmapToDb = false;
            boolean isFirstFrame = true;
            long lastTime = System.currentTimeMillis();
            while (!done) {
                temp = false;
                try {
                    if (previewLaunchMode == MPreview.RT_PREVIEW_MODE) {
                        temp = previewStreamControl.getNextVideoFrame(buffer);
                    } else {
                        // temp = videoPbControl.getNextVideoFrame(buffer);
                    }
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                    continue;
                } catch (Exception ex) {
                    Log.e(TAG, "getNextVideoFrame " + ex.getClass().getSimpleName());
                    ex.printStackTrace();
                    return;
                }
                if (!temp) {
                    Log.e(TAG,"getNextVideoFrame failed");
                    continue;
                }
                if (buffer.getFrameSize() == 0) {
                    continue;
                }

                bmpBuf.rewind();
                if (videoFrameBitmap == null) {
                    continue;
                }
                if (isFirstFrame) {
                    isFirstFrame = false;
                    Log.i(TAG, "get first Frame");
                }
                videoFrameBitmap.copyPixelsFromBuffer(bmpBuf);

                if (!isSaveBitmapToDb) {
                    if (videoFrameBitmap != null && previewLaunchMode == MPreview.RT_PREVIEW_MODE) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // DatabaseHelper.updateCameraPhoto(GlobalInfo.curSlotId, videoFrameBitmap);
                            }
                        }).start();
                        isSaveBitmapToDb = true;
                    }
                }

                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null) {
                    continue;
                }
                int w = mPreview.getWidth();
                int h = mPreview.getHeight();
                drawFrameRect = ScaleUtils.getScaledPosition(frameWidth, frameHeight, w, h);
                canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
                if (onDecodeTimeListener != null) {
                    if (System.currentTimeMillis() - lastTime > 500) {
                        lastTime = System.currentTimeMillis();
                        long decodeTime = buffer.getDecodeTime();
                        onDecodeTimeListener.decodeTime(decodeTime);
                    }
                }
                if (previewLaunchMode == MPreview.VIDEO_PB_MODE && videoPbUpdateBarListener != null) {
                    videoPbUpdateBarListener.onFramePtsChanged(buffer.getPresentationTime());
                }
            }
            Log.i(TAG, "stopMPreview video thread");
        }

        void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
            try {
                join();
            } catch (InterruptedException ignored) {}
        }
    }

    public void redrawBitmap() {
        Log.i(TAG, "redrawBitmap");
        if (videoFrameBitmap != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            int w = mPreview.getWidth();
            int h = mPreview.getHeight();
            Log.i(TAG, "redrawBitmap mPreview.getWidth()=" + mPreview.getWidth());
            Log.i(TAG, "redrawBitmap mPreview.getHeight()=" + mPreview.getHeight());
            Rect drawFrameRect = ScaleUtils.getScaledPosition(frameWidth, frameHeight, w, h);
            canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private class AudioThread extends Thread {
        private boolean done = false;
        private AudioTrack audioTrack;

        public void run() {
            Log.i(TAG, "Run AudioThread");
            ICatchAudioFormat audioFormat = null;
            if (previewLaunchMode == MPreview.RT_PREVIEW_MODE) {
                audioFormat = previewStream.getAudioFormat(previewStreamControl);
            } else {
                // audioFormat = videoPlayback.getAudioFormat();
            }

            if (audioFormat == null) {
                Log.e(TAG, "Run AudioThread audioFormat is null!");
                return;
            }
            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT,
                    bufferSize, AudioTrack.MODE_STREAM);

            audioTrack.play();
            byte[] audioBuffer = new byte[1024 * 50];
            ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer(1024 * 50);
            icatchBuffer.setBuffer(audioBuffer);
            boolean temp = false;
            while (!done) {
                try {
                    if (previewLaunchMode == MPreview.RT_PREVIEW_MODE) {
                        temp = previewStreamControl.getNextAudioFrame(icatchBuffer);
                    } else {
                        // temp = videoPbControl.getNextAudioFrame(icatchBuffer);
                    }
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                    continue;
                } catch (Exception ex) {
                    Log.e(TAG, "getNextVideoFrame " + ex.getClass().getSimpleName());
                    ex.printStackTrace();
                    return;
                }
                if (!temp) {
                    continue;
                }
                audioTrack.write(icatchBuffer.getBuffer(), 0, icatchBuffer.getFrameSize());
            }
            audioTrack.stop();
            audioTrack.release();
            Log.i(TAG, "stopMPreview audio thread");

        }

        void requestExitAndWait() {
            done = true;
            try {
                join();
            } catch (InterruptedException ignored) {}
        }
    }

    public void redrawBitmap(SurfaceHolder surfaceHolder, int w, int h) {
        if (videoFrameBitmap != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                drawFrameRect = ScaleUtils.getScaledPosition(frameWidth, frameHeight, w, h);
                canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
