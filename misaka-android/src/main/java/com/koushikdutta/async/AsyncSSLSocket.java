package com.koushikdutta.async;

import javax.net.ssl.SSLEngine;
import java.security.cert.X509Certificate;

public interface AsyncSSLSocket extends AsyncSocket {
    public X509Certificate[] getPeerCertificates();
    public SSLEngine getSSLEngine();
}
