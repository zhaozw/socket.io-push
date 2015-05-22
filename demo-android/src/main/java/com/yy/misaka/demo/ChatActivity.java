package com.yy.misaka.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.yy.androidlib.util.logging.Logger;
import com.yy.androidlib.util.sdk.BaseAdapter;
import com.yy.misaka.demo.appmodel.AppModel;
import com.yy.misaka.demo.appmodel.callback.ImCallback;
import com.yy.misaka.demo.appmodel.callback.ProfileCallback.Info;
import com.yy.misaka.demo.util.Image;


public class ChatActivity extends BaseActivity implements ImCallback.Message, Info {

    private static final String TAG = "ChatActivity";
    private static final String EXTRA_USER = "USER";
    private MessageAdapter adapter;
    private User buddy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        buddy = (User) getIntent().getSerializableExtra(EXTRA_USER);
        setTitle(buddy.getNick());

        ListView messageListView = (ListView) findViewById(R.id.lv_messages);
        adapter = new MessageAdapter(buddy);
        adapter.updateMyInfo(AppModel.INSTANCE.getProfile().getMyInfo());
        adapter.setItems(AppModel.INSTANCE.getIm().getBuddyMessages(buddy.getUid()));
        messageListView.setAdapter(adapter);

        final EditText inputEdit = (EditText) findViewById(R.id.et_input);
        findViewById(R.id.btn_send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(inputEdit.getText().toString());
                inputEdit.setText("");
            }
        });
    }

    private void sendMessage(String text) {
        AppModel.INSTANCE.getIm().sendMessage(buddy.getUid(), text);
    }

    public static void navigateFrom(Activity activity, User user) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(EXTRA_USER, user);
        activity.startActivity(intent);
    }

    @Override
    public void onMessageSendSuccess(Message message) {
        Logger.info(TAG, "message sent successfully");
        adapter.addItem(message);
    }

    @Override
    public void onMessageSendFailure(int code, String errorMessage) {
        Toast toast = Toast.makeText(this, "request error!code:" + code + " ,message:" + errorMessage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onMessageReceived(Message message) {
        if (buddy.getUid().equals(message.getFromUid())) {
            adapter.addItem(message);
        }
    }

    @Override
    public void onUser(User user) {
        adapter.updateMyInfo(AppModel.INSTANCE.getProfile().getMyInfo());
    }

    static class MessageAdapter extends BaseAdapter<Message> {

        private final User buddy;
        private User me;

        public MessageAdapter(User buddy) {
            this.buddy = buddy;
        }

        public void updateMyInfo(User me) {
            this.me = me;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.portraitImageView = (ImageView) convertView.findViewById(R.id.iv_portrait);
                viewHolder.nicknameTextView = (TextView) convertView.findViewById(R.id.tv_nickname);
                viewHolder.messageTextView = (TextView) convertView.findViewById(R.id.tv_message);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            Message message = getItem(position);
            User sender = getSender(message);
            Image.displayPortrait(sender.getPortrait(), viewHolder.portraitImageView);
            if (sender.getNick() != null) {
                viewHolder.nicknameTextView.setText(sender.getNick() + ":");
            }
            viewHolder.messageTextView.setText(message.getMessage());
            return convertView;
        }

        private User getSender(Message message) {
            if (AppModel.INSTANCE.getLogin().getCurrentUid().equals(message.getFromUid())) {
                return AppModel.INSTANCE.getProfile().getMyInfo();
            } else {
                return message.getFromUser();
            }
        }

        public void addItem(Message message) {
            items.add(message);
            notifyDataSetChanged();
        }

        static class ViewHolder {
            ImageView portraitImageView;
            TextView nicknameTextView;
            TextView messageTextView;
        }
    }
}
