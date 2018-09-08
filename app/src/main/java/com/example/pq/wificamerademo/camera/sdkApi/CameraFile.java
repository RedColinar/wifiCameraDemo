package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.example.pq.wificamerademo.util.FileUtils;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;

import java.util.List;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 18:07
 * @description
 */
public class CameraFile {
    private ICatchWificamPlayback cameraPlayback;

    private CameraFile() {
        init();
    }

    private static class CameraFileHolder {
        private static final CameraFile sInstance = new CameraFile();
    }

    public static CameraFile getInstance() {
        return CameraFileHolder.sInstance;
    }

    public void init() {
        cameraPlayback = MyApplication.getMyCamera().getCameraPlayback();
    }

    public List<ICatchFile> getFileList(ICatchFileType type) {
        return ExceptionHelper.invoke(() -> cameraPlayback.listFiles(type,20));
    }

    public boolean deleteFile(ICatchFile file) {
        return ExceptionHelper.invokeBool(() -> cameraPlayback.deleteFile(file));
    }

    public boolean downloadFileQuick(ICatchFile file, String path){
        return ExceptionHelper.invokeBool(() -> cameraPlayback.downloadFileQuick(file, path));
    }

    public boolean downloadFile(ICatchFile file, String path) {
        String filePath = FileUtils.createUniqueFilename(path);
        return ExceptionHelper.invokeBool(() -> cameraPlayback.downloadFile(file, filePath));
    }

    public boolean downloadFile(String src, String des) {
        String filePath = FileUtils.createUniqueFilename(des);
        return ExceptionHelper.invokeBool(() -> cameraPlayback.downloadFile(src, filePath));
    }

    public ICatchFrameBuffer downloadFile(ICatchFile curFile) {
        return ExceptionHelper.invoke(() -> cameraPlayback.downloadFile(curFile));
    }

    public ICatchFrameBuffer getQuickview(ICatchFile curFile) {
        return ExceptionHelper.invoke(() -> cameraPlayback.getQuickview(curFile));
    }

    public ICatchFrameBuffer getThumbnail(ICatchFile file) {
        return ExceptionHelper.invoke(() -> cameraPlayback.getThumbnail(file));
    }

    public ICatchFrameBuffer getThumbnail(String filePath) {
        ICatchFile icatchFile = new ICatchFile(33, ICatchFileType.ICH_TYPE_VIDEO, filePath,"",0);
        return ExceptionHelper.invoke(() -> cameraPlayback.getThumbnail(icatchFile));
    }

    public boolean openFileTransChannel() {
        return ExceptionHelper.invokeBool(cameraPlayback::openFileTransChannel);
    }

    public boolean closeFileTransChannel() {
        return ExceptionHelper.invokeBool(cameraPlayback::closeFileTransChannel);
    }

    public boolean uploadFile(String localPath, String remotePath) {
        return ExceptionHelper.invokeBool(() -> cameraPlayback.uploadFile(localPath, remotePath));
    }
}
