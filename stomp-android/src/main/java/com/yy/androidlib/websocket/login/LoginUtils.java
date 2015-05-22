package com.yy.androidlib.websocket.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

//import thirdparty.codec.binary.Base64;

public class LoginUtils {

    private static final String COMMON_KEY = "RANDOM_UUID";
    private static final String COMMON_REF = "login_commom_ref";


    public static String EncryptSha256(String strSrc) {
        MessageDigest md;
        String strDes;

        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    public static String bytes2Hex(byte[] bts) {
        StringBuffer des = new StringBuffer("");
        String tmp;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }

    public static String chinaMobile(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return "";
        }
        if (mobile.startsWith("86")) {
            return mobile;
        } else {
            return "86" + mobile;
        }
    }

    public static String getImei(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();
        if (!TextUtils.isEmpty(imei) && !imei.matches("0+") && !imei.equals("004999010640000"))
            return imei;
        else {
            SharedPreferences preferences = context.getSharedPreferences(COMMON_REF, Context.MODE_PRIVATE);
            String rndId = preferences.getString(COMMON_KEY, "");
            if (TextUtils.isEmpty(rndId)) {
                rndId = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(COMMON_KEY, rndId);
                editor.commit();
            }
            return rndId;
        }
    }

    public static String getImsi(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = mTelephonyMgr.getSubscriberId();
        if (TextUtils.isEmpty(imsi)) {
            return "";
        }
        return imsi;
    }

}
