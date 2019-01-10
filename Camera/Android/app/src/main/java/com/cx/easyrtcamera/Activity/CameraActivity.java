package com.cx.easyrtcamera.Activity;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cx.easyrtcamera.Camera.CameraConnection;
import com.cx.easyrtcamera.EasyRTCameraApplication;
import com.cx.easyrtcamera.R;
import com.cx.easyrtcamera.Socket.SocketWraper;

import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRendererGui;

import java.util.HashMap;

public class CameraActivity extends AppCompatActivity implements SocketWraper.SocketDelegate {

    private GLSurfaceView mGLView;

    private HashMap<String,CameraConnection> mConnectionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        SocketWraper.shareContext().addListener(this);
        setRTCEnvironment();
    }

    @Override
    protected void onDestroy() {
        SocketWraper.shareContext().removeListener(this);
        super.onDestroy();
    }

    private void setRTCEnvironment() {
        //mGLView = new GLSurfaceView(EasyRTCameraApplication.getGlobalContext());
        mGLView = findViewById(R.id.glview);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setKeepScreenOn(true);
        VideoRendererGui.setView(mGLView, new Runnable() {
            @Override
            public void run() {
                boolean isInited = PeerConnectionFactory.initializeAndroidGlobals(EasyRTCameraApplication.getGlobalContext(), true,
                        true, true, VideoRendererGui.getEGLContext());
                Log.e("sliver", "initializeAndroidGlobals " + isInited);
            }
        });
    }

    private void processEventMsg(String source, String target, String type, String value) {
        if (type.equals("invite")) {
            if (!mConnectionMap.containsKey(source)) {
                CameraConnection cameraConnection = new CameraConnection(source);
                mConnectionMap.put(source, cameraConnection);
                SocketWraper.shareContext().ack(source,true);
            }
        }

        if (type.equals("offer")) {
            if (mConnectionMap.containsKey(source)) {
                CameraConnection cameraConnection = mConnectionMap.get(source);
                cameraConnection.setRemoteSdp(type, value);
                cameraConnection.createAnswer();
            }
        }

        if (type.equals("exit")) {
            if (mConnectionMap.containsKey(source)) {
                mConnectionMap.remove(source);
            }
        }
    }

    private void processCandidate(String source, String target,
                                  int label, String mid, String candidate) {
        if (mConnectionMap.containsKey(source)) {
            CameraConnection cameraConnection = mConnectionMap.get(source);
            cameraConnection.setRemoteCandidate(label, mid, candidate);
        }
    }

    /*
     * SocketWraper.Delegate
     */
    @Override
    public void onDisConnect() {
        Toast.makeText(EasyRTCameraApplication.getGlobalContext(),
                "lost connect to EasyRTC Server", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRemoteEventMsg(String source, String target, String type, String value) {
        processEventMsg(source, target, type, value);
    }

    @Override
    public void onRemoteCandidate(String source, String target,
                                  int label, String mid, String candidate) {
        processCandidate(source, target, label, mid, candidate);
    }
}
