package com.example.pq.wificamerademo.view.md360;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.plugins.hotspot.MDAbsHotspot;
import com.asha.vrlib.plugins.hotspot.MDSimpleHotspot;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.pq.wificamerademo.R;
import com.example.pq.wificamerademo.glide.GlideApp;
import com.example.pq.wificamerademo.util.FastClickUtils;
import com.example.pq.wificamerademo.util.StorageUtil;
import com.google.android.apps.muzei.render.GLTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.UUID;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/10 10:56
 * @description
 */
public class MD360Activity extends AppCompatActivity {

    Button captureScreen;
    ImageView screenThumbnail;
    private MDVRLibrary mVRLibrary;
    GLSurfaceView glSurfaceView;
    GLTextureView glTextureView;
    private MDVRLibrary.IImageLoadProvider mImageLoadProvider = new ImageLoadProvider();
    private MDPosition logoPosition = MDMutablePosition.newInstance().setY(-8.0f).setYaw(-90.0f);
    private ValueAnimator animator;

    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_md);
        glSurfaceView = findViewById(R.id.gl_view);
        glTextureView = findViewById(R.id.gl_t_view);

        captureScreen = findViewById(R.id.capture_screen);
        screenThumbnail = findViewById(R.id.screen_thumbnail);
        captureScreen.setOnClickListener(v -> {
            if (FastClickUtils.isFastClick()) return;
            // GLSurface
            // capturePicture(this::savePicture);
            // GLTextureView
            savePicture(glTextureView.getBitmap());
        });

        mVRLibrary = createVRLibrary();
        verifyStoragePermissions(this);

        setLogo();
        startAnimation();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL) // DISPLAY_MODE_GLASS
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)
                .asBitmap((final MD360BitmapTexture.Callback callback) -> {
                    // mImageLoadProvider.onProvideBitmap(currentUri(), callback);
                    try {
                        InputStream is = getAssets().open("demo.jpg");
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                        is.close();

                        mVRLibrary.onTextureResize(bitmap.getWidth(), bitmap.getHeight());
                        callback.texture(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .pinchEnabled(true)
                .projectionFactory(new CustomProjectionFactory())
                .build(glTextureView);
    }

    private void savePicture(Bitmap bitmap) {
        bitmap = cutPicture(bitmap);
        File parent = new File(StorageUtil.getDownloadPath(this));
        boolean hasParent = parent.exists() || parent.mkdirs();
        if (!hasParent) {
            return;
        }

        File filepic = new File(StorageUtil.getDownloadPath(this) + UUID.randomUUID().toString() + ".jpg");
        try {
            if (!filepic.exists()) {
                filepic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filepic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Log.d("vr", "savePicture: " + filepic.getAbsolutePath());

            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        GlideApp.with(this)
                .load(filepic)
                .into(screenThumbnail);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MD360Activity.class);
        context.startActivity(intent);
    }

    private void setLogo() {
        MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                .size(4f,4f)
                .provider(this, R.drawable.icon_laucher)
                .title("logo")
                .position(logoPosition);
        MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
        mVRLibrary.addPlugin(hotspot);
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        MDDirectorCamUpdate cameraUpdate = mVRLibrary.updateCamera();
        PropertyValuesHolder roll = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.getPitch(), 360f - cameraUpdate.getYaw());
        animator = ValueAnimator.ofPropertyValuesHolder(roll).setDuration(4000);

        animator.addUpdateListener(animation -> {
            float rollValue = (float) animation.getAnimatedValue("pitch");
            cameraUpdate.setPitch(rollValue);
        });

        animator.start();
    }

    private class ImageLoadProvider implements MDVRLibrary.IImageLoadProvider{
        @Override
        public void onProvideBitmap(final Uri uri, final MD360BitmapTexture.Callback callback) {
            Target<Bitmap> target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    mVRLibrary.onTextureResize(bitmap.getWidth(), bitmap.getHeight());
                    callback.texture(bitmap);
                }
            };
            GlideApp.with(MD360Activity.this).asBitmap()
                    .load(uri)
                    .centerInside()
                    .into(target);
        }
    }

    private void capturePicture(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        glSurfaceView.queueEvent(() -> {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
            Bitmap bitmap = createBitmapFromGLSurface(0, 0, glSurfaceView.getWidth(), glSurfaceView.getHeight(), gl);

            runOnUiThread(() -> {
                bitmapReadyCallbacks.onBitmapReady(bitmap);
            });
        });
    }

    private Bitmap cutPicture(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int cropWidth = w >= h ? h : w;

        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - cropWidth) / 2,
                (bitmap.getHeight() - cropWidth) / 2, cropWidth, cropWidth);
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }
}
