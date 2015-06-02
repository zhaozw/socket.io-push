package com.yy.misaka.server.lbs.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.misaka.server.lbs.domain.LoginRequest;
import com.yy.misaka.server.lbs.domain.RegisterRequest;
import com.yy.misaka.server.lbs.util.Des3;
import com.yy.misaka.server.lbs.util.ExceptionCodeEnum;
import com.yy.misaka.server.lbs.util.HttpUtil;
import com.yy.misaka.server.support.ServiceException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by xuduo on 1/28/15.
 */
@Component
public class Uaas {

    public final String SIGN_SOURCE = "data=%s&key=%s";
    private final String GRANT_DATA = "{\"as_auth\":\"%s\",\"as_tgt\":\"%s\"}";
    private final String AUTH_DATA = "{\"account\":\"%s\",\"ts\":\"%s\"}";
    private final String SMS_DATA = "{\"mobile\":\"%s\",\"oper_type\":%s,\"country_code\":\"%s\"}";
    private static final String CHECK_SMS_CODE_DATA = "{\"mobile\":\"%s\",\"oper_type\":\"%s\",\"smscode\":\"%s\"}";
    private final String REGISTER_DATA = "{\"account\":\"%s\",\"account_type\":\"%s\",\"password\":\"%s\",\"country_code\":\"%s\",\"authcode\":\"%s\"," +
            "\"imsi\":\"%s\",\"imei\":\"%s\",\"dev_type\":\"%s\",\"dev_id\":\"%s\",\"user_info\":{}}";
    public final String SERVER_KEY = "";
    private final String AUTH_URL = "http://uaas.yy.com/login/auth?appId=%s&sign=%s&data=%s";
    private final String AUTH_CODE_URL = "http://uaas.yy.com/sms/check?appId=%s&sign=%s&data=%s";
    private final String GRANT_URL = "http://uaas.yy.com/login/grant?appId=%s&sign=%s&data=%s";
    private final String SMS_URL = "http://uaas.yy.com/sms/2/get?appId=%s&sign=%s&data=%s";
    private final String REGISTER_URL = "http://uaas.yy.com/register?appId=%s&sign=%s&data=%s";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper mapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthData {

        private String result_code;
        private String as_session;
        private String as_tgt;

        public String getResult_code() {
            return result_code;
        }

        public void setResult_code(String result_code) {
            this.result_code = result_code;
        }

        public String getAs_session() {
            return as_session;
        }

        public void setAs_session(String as_session) {
            this.as_session = as_session;
        }

        public String getAs_tgt() {
            return as_tgt;
        }

        public void setAs_tgt(String as_tgt) {
            this.as_tgt = as_tgt;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthResult {

        private AuthData data;

        public AuthData getData() {
            return data;
        }

        public void setData(AuthData data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GrantData {

        private String result_code;
        private String s_session;
        private String s_t;

        public String getS_session() {
            return s_session;
        }

        public void setS_session(String s_session) {
            this.s_session = s_session;
        }

        public String getS_t() {
            return s_t;
        }

        public void setS_t(String s_t) {
            this.s_t = s_t;
        }

        public String getResult_code() {
            return result_code;
        }

        public void setResult_code(String result_code) {
            this.result_code = result_code;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GrantResult {

        private GrantData data;

        public GrantData getData() {
            return data;
        }

        public void setData(GrantData data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthCodeResult {

        private AuthCodeData data;

        public AuthCodeData getData() {
            return data;
        }

        public void setData(AuthCodeData data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthCodeData {

        private String result_code;
        private String result_desc;
        private String authcode;

        public String getAuthcode() {
            return authcode;
        }

        public void setAuthcode(String authcode) {
            this.authcode = authcode;
        }

        public String getResult_code() {
            return result_code;
        }

        public void setResult_code(String result_code) {
            this.result_code = result_code;
        }

        public String getResult_desc() {
            return result_desc;
        }

        public void setResult_desc(String result_desc) {
            this.result_desc = result_desc;
        }

    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SmsData {

        private String result_code;
        private String result_desc;

        public String getResult_code() {
            return result_code;
        }

        public void setResult_code(String result_code) {
            this.result_code = result_code;
        }

        public String getResult_desc() {
            return result_desc;
        }

        public void setResult_desc(String result_desc) {
            this.result_desc = result_desc;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SmsResult {

        private SmsData data;

        public SmsData getData() {
            return data;
        }

        public void setData(SmsData data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisterData {

        private String result_code;
        private String result_desc;
        private String uuid;

        public String getResult_code() {
            return result_code;
        }

        public void setResult_code(String result_code) {
            this.result_code = result_code;
        }

        public String getResult_desc() {
            return result_desc;
        }

        public void setResult_desc(String result_desc) {
            this.result_desc = result_desc;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisterResult {

        private RegisterData data;

        public RegisterData getData() {
            return data;
        }

        public void setData(RegisterData data) {
            this.data = data;
        }
    }


    public String getAuthReqUrl(String account, String pwd, String appId) {

        long timeStamp = System.currentTimeMillis();

        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String ts;
        try {
            ts = Des3.encode(timeStamp + "", pwd);
        } catch (Exception e) {
            throw new ServiceException(-5, "验证失败错误!");
        }

        String dataContent = String.format(AUTH_DATA, account, ts);
        String signContext = String.format(SIGN_SOURCE, dataContent, SERVER_KEY);
        String sign = DigestUtils.sha256Hex(signContext);
        String signEncode = encode(sign);

        String appIdEncode = encode(appId);
        String dataContentEncode = encode(dataContent);
        return String.format(AUTH_URL, appIdEncode, signEncode, dataContentEncode);
    }

    public String encode(String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return text;
    }

    public String getGrantReqUrl(String account, String asSessionKey, String asTgt, String appId) {
        long time = System.currentTimeMillis();
        String asAuth = "";
        try {
            asAuth = Des3.encode(account + "," + time, asSessionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dataContent = String.format(GRANT_DATA, asAuth, asTgt);
        String signContext = String.format(SIGN_SOURCE, dataContent, SERVER_KEY);
        String sign = DigestUtils.sha256Hex(signContext);
        String signEncode = encode(sign);

        String appIdEncode = encode(appId);
        String dataContentEncode = encode(dataContent);
        return String.format(GRANT_URL,
                appIdEncode, signEncode, dataContentEncode);
    }

    private String getAsSessionKey(String asSession, String pwd) {

        String asSessionValue = "";
        try {
            asSessionValue = Des3.decode(asSession, pwd);
        } catch (Exception e) {
            throw new ServiceException(-1, "用户验证失败!");
        }

        String sessionValue[] = asSessionValue.split(",");
        if (sessionValue != null && sessionValue.length >= 3) {
            return sessionValue[2];
        }
        return "";
    }

    public LoginRequest login(String phone, String password, String appId) {
        AuthResult authResult = getAuthResult(phone, password, appId);
        GrantResult grantResult = getGrantResult(phone, password, authResult, appId);
        try {
            String decode = Des3.decode(grantResult.data.s_session, getAsSessionKey(authResult.getData().getAs_session(), password));
            String[] decoded = decode.split(",");
            LoginRequest user = new LoginRequest();
            user.setUid(decoded[0]);
            user.setPassword(password);
            user.setPhone(phone);
            logger.info("decode user from uaas {}", user);
            return user;
        } catch (Exception e) {
            logger.error("decode error json error", e);
            ExceptionCodeEnum.throwReqFail();
            return null;
        }
    }

    private AuthResult getAuthResult(String phone, String password, String appId) {
        String authReqUrl = getAuthReqUrl(phone, password, appId);
        String result = HttpUtil.httpGet(authReqUrl);
        logger.debug("auth result {}", result);
        AuthResult authResult = null;
        try {
            authResult = mapper.readValue(result, AuthResult.class);
        } catch (IOException e) {
            logger.error("parse json error {}", result, e);
            ExceptionCodeEnum.throwReqFail();
        }
        return ExceptionCodeEnum.findOrThrow(authResult.data.result_code, authResult);
    }

    private GrantResult getGrantResult(String phone, String password, AuthResult authResult, String appId) {
        String grantUrl = getGrantReqUrl(phone, getAsSessionKey(authResult.getData().getAs_session(), password), authResult.getData().getAs_tgt(), appId);
        String result = HttpUtil.httpGet(grantUrl);
        GrantResult grantResult = null;
        try {
            grantResult = mapper.readValue(result, GrantResult.class);
        } catch (IOException e) {
            logger.error("parse json error {}", result, e);
            ExceptionCodeEnum.throwReqFail();
        }
        return ExceptionCodeEnum.findOrThrow(grantResult.data.result_code, grantResult);
    }

    public SmsResult getSms(String mobile, int operType, String countryCode, String appId) {
        String dataContent = String.format(SMS_DATA, mobile, operType, countryCode);
        String signContext = String.format(SIGN_SOURCE, dataContent, SERVER_KEY);
        String sign = DigestUtils.sha256Hex(signContext);
        String signEncode = encode(sign);
        String appIdEncode = encode(appId);
        String dataContentEncode = encode(dataContent);
        String getSmsUrl = String.format(SMS_URL, appIdEncode, signEncode, dataContentEncode);
        String result = HttpUtil.httpGet(getSmsUrl);
        SmsResult smsResult = null;
        try {
            smsResult = mapper.readValue(result, SmsResult.class);
        } catch (IOException e) {
            logger.error("parse json error {}", result, e);
            ExceptionCodeEnum.throwReqFail();
        }
        if (smsResult == null || smsResult.data == null) {
            logger.error("parse json error {}", result);
            ExceptionCodeEnum.throwReqFail();
        }
        return ExceptionCodeEnum.findOrThrow(smsResult.data.result_code, smsResult);
    }


    private String getAuthCode(String appId, String mobile, int type, String smsCode) {
        String dataContent = String.format(CHECK_SMS_CODE_DATA, mobile, type, smsCode);
        String signContext = String.format(SIGN_SOURCE, dataContent, SERVER_KEY);
        String sign = DigestUtils.sha256Hex(signContext);
        String signEncode = encode(sign);
        String appIdEncode = encode(appId);
        String dataContentEncode = encode(dataContent);
        String getAuthCodeUrl = String.format(AUTH_CODE_URL, appIdEncode, signEncode, dataContentEncode);
        String result = HttpUtil.httpGet(getAuthCodeUrl);
        AuthCodeResult authCodeResult = null;
        try {
            authCodeResult = mapper.readValue(result, AuthCodeResult.class);
        } catch (IOException e) {
            logger.error("parse json error {}", result, e);
            ExceptionCodeEnum.throwReqFail();
        }
        logger.debug("authCodeResult {} {} ", authCodeResult.data.result_code, authCodeResult.data.getResult_desc());
        ExceptionCodeEnum.findOrThrow(authCodeResult.data.result_code, authCodeResult);
        return authCodeResult.data.result_code;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        String authCode = getAuthCode(registerRequest.getAppId(), registerRequest.getMobile(), registerRequest.getAccountType(), registerRequest.getAuthcode());

        String dataContent = String.format(REGISTER_DATA, registerRequest.getMobile(), registerRequest.getAccountType(), registerRequest.getPassword(), registerRequest.getCountryCode(),
                authCode, registerRequest.getImsi(), registerRequest.getImei(), registerRequest.getDevType(), registerRequest.getDevId(), registerRequest.getUserInfo());
        String signContext = String.format(SIGN_SOURCE, dataContent, SERVER_KEY);
        String sign = DigestUtils.sha256Hex(signContext);
        String signEncode = encode(sign);
        String appIdEncode = encode(registerRequest.getAppId());
        String dataContentEncode = encode(dataContent);
        String getSmsUrl = String.format(REGISTER_URL, appIdEncode, signEncode, dataContentEncode);
        String result = HttpUtil.httpGet(getSmsUrl);
        RegisterResult registerResult = null;
        try {
            registerResult = mapper.readValue(result, RegisterResult.class);
        } catch (IOException e) {
            logger.error("parse json error {}", result, e);
            ExceptionCodeEnum.throwReqFail();
        }
        if (registerResult == null || registerResult.data == null) {
            logger.error("parse json error {}", result);
            ExceptionCodeEnum.throwReqFail();
        }
        return ExceptionCodeEnum.findOrThrow(registerResult.data.result_code, registerResult);
    }

//    public static void main(String[] args) {
//        Uaas uaas = new Uaas();
//        String account = "8618680268780";
//        String pwd = DigestUtils.sha256Hex("qwe123456");
//        uaas.login(account, pwd, "fm141027");
//    }

//    public static void main(String[] args) {
//
//        Uaas uaas = new Uaas();
//        String account = "8618680268780";
//        String pwd = DigestUtils.sha256Hex("qwe123456");
//        String authReqUrl = uaas.getAuthReqUrl(account, pwd);
//        String result = HttpUtil.getUrlAsString(authReqUrl);
//        try {
//            AuthResult authResult = uaas.mapper.readValue(result, AuthResult.class);
//            String asSessionKey = authResult.getData().getAs_session();
//            asSessionKey = uaas.getAsSessionKey(asSessionKey, pwd);
//            String asTgt = authResult.getData().getAs_tgt();
//            String grantUrl = uaas.getGrantReqUrl(account, asSessionKey, asTgt);
//            result = HttpUtil.getUrlAsString(grantUrl);
//            GrantResult grantResult = uaas.mapper.readValue(result, GrantResult.class);
//            String decode = Des3.decode(grantResult.data.s_session, asSessionKey);
//            System.out.println(decode);
//
//            String sAuth = "";
//            try {
//                sAuth = Des3.encode("8618680268780" + "," + System.currentTimeMillis(), decode.split(",")[3]);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            uaas.logger.info("sAuthDecode {} ", Des3.decode(sAuth, decode.split(",")[3]));
//            String st = Des3.encode("8618680268780" + "," + decode.split(",")[0] + "," + System.currentTimeMillis() + ",600," + decode.split(",")[3], uaas.SERVER_KEY);
//            uaas.logger.info("s_t decode {}", Des3.decode(grantResult.data.s_t, uaas.SERVER_KEY));
//
//
//            String webUrl = "http://119.147.175.94/discuztest/sleepapp/logincallback.php";
//            HttpClient httpClient = new HttpClient();
//            PostMethod get = new PostMethod(webUrl);
//            get.addParameter("load_content", "{\"s_auth\":\"" + sAuth + "\",\"s_t\":\"" + grantResult.data.s_t + "\"}");
//
//            get.setRequestHeader("User-Agent",
//                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
//            get.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//            get.setRequestHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
//
//            Object o = null;
//            try {
//                httpClient.executeMethod(get);
//                int code = get.getStatusCode();
//                if (code >= 400) {
//                }
//                o = get.getResponseBodyAsString();
//                System.out.println("o ++ " + o);
//            } catch (Exception e) {
//                result = null;
//            } finally {
//                get.releaseConnection();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
