package com.yy.misaka.server.lbs.util;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GZIPAwareGetMethod extends GetMethod {
    /**
     * Creates a new instance of GZIPAwareGetMethod
     */
    public GZIPAwareGetMethod() {
        super();
    }

    /**
     * Constructor specifying a URI.
     *
     * @param uri either an absolute or relative URI
     * @since 1.0
     */
    public GZIPAwareGetMethod(String uri) {
        super(uri);
    }

    /**
     * Overrides method in {@link org.apache.commons.httpclient.HttpMethodBase}.
     * <p/>
     * Notifies the server that we can process a GZIP-compressed response before
     * sending the request.
     */
    public int execute(HttpState state, HttpConnection conn) throws HttpException, IOException {
        // Tell the server that we can handle GZIP-compressed data in the
        // response body addRequestHeader("Accept-Encoding", "gzip");

        return super.execute(state, conn);
    }

    /**
     * Overrides method in {@link org.apache.commons.httpclient.methods.GetMethod} to set the responseStream variable
     * \ appropriately.
     * <p/>
     * If the response body was GZIP-compressed, responseStream will be set to a
     * \ GZIPInputStream wrapping the original InputStream used by the
     * superclass.
     */
    protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException, HttpException {
        super.readResponseBody(state, conn);
        int a = 0;
        Header contentEncodingHeader = getResponseHeader("Content-Encoding");

        if (contentEncodingHeader != null && contentEncodingHeader.getValue().contains("gzip")) {
            setResponseStream(new GZIPInputStream(getResponseStream()));
        }
    }

}
