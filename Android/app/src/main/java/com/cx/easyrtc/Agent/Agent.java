package com.cx.easyrtc.Agent;

/**
 * Created by cx on 2018/12/21.
 */

public class Agent {

    private String mID;

    private String mName;

    public Agent(String id, String name) {
        mID = id;
        mName = name;
    }

    public String id() {
            return mID;
        }

    public String name() {
        return mName;
    }

}