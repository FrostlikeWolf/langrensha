package com.wzc.lrsdemo.event;

import android.os.Message;

import com.wzc.lrsdemo.role.Role;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GameStateMessage {
    //handle消息类型，int
    public static final int ROLE_DEPLOY = 0xad0;
    public static final int ZHOUYE = 0xad1;
    public static final int CHAT = 0xad2;
    public static final int GAME_START = 0xad3;
    public static final int GAME_OVER = 0xad4;
    public static final int GAME_SPEAK = 0xad5;
    public static final int GAME_DIE = 0xad6;
    public static final int COUNTDOWNTIMER = 0xad7;
    public static final int STAGE_CHANGE = 0xad8;

    private Role role;
    //直接过去role对象，role对象中包含number
    private String text;
    private long time = 15 * 1000;

    public GameStateMessage(Role role, String text) {
        this.role = role;
        this.text = text;
    }

    public GameStateMessage(Role role, String text, long time) {
        this.role = role;
        this.text = text;
        this.time = time;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
