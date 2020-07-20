package com.liu.study.network.httpclient.first;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.CharsetUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author lwa
 * @version 1.0.0
 * @createTime 2020/7/18 16:03
 */
public class HttpClientUtils {

    private final static String DEFAULT_CHARSET = "UTF-8";

    private HttpClientUtils() {

    }

    /**
     * 默认字符编码的post请求。
     *
     * @param url
     * @param content
     * @return
     * @throws Exception
     */
    public static String postRequest(String url, String content, String contentType) throws Exception {
        return postRequest(url, content, null, Charset.forName(DEFAULT_CHARSET), contentType);
    }

    /**
     * 指定字符编码的post请求。
     * @param url
     * @param content
     * @param charsetStr
     * @return
     * @throws Exception
     */
    public static String postRequest(String url, String content, String charsetStr, String contentType) throws Exception {
        return postRequest(url, content, null, Charset.forName(charsetStr), contentType);
    }

    /**
     * 指定字符编码的post请求。
     * @param url
     * @param content
     * @param charset
     * @return
     * @throws Exception
     */
    public static String postRequest(String url, String content, Charset charset, String contentType) throws Exception {
        return postRequest(url, content, null, charset, contentType);
    }

    /**
     * post请求。输入时Map。
     * @param url
     * @param content
     * @param headerMap
     * @param charset
     * @return
     * @throws Exception
     */
    public static String postRequest(String url, String content, Map<String, String> headerMap, Charset charset, String contentType) throws Exception {

        // 01、请求准备阶段
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);
        if (headerMap != null && headerMap.size() > 0) {
            headerMap.forEach((key, value) -> {
                httpPost.setHeader(key, value);
            });
        }

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
        httpPost.setConfig(requestConfig);

        HttpEntity httpEntity = new StringEntity(content, contentType);
        httpPost.setEntity(httpEntity);

        // 02、请求操作
        HttpResponse httpResponse = httpClient.execute(httpPost);

        // 03、响应操作。
        InputStream inputStream = null;
        try {
            inputStream = httpResponse.getEntity().getContent();
            return  readDataForInputStream(inputStream, charset);
        } finally {
            if (inputStream != null)  {
                inputStream.close();
            }
        }

    }

    /**
     * 从InputStream中读取数据。
     * @param inputStream
     * @param charset
     * @return
     * @throws IOException
     */
    private static String readDataForInputStream(InputStream inputStream, Charset charset) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();

        Reader in = null;
        try{
            in = new InputStreamReader(inputStream, charset);
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return out.toString();
    }

}
