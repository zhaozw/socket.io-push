package com.yy.misaka.demo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.yy.misaka.demo.appmodel.AppModel;
import com.yy.misaka.demo.appmodel.callback.ProfileCallback;
import com.yy.misaka.demo.util.Image;
import com.yy.misaka.demo.util.YYImageUtils;


public class ProfileActivity extends BaseActivity implements ProfileCallback.Info, ProfileCallback.Modify, ProfileCallback.Portrait {


    private static final String TAG = "ProfileActivity";
    /**
     * 从服务器获取的头像文件url(有默认值)
     */
    private String url;
    /**
     * 头像ImageView
     */
    private ImageView portrait;
    /**
     * 昵称TextView
     */
    private TextView nick;
    /**
     * 修改昵称EditText
     */
    private EditText editNick;
    /**
     * 确定按钮Button
     */
    private Button btn_confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onUser(AppModel.INSTANCE.getProfile().getMyInfo());
    }

    /**
     * 初始化UI
     */
    private void initView() {
        nick = (TextView) findViewById(R.id.tv_nick);
        portrait = (ImageView) findViewById(R.id.iv_profile);
        editNick = (EditText) findViewById(R.id.edit_nick);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        /** 点击头像View,选择相册图片 */
        portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                album();
            }
        });
        /** 确认修改 */
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestModify();
            }
        });
    }

    /**
     * 发送修改请求
     */
    private void requestModify() {
        /** 请求修改昵称 */
        User user = new User();
        user.setNick(editNick.getText().toString());
        AppModel.INSTANCE.getProfile().modifyUser(user);
    }

    /**
     * 打开相册选择图片
     */
    private void album() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 0);
    }

    /**
     * 更新头像UI
     *
     * @param bmp 头像位图
     */
    private void upDateHead(Bitmap bmp) {
        portrait.setImageBitmap(bmp);
    }

    /**
     * 更新昵称UI
     *
     * @param name 昵称
     */
    private void upDateNick(String name) {
        nick.setText(name);
    }

    /**
     * Uri 转 本地Url
     *
     * @param uri 文件uri
     * @return uri对应本地url
     */
    private String Uri2Url(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cur = this.managedQuery(uri, proj, null, null, null);
        int index = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cur.moveToFirst();
        String url = cur.getString(index);
        return url;
    }

    @Override
    public void onNameModifySuccess(User user) {
        onUser(user);
        Toast.makeText(this, "modify success", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNameModifyFailure(int code, String errorMessage) {
        Toast.makeText(this, "request error!code:" + code + " ,message:" + errorMessage, Toast.LENGTH_LONG).show();
    }


    /**
     * 获取相册图片结果
     *
     * @param requestCode 请求码
     * @param resultCode
     * @param data        包含图片文件uri的Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 0) {
            Uri uri = data.getData();
            String path = Uri2Url(uri);
            String temp = getCacheDir() + "/upload.jpg";
            YYImageUtils.resizeAndRotateImage(path, temp, 200, 200, null);
            /** 请求修改头像 */
            AppModel.INSTANCE.getProfile().uploadHead(temp);
        }
    }


    @Override
    public void onUser(User user) {
        nick.setText(user.getNick());
        if (editNick.getText().length() == 0) {
            editNick.setTag(user.getNick());
        }
        Image.displayPortrait(user.getPortrait(), portrait);
    }

    @Override
    public void onPortraitUploadSuccess(String url) {
        Image.displayPortrait(url, portrait);
    }

    @Override
    public void onPortraitUploadFailure(int code, String errorMessage) {

    }
}
