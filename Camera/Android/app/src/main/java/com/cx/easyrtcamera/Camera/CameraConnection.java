package com.cx.easyrtcamera.Camera;

import android.util.Log;

import com.cx.easyrtcamera.Socket.SocketWraper;
import com.cx.easyrtcamera.WebRTC.WebRTCWraper;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;

public class CameraConnection implements WebRTCWraper.RtcListener{

    private String mConnectionID;

    private WebRTCWraper mRTCWraper;

    public CameraConnection(String id) {
        mConnectionID = id;
        mRTCWraper = new WebRTCWraper(this);
    }

    public void setRemoteSdp(String type, String sdp) {
        mRTCWraper.setRemoteSdp(type, sdp);
    }

    public void setRemoteCandidate(int label, String mid, String candidate) {
        mRTCWraper.setCandidate(label, mid, candidate);
    }

    public void createAnswer() {
        mRTCWraper.createAnswer();
    }

    /*
     * WebRTCWraper.RtcListener
     */
    @Override
    public void onLocalStream(MediaStream mediaStream) {
        Log.e("sliver", "CameraConnection " + mConnectionID + " onLocalStream");
    }

    @Override
    public void onAddRemoteStream(MediaStream mediaStream) {
        Log.e("sliver", "CameraConnection " + mConnectionID + " onAddRemoteStream");
    }

    @Override
    public void onRemoveRemoteStream(MediaStream mediaStream) {
        Log.e("sliver", "CameraConnection " + mConnectionID + " onRemoveRemoteStream");
    }

    @Override
    public void onCreateOfferOrAnswer(String type, String sdp) {
        Log.e("sliver", "CameraConnection " + mConnectionID + " create " + type);
        try {
            SocketWraper.shareContext().emit(mConnectionID, type, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidate(int label, String id, String candidate) {
        Log.e("sliver", "CameraConnection " + mConnectionID + " onIceCandidate ");
        try {
            JSONObject msg = new JSONObject();
            msg.put("source", SocketWraper.shareContext().getSource());
            msg.put("target", mConnectionID);
            msg.put("label", label);
            msg.put("mid", id);
            msg.put("candidate", candidate);

            SocketWraper.shareContext().emit("candidate", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
