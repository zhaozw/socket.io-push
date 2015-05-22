package com.yy.misaka.demo.appmodel;

import android.content.Context;
import com.yy.androidlib.util.http.AsyncHttp;
import com.yy.androidlib.util.http.FormEntry;
import com.yy.androidlib.util.notification.NotificationCenter;
import com.yy.androidlib.websocket.Destination;
import com.yy.androidlib.websocket.ReplyHandler;
import com.yy.androidlib.websocket.StompClient;
import com.yy.misaka.demo.User;
import com.yy.misaka.demo.appmodel.callback.ProfileCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangtian
 *         Date: 15/3/5
 */
public class ProfileModel {
    private static final String TAG = "PROFILE";

    private final StompClient stomp;

    /**
     * Application
     */
    private final Context context;

    private String host;
    private User myInfo;

    public ProfileModel(Context context, StompClient stomp, String host) {
        this.host = host;
        this.context = context;
        this.stomp = stomp;
        myInfo = new User();
    }

    /**
     * 请求server修改昵称
     *
     * @param user 改后昵称
     */
    public void modifyUser(User user) {
        stomp.request("demo-server", "/modifyMyInfo", user, new ReplyHandler<User>(User.class) {
            @Override
            public void onSuccess(User result) {
//                NotificationCenter.INSTANCE.getObserver(ProfileCallback.Modify.class).onNameModifySuccess(result);
                queryMyInfo();
            }

            @Override
            public void onError(int code, String message) {
                NotificationCenter.INSTANCE.getObserver(ProfileCallback.Modify.class).onNameModifyFailure(code, message);
            }
        });
    }

    /**
     * 请求server修改头像
     *
     * @param path 本地头像文件url
     */
    public void uploadHead(String path) {
        List<FormEntry> formEntries = new ArrayList<FormEntry>();
        formEntries.add(new FormEntry(FormEntry.Type.File, "file", path));
        AsyncHttp.post(host + "/uploadFile", formEntries, new AsyncHttp.ResultCallback() {
            @Override
            public void onSuccess(String url, int statusCode, String result) {
                User user = new User();
                user.setPortrait(result);
                AppModel.INSTANCE.getProfile().modifyUser(user);
            }

            @Override
            public void onFailure(String url, int statusCode, int errorType, Throwable throwable) {
                NotificationCenter.INSTANCE.getObserver(ProfileCallback.Portrait.class).onPortraitUploadFailure(statusCode, "");
            }

        });
    }

//    /**
//     * 从服务器取得改后头像
//     *
//     * @param path 服务器头像文件url
//     */
//    public void getHeadImage(final String path) {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL(path);
//                    url.openConnection();
//                    Bitmap bmp = BitmapFactory.decodeStream(url.openStream());
//                    NotificationCenter.INSTANCE.getObserver(ProfileCallback.portrait.class).onGetSuccess(bmp);
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                    NotificationCenter.INSTANCE.getObserver(ProfileCallback.portrait.class).onGetFailure(e.getMessage());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    NotificationCenter.INSTANCE.getObserver(ProfileCallback.portrait.class).onGetFailure(e.getMessage());
//                }
//            }
//        }.start();
//    }


    public void queryMyInfo() {
        stomp.request("demo-server", "/myInfo", null, new ReplyHandler<User>(User.class) {
            @Override
            public void onSuccess(User result) {
                myInfo = result;
                NotificationCenter.INSTANCE.getObserver(ProfileCallback.Info.class).onUser(result);
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    public User getMyInfo() {
        return myInfo;
    }
}





