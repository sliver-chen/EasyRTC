package com.cx.easyrtc;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.cx.easyrtc.Socket.SocketWraper;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cx on 2018/12/22.
 */

public class EasyRTCApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        Log.e("sliver", "EasyRTCApplication SocketWrpaer setURL");
        super.onCreate();

        mContext = getApplicationContext();

        SocketWraper.shareContext().setURL("http://129.28.101.171:1234/");
        initSocketHeartBeat();
    }

    public static Context getContext() {
        return mContext;
    }

    private void initSocketHeartBeat() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SocketWraper.shareContext().emitHeartBeat();
            }
        }, 0, 2000);
    }
}
