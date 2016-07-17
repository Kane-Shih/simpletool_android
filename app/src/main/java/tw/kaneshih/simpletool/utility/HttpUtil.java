package tw.kaneshih.simpletool.utility;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.os.Build;
import android.support.annotation.IntDef;
import android.util.Base64;

public final class HttpUtil {
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    public static final int DEFAULT_TRY_COUNT = 1;

    public static final int TIMEOUT_VALID_MININUM = 3000;

    private static final String TAG = "HttpUtil";

    private static final String CRLF = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String BOUNDARY = "**********";

    private HttpUtil() {
    }

    public static final class Request {
        public static final int GET = 0;
        public static final int POST = 1;

        @IntDef({GET, POST})
        public @interface Method {
        }

        // basic
        private @Method int method;
        private String url;
        private String query;
        private Map<String, String> queryStore;
        private Map<String, String> header;

        // connection control
        private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private int tryCount = DEFAULT_TRY_COUNT;

        // SSL
        private String sslCert;
        private HostnameVerifier hostnameVerifier;

        // advanced
        private boolean isCollectHeader = false;

        // multipart
        private File file;
        private String fileParamName;
        private String fileContentType;

        public Request(@Method int method) throws IllegalArgumentException {
            this.method = method;
        }

        /**
         * @param url
         * @return
         * @throws IllegalArgumentException - if URL format is wrong
         */
        public Request setUrl(String url) throws IllegalArgumentException {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("invalid URL: " + url);
            }
            this.url = url;
            return this;
        }

        /**
         * @param key
         * @param value - we'll do URL encode or compose for multipart if needed
         * @return
         * @throws IllegalArgumentException - if key is empty or value is empty
         */
        public Request addQuery(String key, String value) throws IllegalArgumentException {
            if (Validator.isEmpty(key) || Validator.isEmpty(value)) {
                throw new IllegalArgumentException("invalid query, key[" + key + "] or value[" + value + "]");
            }
            if (queryStore == null) {
                queryStore = new HashMap<>();
            }
            queryStore.put(key, value);
            return this;
        }

        public Request addQuery(String key, int value) throws IllegalArgumentException {
            return addQuery(key, String.valueOf(value));
        }

        public Request addQuery(String key, boolean value) throws IllegalArgumentException {
            return addQuery(key, String.valueOf(value));
        }

        public Request setAuth(String password) throws IllegalArgumentException {
            if (Validator.isEmpty(password)) {
                throw new IllegalArgumentException("invalid password: null or empty");
            }
            String enPwd;
            try {
                enPwd = Base64.encodeToString(password.getBytes("UTF-8"), Base64.DEFAULT);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new IllegalArgumentException("invalid password: base64 failure");
            }
            return addHeader("Authorization", "Basic " + enPwd);
        }

        /**
         * @param key
         * @param value
         * @return
         * @throws IllegalArgumentException - if key is empty or value is empty
         */
        public Request addHeader(String key, String value) throws IllegalArgumentException {
            if (Validator.isEmpty(key) || Validator.isEmpty(value)) {
                throw new IllegalArgumentException("invalid header, key[" + key + "] or value[" + value + "]");
            }
            if (header == null) {
                header = new HashMap<>();
            }
            header.put(key, value);
            return this;
        }

        /**
         * default is {@link HttpUtil#DEFAULT_SOCKET_TIMEOUT}
         *
         * @param timeout - unit is millisecond
         * @return
         * @throws IllegalArgumentException - if timeout < {@link HttpUtil#TIMEOUT_VALID_MININUM}
         */
        public Request setSocketTimeout(int timeout) throws IllegalArgumentException {
            if (timeout < TIMEOUT_VALID_MININUM) {
                throw new IllegalArgumentException("invalid socket timeout:" + timeout);
            }
            this.socketTimeout = timeout;
            return this;
        }

        /**
         * default is {@link HttpUtil#DEFAULT_CONNECTION_TIMEOUT}
         *
         * @param timeout - unit is millisecond
         * @return
         * @throws IllegalArgumentException - if timeout < {@link HttpUtil#TIMEOUT_VALID_MININUM}
         */
        public Request setConnectionTimeout(int timeout) throws IllegalArgumentException {
            if (timeout < TIMEOUT_VALID_MININUM) {
                throw new IllegalArgumentException("invalid connection timeout:" + timeout);
            }
            this.socketTimeout = timeout;
            return this;
        }

