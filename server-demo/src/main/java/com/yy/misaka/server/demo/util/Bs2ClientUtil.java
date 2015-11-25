package com.yy.misaka.server.demo.util;

import org.apache.commons.codec.binary.Base64;

import java.util.regex.Pattern;

/**
 * bs2的一些工具方法
 *
 * @author haoqing
 */
public class Bs2ClientUtil {

    private static final String SEPARATE = "\n";

    public static final String BS2_DOMAIN = "bs2.yy.com";

    public static final String BS2CDN_DOMAIN = "bs2cdn.yy.com";

    private static final String HTTP = "http://";

    private static final String DOT = ".";

    public static final String BACK_SLASH = "/";

    private static final String COLON = ":";

    private static final String TOKEN = "token";

    public static final String QUESTION_MARK = "?";

    public static final String EQUAL = "=";

    public static final String AND = "&";

    public static Pattern UPLOAD_ID_PATTERN = Pattern.compile(".*?<zone>(.*?)</zone>.*?<uploadid>(.*?)</uploadid>.*?");

    public static final String COMPLETE_XML_FORMATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><uploadscomplete><partcount>%s</partcount></uploadscomplete>";


    public interface BS2HEADER {
        public static final String HOST = "Host";
        public static final String DATE = "Date";
        public static final String AUTHORIZATION = "Authorization";
        public static final String ACCESSID = "AccessId";
        public static final String CONTENT_LENGTH = "Content-Length";
    }

    public interface BS2REQUESTURI {
        public static final String BUCKET = "bucket";
        public static final String PART_COUNT = "partcount";
        public static final String UPLOADID = "uploadid";
        public static final String PARTNUMBER = "partnumber";
        public static final String UPLOADS = "uploads";
        public static final String UPLOADSCOMPLETE = "uploadscomplete";
    }

    /**
     * bs2对外协议authorization生成规则
     *
     * @param method       http请求的方式
     * @param filename     文件名
     * @param expires      授权码的到期时间戳
     * @param bucketName   bs2的bucket名称
     * @param accessSecret
     * @return
     * @throws java.security.InvalidKeyException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String getAuthorization(String method, String filename, String expires, String bucketName, String accessSecret, String accessKey) {
        String content = method + SEPARATE + bucketName + SEPARATE + filename + SEPARATE + expires + SEPARATE;
        byte[] hmac;
        try {
            hmac = HmacSha1.getSignature(content.getBytes(), accessSecret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String temp = Base64.encodeBase64String(hmac).replaceAll("\\+", "-").replaceAll("/", "_");
        return accessKey + COLON + temp + COLON + expires;
    }


//	/**
//	 * bs2对外协议authorization生成规则
//     * @param method  http请求的方式
//     * @param fileName   文件名
//     * @param expires    授权码的到期时间戳
//	 * @param appInfo   业务基本信息
//	 * @return
//	 * @throws java.security.NoSuchAlgorithmException
//	 * @throws java.security.InvalidKeyException
//	 */
//	public static String getAuthorization(String method, String fileName, String expires, String bucketName,String secret,String key) throws InvalidKeyException, NoSuchAlgorithmException{
//	    return getAuthorization(method, fileName, expires, bucketName, secret, key);
//	}

    /**
     * 返回类似  http://cs_base.bs2.yy.com/test.jpg 的url
     *
     * @param bucketName 如上url, bucketName 为 cs_base
     * @param fileName   如上url, bucketName 为test.jpg
     * @return
     */
    protected static String generateHttpUrl(String bucketName, String fileName) {
        return HTTP + bucketName + DOT + BS2_DOMAIN + BACK_SLASH + fileName;
    }


    /**
     * 返回类似  http://cs_base.bs2.yy.com/test.jpg 的url
     *
     * @param bucketName 如上url, bucketName 为 cs_base
     * @param fileName   如上url, bucketName 为test.jpg
     * @return
     */
    protected static String generateHttpCDNUrl(String bucketName, String fileName) {
        return HTTP + bucketName + DOT + BS2CDN_DOMAIN + BACK_SLASH + fileName;
    }

    /**
     * 返回类似  cs_base.bs2.yy.com
     *
     * @param bucketName 如上url, bucketName 为 cs_base
     * @return
     */
    protected static String generateHost(String bucketName) {
        return bucketName + DOT + BS2_DOMAIN;
    }

    /**
     * 返回带有token的url,比如  http://cs_base.bs2.yy.com/test.jpg?token=xxxx
     *
     * @param uri
     * @param token
     * @return
     */
    public static String generateTokenUrl(String uri, String token) {
        return uri + QUESTION_MARK + TOKEN + EQUAL + token;
    }

    /**
     * 返回requestUri,比如 http://bs2.yy.com/uploads
     *
     * @param requestUri 比如 uploads
     * @return
     */
    public static String generateRequestURI(String requestUri) {
        return HTTP + BS2_DOMAIN + BACK_SLASH + requestUri;
    }

    /**
     * 返回requestUri,比如 http://bs2.yy.com/uploads
     *
     * @param host       比如bs2.yy.com
     * @param requestUri 比如 uploads
     * @return
     */
    public static String generateRequestURI(String host, String requestUri) {
        return HTTP + host + BACK_SLASH + requestUri;
    }


    /**
     * 返回大文件的下载地址,比如 http://bs2.yy.com/large4cs/largeApk2.apk
     *
     * @param host       比如bs2.yy.com
     * @param bucketName 比如 large4cs
     * @param fileName   比如 largeApk2.apk
     * @return
     */
    public static String generateLargeFileLoadDownURL(String bucketName, String fileName) {
        return HTTP + BS2_DOMAIN + BACK_SLASH + bucketName + BACK_SLASH + fileName;
    }
}


