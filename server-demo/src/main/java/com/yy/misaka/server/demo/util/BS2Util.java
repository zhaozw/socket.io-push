package com.yy.misaka.server.demo.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;

@Component
public class BS2Util {
    private static Logger LOG = LoggerFactory.getLogger(BS2Util.class);
    public static String ACCEPT_LANGUAGE_HEADER_KEY = "Accept-Language";
    private String bucketName = "sleepappimage";
    private String key = "ak_ngh";
    private String secret = "d2598dd2a898dee022a084c56fde92aa5098e87d";

    public String uploadFile(MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        MimeType jpeg = MimeType.valueOf(contentType);
        String jpegExt = jpeg.getSubtype(); // .jpg
        String filename = DigestUtils.md5Hex(file.getInputStream()) + "." + jpegExt;
        String url = Bs2ClientUtil.generateHttpUrl(bucketName, filename);
//        url = "http://sleepappimage.bs2ul.yy.com/test.png";
        HttpClient httpClient = new HttpClient();
        PutMethod httpPut = new PutMethod(url);

//        get.setRequestHeader("User-Agent",
//                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
//        get.setRequestHeader("Date", new Date().toString());
//        get.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        get.setRequestHeader("Accept-Encoding", "gzip,deflate");
//        get.setRequestHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
//        try {
//            FileRequestEntity entity = new FileRequestEntity(img, Files.probeContentType(Paths.get(img.getAbsolutePath())));
//            get.setRequestEntity(entity);
//        } catch (IOException e) {
//            LOG.error("", e);
//        }
//
//        for (Header header : headers) {
//            get.setRequestHeader(header);
//        }

        httpPut.setRequestHeader(Bs2ClientUtil.BS2HEADER.HOST, Bs2ClientUtil.generateHost(bucketName));
        httpPut.setRequestHeader(Bs2ClientUtil.BS2HEADER.DATE, Calendar.getInstance().getTime().toString());
        String expires = String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000);

        httpPut.setRequestHeader(Bs2ClientUtil.BS2HEADER.AUTHORIZATION,
                Bs2ClientUtil.getAuthorization(httpPut.getName(), filename, expires, bucketName, secret, key));
        httpPut.setRequestHeader(Bs2ClientUtil.BS2HEADER.ACCESSID, key);

        try {
            InputStreamRequestEntity entity = new InputStreamRequestEntity(file.getInputStream(), contentType);
            httpPut.setRequestEntity(entity);
        } catch (IOException e) {
            LOG.error("set entity error", e);
            return null;
        }

        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());


        String fileUrl;
        try {
            httpClient.executeMethod(httpPut);
            int code = httpPut.getStatusCode();
            if (code >= 400) {
                LOG.error("http status code error : " + code + ",url " + url);
                return null;
            }
            fileUrl = Bs2ClientUtil.generateHttpUrl(bucketName, filename);
        } catch (Exception e) {
            LOG.error("http request error", e);
            fileUrl = null;
        }

        LOG.debug("http get url : " + url + " ,string : " + fileUrl);
        return fileUrl;
    }

//    public static void main(String[] args) {
//        BS2Util util = new BS2Util();
//        util.uploadFile();
//    }

}
