package com.yy.misaka.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.yy.androidlib.util.sdk.BaseAdapter;
import com.yy.misaka.demo.appmodel.AppModel;
import com.yy.misaka.demo.appmodel.callback.ImCallback;
import com.yy.misaka.demo.appmodel.callback.ImCallback.Room;
import com.yy.misaka.demo.util.Image;


public class ImUserListActivity extends BaseActivity implements Room, ImCallback.Message {

    private UserAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_user_list);
        ListView userListView = (ListView) findViewById(R.id.lv_users);
        userListAdapter = new UserAdapter();
        userListView.setAdapter(userListAdapter);
        userListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) parent.getAdapter().getItem(position);
                if (!user.getUid().equals(AppModel.INSTANCE.getLogin().getCurrentUid())) {
                    ChatActivity.navigateFrom(ImUserListActivity.this, user);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppModel.INSTANCE.getIm().enterRoom();
    }

    @Override
    public void onUserList() {
        updateUserList();
    }

    private void updateUserList() {
        userListAdapter.setItems(AppModel.INSTANCE.getIm().userList());
    }

    @Override
    public void onMessageSendSuccess(Message message) {
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageSendFailure(int code, String errorMessage) {
        // do nothing
    }

    @Override
    public void onMessageReceived(Message message) {
        userListAdapter.notifyDataSetChanged();
    }

    static class UserAdapter extends BaseAdapter<User> {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.nicknameTextView = (TextView) convertView.findViewById(R.id.tv_nickname);
                viewHolder.portraitImageView = (ImageView) convertView.findViewById(R.id.iv_portrait);
                viewHolder.messageCountTextView = (TextView) convertView.findViewById(R.id.tv_message_count);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            User user = getItem(position);
            viewHolder.nicknameTextView.setText(user.getNick());
            int messageCount = AppModel.INSTANCE.getIm().getBuddyMessageCount(user.getUid());
            viewHolder.messageCountTextView.setText(messageCount > 0 ? String.valueOf(messageCount) : "");
            Image.displayPortrait(user.getPortrait(), viewHolder.portraitImageView);
            return convertView;
        }

        static class ViewHolder {
            ImageView portraitImageView;
            TextView nicknameTextView;
            TextView messageCountTextView;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Profile").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
        if (item.getItemId() == 0) {
            Intent intent = new Intent(ImUserListActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
