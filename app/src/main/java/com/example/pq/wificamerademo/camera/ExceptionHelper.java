package com.example.pq.wificamerademo.camera;

import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchCaptureImageException;
import com.icatch.wificam.customer.exception.IchDeviceException;
import com.icatch.wificam.customer.exception.IchDevicePropException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidPasswdException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchNoSDCardException;
import com.icatch.wificam.customer.exception.IchNoSuchFileException;
import com.icatch.wificam.customer.exception.IchNoSuchPathException;
import com.icatch.wificam.customer.exception.IchPbStreamPausedException;
import com.icatch.wificam.customer.exception.IchPtpInitFailedException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchStreamNotSupportException;
import com.icatch.wificam.customer.exception.IchTryAgainException;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 17:16
 * @description 异常捕获简写
 */
public class ExceptionHelper {

    public static <T> T invoke(Callable<T> callable) {
        return invokeWithExceptionHandler(callable, null);
    }

    public static Boolean invokeBool(Callable<Boolean> callable) {
        Boolean result =  invokeWithExceptionHandler(callable, null);
        return result == null ? false : result;
    }

    public static Integer invokeInt(Callable<Integer> callable, int defaultValue) {
        Integer result = invokeWithExceptionHandler(callable, null);
        return result == null ? defaultValue : result;
    }

    public static <T> T invokeWithExceptionHandler(Callable<T> callable, Consumer consumer) {
        try {
            return callable.call();
        } catch (IchSocketException
                | IchStreamNotRunningException
                | IchNoSuchFileException
                | IchNoSuchPathException
                | IchBufferTooSmallException
                | IchCameraModeException
                | IchCaptureImageException
                | IchInvalidSessionException
                | IchDeviceException
                | IchDevicePropException
                | IchInvalidPasswdException
                | IchPtpInitFailedException
                | IchStreamNotSupportException
                | IchInvalidArgumentException
                | IchNoSDCardException
                | IchTryAgainException
                | IchPbStreamPausedException
                | IchAudioStreamClosedException
                e) {
            e.printStackTrace();
            if (consumer != null) {
                consumer.accept(e);
            }
        }
        return null;
    }

    public interface Callable<V> {
        V call() throws IchSocketException,
                IchStreamNotRunningException,
                IchNoSuchFileException,
                IchNoSuchPathException,
                IchBufferTooSmallException,
                IchCameraModeException,
                IchCaptureImageException,
                IchInvalidSessionException,
                IchDeviceException,
                IchDevicePropException,
                IchInvalidPasswdException,
                IchPtpInitFailedException,
                IchStreamNotSupportException,
                IchInvalidArgumentException,
                IchNoSDCardException,
                IchTryAgainException,
                IchPbStreamPausedException,
                IchAudioStreamClosedException;
    }

    public interface Consumer {
        void accept(Throwable t);
    }
}
