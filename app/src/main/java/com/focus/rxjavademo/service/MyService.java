package com.focus.rxjavademo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by focus on 2017/10/17.
 */

public class MyService extends Service {

    private String TAG = "my_log";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MyService onCreate thread id is : " + Thread.currentThread().getId());
        Log.d(TAG, "call myservice onCreate !");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "call myservice onStartCommand !");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "call myservice onBind !");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "call myservice onUnbind !");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "call myservice onDestroy !");
        super.onDestroy();
    }

}
