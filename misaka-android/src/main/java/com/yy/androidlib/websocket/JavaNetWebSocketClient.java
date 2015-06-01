package com.yy.androidlib.websocket;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.yy.androidlib.util.apache.RandomStringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class JavaNetWebSocketClient implements IWebSocketClient {

    private static final String TAG = "JavaNetWebSocketClient";

    private URI mURI;
    private WebSocketListener mListener;
    private Socket mSocket;
    private Thread mThread;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private List<BasicNameValuePair> mExtraHeaders;
    private HybiParser mParser;

    private final Object mSendLock = new Object();

    private static TrustManager[] sTrustManagers;
    private Handler handler = new Handler(Looper.getMainLooper());
    /*
        connecttask
     */
    private Runnable connectTask = new Runnable() {
        @Override
        public void run() {
            if (!isConnected()) {
                doConnect();
            }
            handler.removeCallbacks(this);
            handler.postDelayed(this, 5000L);
        }
    };

    public static void setTrustManagers(TrustManager[] tm) {
        sTrustManagers = tm;
    }

    public JavaNetWebSocketClient(URI uri, WebSocketListener listener) {
        mURI = uri;
        mListener = listener;
        mParser = new HybiParser(this);

        mHandlerThread = new HandlerThread("websocket-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public WebSocketListener getListener() {
        return mListener;
    }

    public void connect() {
        handler.removeCallbacks(connectTask);
        handler.post(connectTask);
    }

    @Override
    public boolean isDisconnected() {
        return mSocket == null || mSocket.isClosed();
    }

    @Override
    public void close() {
        closeSocket();
    }

    public void doConnect() {
        if (mThread != null && mThread.isAlive()) {
            return;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String secret = createSecret();

                    URI uri = new URI(mURI.toString() + "/stomp/1/" + RandomStringUtils.randomAlphanumeric(32) + "/websocket");

                    int port = (uri.getPort() != -1) ? uri.getPort() : (uri.getScheme().equals("wss") ? 443 : 80);

                    String path = TextUtils.isEmpty(uri.getPath()) ? "/" : uri.getPath();
                    if (!TextUtils.isEmpty(uri.getQuery())) {
                        path += "?" + uri.getQuery();
                    }

                    String originScheme = uri.getScheme().equals("wss") ? "https" : "http";
                    URI origin = new URI(originScheme, "//" + mURI.getHost(), null);

                    SocketFactory factory = uri.getScheme().equals("wss") ? getSSLSocketFactory() : SocketFactory.getDefault();
                    mSocket = factory.createSocket(uri.getHost(), port);

                    PrintWriter out = new PrintWriter(mSocket.getOutputStream());
                    out.print("GET " + path + " HTTP/1.1\r\n");
                    out.print("Upgrade: websocket\r\n");
                    out.print("Connection: Upgrade\r\n");
                    out.print("Host: " + mURI.getHost() + "\r\n");
                    out.print("Origin: " + origin.toString() + "\r\n");
                    out.print("Sec-WebSocket-Key: " + secret + "\r\n");
                    out.print("Sec-WebSocket-Version: 13\r\n");
                    if (mExtraHeaders != null) {
                        for (NameValuePair pair : mExtraHeaders) {
                            out.print(String.format("%s: %s\r\n", pair.getName(), pair.getValue()));
                        }
                    }
                    out.print("\r\n");
                    out.flush();

                    HybiParser.HappyDataInputStream stream = new HybiParser.HappyDataInputStream(mSocket.getInputStream());

                    // Read HTTP response status line.
                    StatusLine statusLine = parseStatusLine(readLine(stream));
                    if (statusLine == null) {
                        throw new HttpException("Received no reply from server.");
                    } else if (statusLine.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }

                    // Read HTTP response headers.
                    String line;
                    boolean validated = false;

                    while (!TextUtils.isEmpty(line = readLine(stream))) {
                        Header header = parseHeader(line);
                        if (header.getName().equals("Sec-WebSocket-Accept")) {
                            String expected = createSecretValidation(secret);
                            String actual = header.getValue().trim();

                            if (!expected.equals(actual)) {
                                throw new HttpException("Bad Sec-WebSocket-Accept header value.");
                            }
                            validated = true;
                        }
                    }

                    if (!validated) {
                        throw new HttpException("No Sec-WebSocket-Accept header.");
                    }

//                    mListener.onConnect();

                    // Now decode websocket frames.
                    mParser.start(stream);

                } catch (EOFException ex) {
                    Log.d(TAG, "WebSocket EOF!", ex);
                    mListener.onDisconnect("EOF", ex);
                    closeSocket();
                } catch (SSLException ex) {
                    // Connection reset by peer
                    Log.d(TAG, "Websocket SSL error!", ex);
                    mListener.onDisconnect("SSL", ex);
                    closeSocket();
                } catch (Exception ex) {
                    mListener.onDisconnect("Exception " + ex.getMessage(), ex);
                    closeSocket();
                }
            }
        });
        mThread.start();
    }

    private void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close socket error");
            }
            mSocket = null;
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSocket.close();
                        mSocket = null;
                    } catch (IOException ex) {
                        Log.d(TAG, "Error while disconnecting", ex);
                        mListener.onDisconnect("Exception " + ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    public boolean send(String data) {
        if (isConnected()) {
            sendFrame(mParser.frame(data));
            return true;
        } else {
            return false;
        }
    }

    public void send(byte[] data) {
        sendFrame(mParser.frame(data));
    }

    private StatusLine parseStatusLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        return BasicLineParser.parseStatusLine(line, new BasicLineParser());
    }

    private Header parseHeader(String line) {
        return BasicLineParser.parseHeader(line, new BasicLineParser());
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private String readLine(HybiParser.HappyDataInputStream reader) throws IOException {
        int readChar = reader.read();
        if (readChar == -1) {
            return null;
        }
        StringBuilder string = new StringBuilder("");
        while (readChar != '\n') {
            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read();
            if (readChar == -1) {
                return null;
            }
        }
        return string.toString();
    }

    private String createSecret() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) (Math.random() * 256);
        }
        return Base64.encodeToString(nonce, Base64.DEFAULT).trim();
    }

    private String createSecretValidation(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update((secret + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());
            return Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    void sendFrame(final byte[] frame) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mSendLock) {
                        if (mSocket == null) {
                            throw new IllegalStateException("Socket not connected");
                        }
                        OutputStream outputStream = mSocket.getOutputStream();
                        outputStream.write(frame);
                        outputStream.flush();
                    }
                } catch (IOException ex) {
                    mListener.onDisconnect("Exception " + ex.getMessage(), ex);
                    closeSocket();
                }
            }
        });
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    private SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, sTrustManagers, null);
        return context.getSocketFactory();
    }

}