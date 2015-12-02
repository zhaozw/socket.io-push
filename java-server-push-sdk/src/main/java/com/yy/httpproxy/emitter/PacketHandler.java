package com.yy.httpproxy.emitter;

import java.util.Map;

/**
 * Created by xuduo on 11/23/15.
 */
public abstract class PacketHandler<T> {

    private Emitter emitter;
    private Object clazz;
    private Serializer serializer;
    public static final String DISCONNECT = "/socketDisconnect";

    public PacketHandler(Object clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    public PacketHandler() {

    }

    abstract void handle(String pushId, String sequenceId, String path, T body);

    private void broadcastInternal(String topic, byte[] data) {
        emitter.push(topic, data);
    }

    public void broadcast(String path, T object) {
        byte[] data = new byte[0];
        try {
            data = serializer.toBinary(path, object);
            broadcastInternal(path, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replyInternal(String sequenceId, String pushId, String path, byte[] data) {
        emitter.reply(sequenceId, pushId, data);
    }

    public void reply(String sequenceId, String pushId, String path, T object) {
        byte[] data = new byte[0];
        try {
            data = serializer.toBinary(path, object);
            replyInternal(sequenceId, pushId, path, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    public void handleBinary(String pushId, String sequenceId, String path, byte[] body) {
        T object = null;
        if (serializer == null || clazz == null) {
            handle(pushId, sequenceId, path, null);
        } else {
            try {
                object = (T) serializer.toObject(path, clazz, body);
                handle(pushId, sequenceId, path, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
