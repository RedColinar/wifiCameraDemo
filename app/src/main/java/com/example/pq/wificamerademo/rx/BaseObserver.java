package com.example.pq.wificamerademo.rx;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 13:22
 * @description
 */
public class BaseObserver<T> implements Observer<T> {


    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
