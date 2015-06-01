package com.yy.androidlib.websocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class HttpTests {

    private ReplyHandler replyHandler;
    private List<StompClient> stomps = new ArrayList<StompClient>();

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        //you other setup here
    }

    @Test
    public void testWebSocket() throws Exception {
        for (int i = 0; i < 10; i++) {
            final StompClient stomp = new StompClient(this, "http://172.19.206.96:8090/stomp/608/sovu732o/websocket", new Config().mode(Config.Mode.REMOTE));
            stomp.connect();
            stomps.add(stomp);
            Robolectric.runUiThreadTasks();
            Thread.sleep(100l);
        }
        while (true) {
            Robolectric.runUiThreadTasks();
            Thread.sleep(100l);
        }
        Robolectric.runUiThreadTasksIncludingDelayedTasks();


//        Patch patch = new Patch();
//        patch.setAppId("test");
//        stomp.request("/appList", null, replyHandler);
//        User user = new User();
//        user.setPhone("18680268780");
//        user.setPassword("qwe123456");
//        stomp.request("/login", user, null);
//
//        Thread.sleep(5000l);
//
//        for (int i = 0; i < 5; i++) {
//            for (StompJsonClient stomp : stomps) {
//                stomp.request("/appList", null, null);
//            }
//
//            Thread.sleep(5000l);
//
//        }

    }
}