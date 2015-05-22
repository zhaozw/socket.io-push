package com.yy.misaka.demo.appmodel;

import android.content.Context;
import com.google.gson.reflect.TypeToken;
import com.yy.androidlib.util.logging.Logger;
import com.yy.androidlib.util.notification.NotificationCenter;
import com.yy.androidlib.websocket.Destination;
import com.yy.androidlib.websocket.ReplyHandler;
import com.yy.androidlib.websocket.StompClient;
import com.yy.androidlib.websocket.login.Login;
import com.yy.misaka.demo.Message;
import com.yy.misaka.demo.User;
import com.yy.misaka.demo.appmodel.callback.ImCallback;
import com.yy.misaka.demo.appmodel.callback.ImCallback.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: lxl
 * Date: 3/4/15
 * Time: 5:12 PM
 */
public class ImModel {
    private static final String TAG = "IM";
    private final StompClient stomp;
    private final Context context;
    private final Login login;
    private List<User> users = new ArrayList<User>();
    private Map<String, List<Message>> messages = new HashMap<String, List<Message>>();
    private ReplyHandler<List<User>> roomUserCallback = new ReplyHandler<List<User>>(new TypeToken<List<User>>() {
    }.getType()) {

        @Override
        public void onSuccess(List<User> result) {
            Logger.info(this, "onUserList, size: %d", result == null ? 0 : result.size());
            Iterator<User> it = result.iterator();
            while (it.hasNext()) {
                User user = it.next();
                if (user.getUid().equals(AppModel.INSTANCE.getLogin().getCurrentUid())) {
                    it.remove();
                }
            }
            if (result != null) {
                users = result;
                NotificationCenter.INSTANCE.getObserver(Room.class).onUserList();
            }
        }

        @Override
        public void onError(int code, String message) {
            Logger.error(this, "get userList failed, code: %d, message: %s", code, message);
        }
    };

    public ImModel(Context context, StompClient stomp, Login login) {
        this.context = context;
        this.stomp = stomp;
        this.login = login;
    }

    public void onConnected() {
        stomp.subscribeBroadcast("demo-server/userList", roomUserCallback);
        stomp.subscribeUserPush("demo-server/message", new StompClient.SubscribeHandler<Message>(Message.class) {
            @Override
            public void onSuccess(Message result) {
                saveBuddyMessage(result.getFromUid(), result);
                NotificationCenter.INSTANCE.getObserver(ImCallback.Message.class).onMessageReceived(result);
            }
        });
    }

    public void sendMessage(final String toUid, String text) {
        final Message message = new Message();
        message.setFromUid(login.getCurrentUid());
        message.setToUid(toUid);
        message.setMessage(text);
        stomp.request("demo-server", "/sendMessage", message, new ReplyHandler<Message>(Message.class) {

            @Override
            public void onSuccess(Message result) {
                saveBuddyMessage(message.getToUid(), message);
                NotificationCenter.INSTANCE.getObserver(ImCallback.Message.class).onMessageSendSuccess(message);
            }

            @Override
            public void onError(int code, String message) {
                NotificationCenter.INSTANCE.getObserver(ImCallback.Message.class).onMessageSendFailure(code, message);
            }
        });
    }

    private void saveBuddyMessage(String buddyUid, Message message) {
        List<Message> buddyMessages = getBuddyMessages(buddyUid);
        buddyMessages.add(message);
    }

    public int getBuddyMessageCount(String buddyUid) {
        return getBuddyMessages(buddyUid).size();
    }

    public List<Message> getBuddyMessages(String buddyUid) {
        List<Message> buddyMessages = messages.get(buddyUid);
        if (buddyMessages == null) {
            buddyMessages = new ArrayList<Message>();
            messages.put(buddyUid, buddyMessages);
        }
        return buddyMessages;
    }

    public void enterRoom() {
        stomp.request("demo-server", "/enterRoom", null, roomUserCallback);
    }

    public List<User> userList() {
        return users;
    }
}