        /**
         * default is {@link HttpUtil#DEFAULT_TRY_COUNT}
         *
         * @param tryCount
         * @return
         * @throws IllegalArgumentException - if tryCount < 1
         */
        public Request setTryCount(int tryCount) throws IllegalArgumentException {
            if (tryCount < 1) {
                throw new IllegalArgumentException("invalid try count:" + tryCount);
            }
            this.tryCount = tryCount;
            return this;
        }

        /**
         * @param cert
         * @return
         * @throws IllegalArgumentException - if certificate is empty
         */
        public Request setSSLCertificate(String cert) throws IllegalArgumentException {
            if (Validator.isEmpty(cert)) {
                throw new IllegalArgumentException("invalid SSL Cert:" + cert);
            }
            this.sslCert = cert;
            return this;
        }

        /**
         * @param hostnameVerifier - we'll use this instance
         * @throws IllegalArgumentException - if hostnameVerifier is null
         */
        public Request setHostnameVerifier(HostnameVerifier hostnameVerifier) throws IllegalArgumentException {
            if (Validator.isNull(hostnameVerifier)) {
                throw new IllegalArgumentException("hostname verifier is null");
            }
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * default is false
         *
         * @param isCollectHeader
         */
        public Request setCollectHeader(boolean isCollectHeader) {
            this.isCollectHeader = isCollectHeader;
            return this;
        }

        /**
         * @param paramName
         * @param contentType
         * @param file        - we'll use this instance
         * @return
         * @throws IllegalArgumentException - if method is GET, or paramName is empty, or contentType
         *                                  is empty, or file is null, or has no read permission.
         */
        public Request setFile(String paramName, String contentType, File file) throws IllegalArgumentException {
            if (method == GET) {
                throw new IllegalArgumentException("GET does not support file upload");
            }
            if (Validator.isEmpty(paramName)) {
                throw new IllegalArgumentException("invalid param name: " + paramName);
            }
            if (Validator.isEmpty(contentType)) {
                throw new IllegalArgumentException("invalid content type: " + contentType);
            }
            if (Validator.isNull(file) || !file.canRead()) {
                throw new IllegalArgumentException("file is null or has no read permission: " + file);
            }
            this.fileParamName = paramName;
            this.fileContentType = contentType;
            this.file = file;
            return this;
        }

        private String getMultipartQuery() {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, String> entry : queryStore.entrySet()) {
                sb.append(TWO_HYPHENS + BOUNDARY + CRLF);
                sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(CRLF).append(CRLF).append(entry.getValue()).append(CRLF);
            }
            return sb.toString();
        }

        private String getQuery() {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, String> entry : queryStore.entrySet()) {
                sb.append(entry.getKey()).append("=").append(EncUtil.urlEncode(entry.getValue())).append("&");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }

        private String buildQuery() {
            if (queryStore == null || queryStore.isEmpty()) {
                return "";
            }
            if (file != null) {
                return getMultipartQuery();
            } else {
                return getQuery();
            }
        }

        public Response execute() {
            query = buildQuery();
            switch (method) {
                case POST:
                    return post(this);
                case GET:
                    return get(this);
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            return "Request [method=" + method + ", url=" + url + ", queryStore=" + queryStore + ", header=" + header
                    + ", socketTimeout=" + socketTimeout + ", connectionTimeout=" + connectionTimeout + ", tryCount="
                    + tryCount + ", sslCert=" + sslCert + ", hostnameVerifier=" + hostnameVerifier
                    + ", isCollectHeader=" + isCollectHeader + ", file=" + file + ", fileParamName=" + fileParamName
                    + ", fileContentType=" + fileContentType + "]";
        }
    }

    public final static class Response {
        private int statusCode;
        private String body;
        private Map<String, List<String>> headers;
        private String headerString;
        private Throwable throwable;

        public List<String> getHeaderByKey(String key) {
            if (Validator.isNull(headers) || Validator.isNull(key)) {
                return null;
            } else {
                return headers.get(key);
            }
        }

