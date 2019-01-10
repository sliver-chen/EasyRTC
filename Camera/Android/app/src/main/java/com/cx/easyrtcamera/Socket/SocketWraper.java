package com.cx.easyrtcamera.Socket;

import android.os.Build;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class SocketWraper {

    private Socket mSignaling;

    private MessageProcessor mMsgProcessor;

    private ArrayList<SocketDelegate> mListeners;

    private String uid;

    private static SocketWraper mShareContext;

    public interface SocketDelegate {
        public void onDisConnect();

        public void onRemoteEventMsg(String source, String target, String type, String value);

        public void onRemoteCandidate(String source, String target, int label, String mid, String candidate);
    }

    public static synchronized SocketWraper shareContext() {
        if (mShareContext == null) {
            mShareContext = new SocketWraper();
        }
        return mShareContext;
    }

    public SocketWraper() {
        mListeners = new ArrayList<SocketDelegate>();
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    public void connect(String host) {
        mMsgProcessor = new MessageProcessor();
        try {
            mSignaling = IO.socket(host);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        setEvent();
        mSignaling.connect();
        register();
    }

    public synchronized void addListener(SocketDelegate delegate) {
        mListeners.add(delegate);
    }

    public synchronized void removeListener(SocketDelegate delegate) {
        mListeners.remove(delegate);
    }

    private void destroy() {
        mSignaling.disconnect();
        mSignaling.close();
    }

    public void setSource(String source) {
        uid = source;
    }

    public String getSource() {
        return uid;
    }

    public void emit(String target, String type, String value) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("source", uid);
        msg.put("target", target);
        msg.put("type", type);
        msg.put("value", value);
        mSignaling.emit("event", msg);
    }

    public void emit(String event, JSONObject object) throws JSONException{
        mSignaling.emit(event, object);
    }

    public void keepAlive() {
        if (uid != null) {
            try {
                JSONObject msg = new JSONObject();
                msg.put("source", uid);
                msg.put("target", "EastRTC Server");
                msg.put("type", "heart");
                msg.put("value", "yes");
                mSignaling.emit("heart", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void register() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("name", Build.MODEL);
            msg.put("type", "Android_Camera");
            mSignaling.emit("get_id", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void invite(String target) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("source", uid);
            msg.put("target", target);
            msg.put("type", "invite");
            msg.put("value", "yes");

            mSignaling.emit("event", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void ack(String target, boolean accept) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("source", uid);
            msg.put("target", target);
            msg.put("type", "ack");
            if (accept) {
                msg.put("value", "yes");
            } else {
                msg.put("value", "no");
            }

            mSignaling.emit("event", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setEvent() {
        mSignaling.on("connect", mMsgProcessor.onConnect);
        mSignaling.on("event", mMsgProcessor.onEvent);
        mSignaling.on("candidate", mMsgProcessor.onCandidate);
        mSignaling.on("set_id", mMsgProcessor.onGet);
        mSignaling.on("disconnect", mMsgProcessor.onDisconnect);
    }

    private void verify(String type) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("source", uid);
            msg.put("target", "EasyRTC Server");
            msg.put("type", type);
            mSignaling.emit("ack", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class MessageProcessor {

        private Emitter.Listener onConnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("sliver", "SocketWraper SokcetWraper onConnect");
            }
        };

        private Emitter.Listener onEvent = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String type   = data.getString("type");
                    String source = data.getString("source");
                    String target = data.getString("target");
                    String value  = data.getString("value");

                    synchronized (SocketWraper.shareContext()) {
                        Log.e("sliver", "SocketWraper receive event type " + type);
                        for (SocketDelegate delegate : SocketWraper.shareContext().mListeners) {
                            delegate.onRemoteEventMsg(source, target, type, value);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                verify("event");
            }
        };

        private Emitter.Listener onCandidate = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("sliver", "SocketWraper receive remote candidate");
                JSONObject data = (JSONObject)args[0];
                try {
                    String source = data.getString("source");
                    String target = data.getString("target");
                    int label = data.getInt("label");
                    String mid = data.getString("mid");
                    String candidate = data.getString("candidate");

                    synchronized (SocketWraper.shareContext()) {
                        for (SocketDelegate delegate : SocketWraper.shareContext().mListeners) {
                            delegate.onRemoteCandidate(source, target, label, mid, candidate);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                verify("candidate");
            }
        };

        private Emitter.Listener onGet = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try {
                    String source = data.getString("source");
                    String target = data.getString("target");
                    String type   = data.getString("type");
                    String value  = data.getString("value");

                    uid = value;
                    Log.e("sliver", "SocketWraper get id : " + uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                verify("set_id");
            }
        };

        private Emitter.Listener onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("sliver", "onDisconnect");
                for (SocketDelegate delegate : SocketWraper.shareContext().mListeners) {
                    delegate.onDisConnect();
                }
            }
        };
    }

}
