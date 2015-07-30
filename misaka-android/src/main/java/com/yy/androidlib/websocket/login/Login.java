package com.yy.androidlib.websocket.login;

import android.content.Context;
import com.yy.androidlib.websocket.Callback;
import com.yy.androidlib.websocket.MisakaClient;
import com.yy.androidlib.websocket.ReplyHandler;

public class Login {

    private MisakaClient misakaClient;
    private String appId = "login";
    private String loginHost = "http://uaas.yy.com";
    private LoginRequest request;
    private Context context;

    public String getCurrentUid() {
        if (request != null) {
            return request.getUid();
        } else {
            return "0";
        }
    }

    public Login(Context context, MisakaClient misakaClient, final String loginAppId, String loginHost) {
        this.context = context;
        this.misakaClient = misakaClient;
        this.appId = loginAppId;
        this.loginHost = loginHost;
        misakaClient.addConnectionCallback(new Callback() {
            @Override
            public void onConnected() {
                if (request != null) {
                    login(request, null);
                }
            }
        });
    }

    public void login(LoginRequest user, final ReplyHandler<LoginRequest> handler) {
        user.setAppId(appId);
        if (!user.getPhone().startsWith("86")) {
            user.setPhone("86" + user.getPhone());
            user.setPassword(LoginUtils.EncryptSha256(LoginUtils.EncryptSha256(user.getPassword())));
        }
        misakaClient.request(loginHost, "/login", user, new ReplyHandler<LoginRequest>(LoginRequest.class) {

            @Override
            public void onSuccess(LoginRequest result) {
                request = result;
                if (handler != null) {
                    handler.onSuccess(result);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (handler != null) {
                    handler.onError(code, message);
                }
            }
        });
    }

    public void getRegisterSmsCode(String phone, String countryCode, ReplyHandler<RegisterRequest> handler) {
        RegisterRequest request = new RegisterRequest();
        request.setMobile(LoginUtils.chinaMobile(phone));
        request.setCountryCode(countryCode);
        request.setAppId(appId);
        misakaClient.request("login", "/getRegisterSms", request, handler);
    }

    public void register(String phone, String password, String code, ReplyHandler<RegisterRequest> handler) {
        RegisterRequest request = new RegisterRequest();
        request.setMobile(LoginUtils.chinaMobile(phone));
        request.setCountryCode("86");
        request.setAppId(appId);
        String pwdSha = LoginUtils.EncryptSha256(LoginUtils.EncryptSha256(password));
        request.setPassword(pwdSha);
        request.setAuthcode(code);
        request.setImei(LoginUtils.getImei(context));
        request.setImsi(LoginUtils.getImsi(context));
        request.setDevType(0);// android
        request.setDevId(LoginUtils.getImei(context));
        misakaClient.request("login", "/register", request, handler);
    }
}