        public String getHeaders() {
            if (headerString == null) {
                if (headers != null) {
                    StringBuilder sb = new StringBuilder();
                    String key;
                    for (Entry<String, List<String>> entry : headers.entrySet()) {
                        key = entry.getKey();
                        if (key != null) {
                            sb.append(entry.getKey()).append(":");
                        }
                        for (String value : entry.getValue()) {
                            sb.append(value).append(",");
                        }
                        if (!Validator.isEmpty(entry.getValue()) && sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        sb.append("\n");
                    }
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    headerString = sb.toString();
                }
            }
            return headerString;
        }

        public String getBody() {
            return body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isOk() {
            return statusCode == HttpURLConnection.HTTP_OK;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public String toString() {
            return "=== Response ["
                    + statusCode
                    + "]===\n== Header ==\n"
                    + getHeaders()
                    + "\n== Body ==\n"
                    + body
                    + "\n====================="
                    + ((throwable != null) ? "\nThrowable: " + throwable.getClass().getName() + " -- "
                    + throwable.getMessage() + "\n=====================" : "");
        }
    }

    private static Response get(Request req) {
        URL link = null;
        try {
            if (req.query != null) {
                link = new URL(req.url + "?" + req.query);
            } else {
                link = new URL(req.url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        assert link != null;

        Logcat.d(TAG, "Post - URL : " + link);

        HttpURLConnection conn = null;
        int tryCount = 0;
        int statusCode = 0;
        Map<String, List<String>> responseHeaders = null;
        String result = null;
        Throwable t = null;

        while (tryCount < req.tryCount) {
            Logcat.d(TAG, "GET - Try: " + (++tryCount) + "/" + req.tryCount);
            if (tryCount > 1) {
                statusCode = 0;
                responseHeaders = null;
                result = null;
                t = null;
            }
            try {
                conn = (HttpURLConnection) link.openConnection();
                conn.setReadTimeout(req.socketTimeout);
                conn.setConnectTimeout(req.connectionTimeout);
                if (req.header != null) {
                    for (Entry<String, String> reqHeaders : req.header.entrySet()) {
                        conn.setRequestProperty(reqHeaders.getKey(), reqHeaders.getValue());
                    }
                }
                conn.setRequestMethod("GET");

                setConnectionSSL(conn, req);
                setConnectionNotKeepAliveIfNeeded(conn);

                conn.connect();

                statusCode = conn.getResponseCode();
                if (req.isCollectHeader) {
                    responseHeaders = conn.getHeaderFields();
                }
                result = StreamUtil.convertStreamToString(conn.getInputStream(), "UTF-8");

            } catch (Throwable e) {
                e.printStackTrace();
                t = e;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    break;
                }
            }
        }

        Response response = new Response();
        response.statusCode = statusCode;
        response.throwable = t;
        response.headers = responseHeaders;
        response.body = result;
        return response;
    }

    private static Response post(Request req) {
        URL link = null;
        try {
            link = new URL(req.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        assert link != null;

        Logcat.d(TAG, "Post - URL : " + req.url);
        Logcat.d(TAG, "Post - Params : " + req.query);

        byte[] fileData = (req.file != null) ? FileUtil.convertFileToByteArray(req.file) : null;

        HttpURLConnection conn = null;
        int tryCount = 0;

        DataOutputStream writer = null;
        int statusCode = 0;
        Map<String, List<String>> responseHeaders = null;
        String result = null;
        Throwable t = null;

        long time;

        while (tryCount < req.tryCount) {
            Logcat.d(TAG, "Post - Try: " + (++tryCount) + "/" + req.tryCount);
            if (tryCount > 1) {
                writer = null;
                statusCode = 0;
                responseHeaders = null;
                result = null;
                t = null;
            }
            try {
                conn = (HttpURLConnection) link.openConnection();
                conn.setReadTimeout(req.socketTimeout);
                conn.setConnectTimeout(req.connectionTimeout);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Charset", "UTF-8");
                if (fileData != null) {
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                } else {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }

                if (req.header != null) {
                    for (Entry<String, String> reqHeaders : req.header.entrySet()) {
                        conn.setRequestProperty(reqHeaders.getKey(), reqHeaders.getValue());
                    }
                }

                setConnectionSSL(conn, req);
                setConnectionNotKeepAliveIfNeeded(conn);

                time = System.currentTimeMillis();
                conn.connect();
                Logcat.d(TAG, "Post - CONNECT OK, ms " + (System.currentTimeMillis() - time));

                time = System.currentTimeMillis();
                writer = new DataOutputStream(conn.getOutputStream());
                writer.writeBytes(req.query);
                if (fileData != null) {
                    Logcat.d(TAG, "Post - File length: " + fileData.length);
                    writer.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
                    writer.writeBytes("Content-Disposition: form-data; name=\"" + req.fileParamName + "\"; filename=\""
                            + req.file.getName() + "\"" + CRLF);
                    writer.writeBytes("Content-Type: " + req.fileContentType + CRLF);
                    writer.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
                    writer.writeBytes(CRLF);
                    writer.write(fileData);
                    writer.writeBytes(CRLF);
                    writer.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS);
                }
                writer.flush();
                Logcat.d(TAG, "Post - WRITE PARAMS OK, ms " + (System.currentTimeMillis() - time));

                time = System.currentTimeMillis();
                statusCode = conn.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    if (req.isCollectHeader) {
                        responseHeaders = conn.getHeaderFields();
                    }
                    result = StreamUtil.convertStreamToString(conn.getInputStream(), "UTF-8");
                    Logcat.d(TAG, "Post - READ RESPONSE OK, ms " + (System.currentTimeMillis() - time));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                t = e;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (statusCode == HttpURLConnection.HTTP_OK) {
                break;
            }
        } // == end of while-loop ==

        Response response = new Response();
        response.statusCode = statusCode;
        response.throwable = t;
        response.headers = responseHeaders;
        response.body = result;
        return response;
    }

    private static void setConnectionSSL(HttpURLConnection conn, Request req) {
        if (conn instanceof HttpsURLConnection) {
            SSLContext sslContext = getSSLContextWithCert(req.sslCert);
            if (!Validator.isNull(sslContext)) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            if (!Validator.isNull(req.hostnameVerifier)) {
                ((HttpsURLConnection) conn).setHostnameVerifier(req.hostnameVerifier);
            } else {
                ((HttpsURLConnection) conn).setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            }
        }
    }

    private static void setConnectionNotKeepAliveIfNeeded(HttpURLConnection conn) {
        if (Build.VERSION.SDK_INT > 13) {
            conn.setRequestProperty("Connection", "close");
        }
    }

    private static SSLContext getSSLContextWithCert(String cert) {
        if (Validator.isEmpty(cert)) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(cert.getBytes());
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(in);
            Logcat.d(TAG, "initSslFromCert - ca=" + ((X509Certificate) ca).getSubjectDN());

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Use default timeout settings: {@link #DEFAULT_SOCKET_TIMEOUT} and
     * {@link #DEFAULT_CONNECTION_TIMEOUT}
     *
     * @param url  - cannot be empty
     * @param file - cannot be null; we won't start download if no write
     *             permission
     * @return download result - this function won't throw exception, just
     * return false if there's invalid argument
     */
    public static boolean getFileFromUrl(String url, File file) {
        return getFileFromUrl(url, file, DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * @param url               - cannot be empty
     * @param file              - cannot be null; we won't start download if no write
     *                          permission
     * @param socketTimeout     - if less than {@link HttpUtil#TIMEOUT_VALID_MININUM}, we
     *                          won't start download
     * @param connectionTimeout - if less than {@link HttpUtil#TIMEOUT_VALID_MININUM}, we
     *                          won't start download
     * @return download result - this function won't throw exception, just
     * return false if there's invalid argument
     */
    public static boolean getFileFromUrl(String url, File file, int socketTimeout, int connectionTimeout) {
        if (Validator.isEmpty(url)) {
            return false;
        }
        if (Validator.isNull(file)) {
            return false;
        }
        if (socketTimeout < TIMEOUT_VALID_MININUM || connectionTimeout < TIMEOUT_VALID_MININUM) {
            return false;
        }

        boolean isSuccess = false;
        HttpURLConnection urlConnection = null;
        int statusCode;
        try {
            URL link = new URL(url);
            urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setReadTimeout(socketTimeout);
            urlConnection.setConnectTimeout(connectionTimeout);
            setConnectionNotKeepAliveIfNeeded(urlConnection);
            urlConnection.connect();

            statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                isSuccess = FileUtil.writeInputStreamToFile(urlConnection.getInputStream(), file);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return isSuccess;
    }
}
