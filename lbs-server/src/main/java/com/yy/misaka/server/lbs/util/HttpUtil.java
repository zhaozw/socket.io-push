package com.yy.misaka.server.lbs.util;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpUtil {
    private static Logger LOG = LoggerFactory.getLogger(HttpUtil.class);
    public static String ACCEPT_LANGUAGE_HEADER_KEY = "Accept-Language";
    private static AsyncHttpClient asyncHttpClient = asyncHttpClient();

    public static AsyncHttpClient asyncHttpClient() {
        int timeout = 5000;
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectTimeout(timeout).setReadTimeout(timeout)
                .setRequestTimeout(timeout).build());
        return asyncHttpClient;
    }
    
    public static <T> void asyncGet(String url, AsyncCompletionHandler<T> handler) {
        asyncHttpClient.prepareGet(url).execute(handler);
    }
    
    public static String httpGet(String url) {
        try {
            return asyncHttpClient.prepareGet(url).execute().get().getResponseBody("UTF-8");
        } catch (Exception e) {
            LOG.error("http request error", e);
        }
        return null;
    }

    private static Object getUrlAsObject(String url, boolean asString, Header... headers) {
        HttpClient httpClient = new HttpClient();
        HttpMethod get = new GetMethod(url);

//        get.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        get.setRequestHeader("Accept-Encoding", "gzip,deflate");
//        get.setRequestHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");

        for (Header header : headers) {
            get.setRequestHeader(header);
        }
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        Object result = null;
        try {
            httpClient.executeMethod(get);
            int code = get.getStatusCode();
            if (code >= 400) {
                LOG.error("http status code error : " + code + ",url" + url);
                return null;
            }
            if (asString) {
                result = get.getResponseBodyAsString();
            } else {
                result = input2byte(get.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            LOG.error("http request error", e);
            result = null;
        } finally {
            get.releaseConnection();
        }

        LOG.debug("http get url : " + url + " ,string : " + result);
        return result;
    }

    public static String postUrl(String url, byte[] body) {
        HttpClient httpClient = new HttpClient();
        PostMethod get = new PostMethod(url);

        get.setRequestHeader("User-Agent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
        get.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.setRequestHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");

        get.setRequestEntity(new ByteArrayRequestEntity(body));

        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String result = null;
        try {
            httpClient.executeMethod(get);
            int code = get.getStatusCode();
            result = get.getResponseBodyAsString();
            if (code >= 400) {
                LOG.error("http status code error : " + code + ",url" + url + " result :" + result);
                return null;
            }

        } catch (Exception e) {
            LOG.error("http request error", e);
            result = null;
        } finally {
            get.releaseConnection();
        }

        LOG.debug("http get url : " + url + " ,string : " + result);
        return result;
    }

    /**
     * @param url
     * @return null if error
     */
    public static String getUrlAsString(String url, Header... headers) {
        return (String) getUrlAsObject(url, true, headers);
    }

    /**
     * @param url
     * @return null if error
     */
    public static byte[] getUrlAsByte(String url, Header... headers) {
        return (byte[]) getUrlAsObject(url, false, headers);
    }

    public static String getUrlAsString(String url) {
        return getUrlAsString(url, new Header[]{});
    }

    public static byte[] getUrlAsByte(String url) {
        return getUrlAsByte(url, new Header[]{});
    }

    public static final byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

}
