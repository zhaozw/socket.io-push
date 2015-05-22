package com.yy.misaka.demo;

import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String nick;
    private String portrait;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + nick + '\'' +
                ", password='" + portrait + '\'' +
                '}';
    }
}

