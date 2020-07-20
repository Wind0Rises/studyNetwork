package com.liu.study.network.httpclient.first;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author lwa
 * @version 1.0.0
 * @createTime 2020/7/18 14:21
 */
public class FirstTest {

    /**
     * httpClient：只有一种方法，execute(..)，其有很多重载方法。
     */
    public static void main(String[] args) throws Exception {
        // firstMethod();
        secondMethod();
    }

    public static void firstMethod() throws IOException {
        CloseableHttpClient  httpClient = HttpClientBuilder.create().build();

        HttpEntity httpEntity = new BasicHttpEntity();
        HttpPost post = new HttpPost();
        post.setURI(URI.create("http://baidu.com"));

        // Header header = new BasicHeader("content-type", "application/json;cha");
        // post.setHeader(header);

        HttpResponse httpResponse = httpClient.execute(post);

        InputStream inputStream = httpResponse.getEntity().getContent();

        byte[] result = new byte[1024];
        while(inputStream.read(result) != -1) {
            String str = new String(result);
            System.out.println(str);
        }
    }

    public static void secondMethod() throws Exception {
        String result = HttpClientUtils.postRequest("http://baidu.com", null, null);
        System.out.println(result);
    }

}
