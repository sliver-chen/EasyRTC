package com.cx.easyrtcamera;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cx.easyrtcamera.Socket.SocketWraper;

import java.util.Timer;
import java.util.TimerTask;

public class EasyRTCameraApplication extends Application {

    private static Context mGlobalContext;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("sliver", "EasyRTCameraApplication onCreate");

        mGlobalContext = getApplicationContext();

        SocketWraper.shareContext().connect("http://129.28.101.171:1234");
        setSocketKeepAlive();
    }

    private void setSocketKeepAlive() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SocketWraper.shareContext().keepAlive();
            }
        }, 0, 2000);
    }

    public static Context getGlobalContext() {
        return mGlobalContext;
    }
}
