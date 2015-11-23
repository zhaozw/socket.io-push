package com.yy.httpproxy.emitter;

import java.util.Map;

/**
 * Created by xuduo on 11/23/15.
 */
public abstract class PacketHandler {

    private Emitter emitter;

    abstract void handle(String sequenceId, String path, Map<String, String> headers, byte[] body);

    public void broadcast(String topic, byte[] data) {
        emitter.push(topic, data);
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }
}
