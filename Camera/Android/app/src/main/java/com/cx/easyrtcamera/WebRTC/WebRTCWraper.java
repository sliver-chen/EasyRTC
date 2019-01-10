package com.cx.easyrtcamera.WebRTC;

import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;

import java.util.LinkedList;

public class WebRTCWraper implements SdpObserver, PeerConnection.Observer {
    private PeerConnection mPeer;

    private PeerConnectionFactory mPeerFactory;

    private MediaStream mLocalMedia;

    private VideoSource mVideoSource;

    private RtcListener mListener;

    private LinkedList<PeerConnection.IceServer> mIceServers = new LinkedList<>();

    private MediaConstraints mMediaConstraints = new MediaConstraints();

    public interface RtcListener {

        void onLocalStream(MediaStream mediaStream);

        void onAddRemoteStream(MediaStream mediaStream);

        void onRemoveRemoteStream(MediaStream mediaStream);

        void onCreateOfferOrAnswer(String type, String sdp);

        void onIceCandidate(int label, String id, String candidate);
    }

    public WebRTCWraper(RtcListener listener) {
        mListener = listener;
        mPeerFactory = new PeerConnectionFactory();
        mIceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        mIceServers.add(new PeerConnection.IceServer("turn:129.28.101.171:3478","cx","cx1234"));
        mIceServers.add(new PeerConnection.IceServer("turn:39.105.125.160:3478", "helloword", "helloword"));
        mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        createLocalMedia();
        createPeerConnection();
    }

    public void createOffer() {
        Log.e("sliver", "WebRTCWraper createOffer");
        mPeer.createOffer(this, mMediaConstraints);
    }

    public void createAnswer() {
        Log.e("sliver", "WebRTCWraper createAnswer");
        mPeer.createAnswer(this, mMediaConstraints);
    }

    public void setRemoteSdp(String type, String sdp) {
        Log.e("sliver", "WebRTCWraper setRemoteSdp");
        SessionDescription sessionDescription =
                new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);
        mPeer.setRemoteDescription(this, sessionDescription);
    }

    public void setCandidate(int label, String mid, String candidate) {
        Log.e("sliver", "WebRTCWraper setRemoteCandidate");
        if (mPeer.getRemoteDescription() != null) {
            IceCandidate iceCandidate = new IceCandidate(mid, label, candidate);
            mPeer.addIceCandidate(iceCandidate);
        } else {
            Log.e("sliver", "WebRTCWraper remote sdp is null when set candidate");
        }
    }

    public void exitSession() {
        mPeer.close();
        mPeer.dispose();
        if (mVideoSource != null) {
            mVideoSource.dispose();
        }
        mPeerFactory.dispose();
    }

    private void createPeerConnection() {
        mPeer = mPeerFactory.createPeerConnection(mIceServers, mMediaConstraints, this);
        mPeer.addStream(mLocalMedia);
    }

    private void createLocalMedia() {
        mLocalMedia = mPeerFactory.createLocalMediaStream("ARDAMS");
        MediaConstraints videoConstraints = new MediaConstraints();

        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(1280)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(720)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(25)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(25)));

        mVideoSource = mPeerFactory.createVideoSource(getVideoCaptureer(), videoConstraints);
        mLocalMedia.addTrack(mPeerFactory.createVideoTrack("ARDAMSv0", mVideoSource));

        AudioSource audioSource = mPeerFactory.createAudioSource(new MediaConstraints());
        mLocalMedia.addTrack(mPeerFactory.createAudioTrack("ARDAMSa0", audioSource));

        mListener.onLocalStream(mLocalMedia);
    }

    private VideoCapturer getVideoCaptureer() {
        String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

    //创建local sdp成功
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.e("sliver", "WebRTCWraper onCreateSuccess type:" + sessionDescription.type.canonicalForm());
        mPeer.setLocalDescription(this, sessionDescription);
        mListener.onCreateOfferOrAnswer(sessionDescription.type.canonicalForm(), sessionDescription.description);
    }

    //设置remote sdp成功
    @Override
    public void onSetSuccess() {
        Log.e("sliver", "WebRTCWraper onSetSuccess");
    }

    //创建local sdp成功
    @Override
    public void onCreateFailure(String s) {
        Log.e("sliver", "WebRTCWraper onCreateFailure error : " + s);
    }

    //设置remote sdp失败
    @Override
    public void onSetFailure(String s) {
        Log.e("sliver", "WebRTCWraper onSetFailure error : " + s);
    }

    //信令状态变化
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.e("sliver", "WebRTCWraper onSignalingChange state : " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.e("sliver", "WebRTCWraper onIceConnectionChange state : " + iceConnectionState);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.e("sliver", "WebRTCWraper onIceGatheringChange state : " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.e("sliver", "WebRTCWraper onIceCandidate");
        mListener.onIceCandidate(iceCandidate.sdpMLineIndex, iceCandidate.sdpMid, iceCandidate.sdp);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.e("sliver", "WebRTCWraper onAddStream");
        mListener.onAddRemoteStream(mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.e("sliver", "WebRTCWraper onRemoveStream");
        mListener.onRemoveRemoteStream(mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.e("sliver", "WebRTCWraper onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.e("sliver", "WebRTCWraper onRenegotiationNeeded");
    }
}
