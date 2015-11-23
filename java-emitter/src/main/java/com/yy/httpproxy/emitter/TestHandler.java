package com.yy.httpproxy.emitter;

import java.util.Map;

/**
 * Created by xuduo on 11/23/15.
 *
 */
public class TestHandler extends PacketHandler {

    @Override
    void handle(String sequenceId, String path, Map<String, String> headers, byte[] body) {
        broadcast(path, body);
    }

}
