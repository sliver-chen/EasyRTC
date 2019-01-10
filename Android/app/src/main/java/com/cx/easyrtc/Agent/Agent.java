package com.cx.easyrtc.Agent;

/**
 * Created by cx on 2018/12/21.
 */

public class Agent {

    private String mID;

    private String mName;

    private String mType;

    public Agent(String id, String name, String type) {
        mID = id;
        mName = name;
        mType = type;
    }

    public String id() {
            return mID;
        }

    public String name() {
        return mName;
    }

    public String type() {
        return mType;
    }

}