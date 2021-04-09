package com.l.jobClient.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class HttpUtil {

    public static String postByJson(String url,String json) {
        String result = "";
        HttpPost post = new HttpPost(url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig =      RequestConfig.custom().setConnectionRequestTimeout(300000)
                .setSocketTimeout(300000).setConnectTimeout(300000).build();

        post.setHeader("Content-Type", "application/json;charset=utf-8");
        StringEntity postingString = new StringEntity(json.toString(), "utf-8");
        post.setEntity(postingString);
        post.setConfig(requestConfig);
        HttpResponse response = null;
        try {
            response = httpClient.execute(post);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            result = e1.getMessage();
            return result;
        }
        if(response!=null) {
            try (InputStream in = response.getEntity().getContent();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            ){
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    strber.append(line);
                }

                result = strber.toString();
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    result = "response status is " + response.getStatusLine().getStatusCode();
                }
//                log.info("请求状态-->" + response.getStatusLine().getStatusCode());
            } catch (Exception e) {
                log.error("请求异常:{}",e.getMessage(),e);
            } finally {
                post.abort();
            }
        }else {
            result = "服务器异常";
            log.error("请求异常:response响应未空");
        }
        return result;
    }
}
