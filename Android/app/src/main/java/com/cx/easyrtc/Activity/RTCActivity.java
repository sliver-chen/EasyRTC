package com.cx.easyrtc.Activity;

import android.content.Intent;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cx.easyrtc.Agent.Agent;
import com.cx.easyrtc.EasyRTCApplication;
import com.cx.easyrtc.R;
import com.cx.easyrtc.Socket.SocketWraper;
import com.cx.easyrtc.WebRTC.WebRTCWraper;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.lang.annotation.Target;
import java.net.Socket;
import java.util.ArrayList;

public class RTCActivity extends AppCompatActivity implements SocketWraper.SocketDelegate, WebRTCWraper.RtcListener{

    private final String TAG = RTCActivity.class.getName();

    private String mCallStatus;

    private boolean mIfNeedAddStream;

    private WebRTCWraper mRtcWraper;

    private VideoRenderer.Callbacks mRtcLocalRender;

    private VideoRenderer.Callbacks mRtcRemoteRender;

    private GLSurfaceView mGLView;

    private ImageButton mCancenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);

        Log.e("skruazzz", "RTCActivity onCreate");
        setUI();
        getCallStatus();
        getType();
        setGLView();
        setVideoRender();
    }

    @Override
    protected void onStop() {
        Log.e("skruazzz", "RTCActivity onStop");
        SocketWraper.shareContext().removeListener(this);

        super.onStop();
    }

    private void setUI() {
        setButton();
    }

    private void setButton() {
        mCancenButton = findViewById(R.id.CancelButton);
        mCancenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("sliver", "cancel button clicked");
                try {
                    SocketWraper.shareContext().emit("exit", "yes");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mRtcWraper.exitSession();
                Intent intent = new Intent(RTCActivity.this, CallActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getCallStatus() {
        mCallStatus = getIntent().getExtras().getString("status");
    }

    private void getType() {
        String remoteType = getIntent().getExtras().getString("type");
        if (remoteType.equals("camera")) {
            mIfNeedAddStream = false;
        } else {
            mIfNeedAddStream = true;
        }
    }

    private void setGLView() {
        mGLView = findViewById(R.id.glview);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setKeepScreenOn(true);
        VideoRendererGui.setView(mGLView, new Runnable() {
            @Override
            public void run() {
                setRtcWraper();
            }
        });
    }

    private void setVideoRender() {
        mRtcLocalRender = VideoRendererGui.create(0, 0, 100, 100,
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
        mRtcRemoteRender = VideoRendererGui.create(0, 0, 100,100,
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
    }

    private void setSocketWraper() {
        SocketWraper.shareContext().addListener(this);
    }

    private void setRtcWraper() {
        Log.e("sliver", "RTCActivity setRtcWraper");
        mRtcWraper = new WebRTCWraper(this, VideoRendererGui.getEGLContext(), mIfNeedAddStream);
        setSocketWraper();
        createOfferOrAck();
    }

    private void createOfferOrAck() {
        if (mCallStatus.equals("send")) {
            mRtcWraper.createOffer();
        } else if (mCallStatus.equals("recv")) {
            SocketWraper.shareContext().ack(true);
        }
    }

    private void processSignalMsg(String source, String target, String type, String value) {
        Log.e("sliver", "RTCActivity processSignalMsg " + type);
        if (target.equals(SocketWraper.shareContext().getUid())) {
            if (type.equals("offer")) {
                Log.e("sliver", "RTCActivity receive offer " + mRtcWraper);
                mRtcWraper.setRemoteSdp(type, value);
                mRtcWraper.createAnswer();
            }

            if (type.equals("answer")) {
                mRtcWraper.setRemoteSdp(type, value);
            }

            if (type.equals("exit")) {
                mRtcWraper.exitSession();
                Intent intent = new Intent(RTCActivity.this, CallActivity.class);
                startActivity(intent);
            }
        } else {
            Log.e("sliver", "RTCActivity get error tag");
        }
    }

    @Override
    public void onUserAgentsUpdate(ArrayList<Agent> agents) {

    }

    @Override
    public void onDisConnect() {
        Log.e("sliver", "RTCActivity onDisConnect");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EasyRTCApplication.getContext(), "can't connect to server", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRemoteEventMsg(String source, String target, String type, String value) {
        processSignalMsg(source, target, type, value);
    }

    @Override
    public void onRemoteCandidate(int label, String mid, String candidate) {
        Log.e("sliver", "RTCActivity onRemoteCandidate");
        mRtcWraper.setCandidate(label, mid, candidate);
    }

    @Override
    public void onLocalStream(MediaStream mediaStream) {
        Log.e("sliver", "RTCActivity onLocalStream");
        mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mRtcLocalRender));
        VideoRendererGui.update(mRtcLocalRender, 75,75, 25, 25,
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
    }

    @Override
    public void onAddRemoteStream(MediaStream mediaStream) {
        Log.e("sliver", "RTCActivity onAddRemoteStream");
        mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(mRtcRemoteRender));
        VideoRendererGui.update(mRtcRemoteRender, 0, 0, 75, 75,
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
        if (mIfNeedAddStream) {
            VideoRendererGui.update(mRtcLocalRender, 75, 75, 25, 25,
                    VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
        }
    }

    @Override
    public void onRemoveRemoteStream(MediaStream mediaStream) {
        Log.e("sliver", "RTCActivity onRemoveRemoteStream");
        VideoRendererGui.update(mRtcLocalRender, 75, 75, 25, 25,
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);
    }

    @Override
    public void onCreateOfferOrAnswer(String type, String sdp) {
        Log.e("sliver", "RTCActivity onCreateOfferOrAnswer");
        try {
            SocketWraper.shareContext().emit(type, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidate(int label, String id, String candidate) {
        Log.e("sliver", "RTCActivity onIceCandidate");
        try {
            JSONObject msg = new JSONObject();
            msg.put("source", SocketWraper.shareContext().getUid());
            msg.put("target", SocketWraper.shareContext().getTarget());
            msg.put("label", label);
            msg.put("mid", id);
            msg.put("candidate", candidate);

            SocketWraper.shareContext().emit("candidate", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
