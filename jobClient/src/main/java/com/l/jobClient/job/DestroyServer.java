package com.l.jobClient.job;

import com.l.jobClient.util.HttpUtil;
import com.l.jobClient.util.JsonUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DestroyServer implements DisposableBean {

    @Value("${job.server.url:127.0.0.1}")
    private String jobServerUrl;

    @Value("${spring.application.name}")
    private String serverName;

    @Value("${server.port:80}")
    private String serverPort;

    @Override
    public void destroy() throws Exception{

        System.out.println("服务销毁");
        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("serverName", serverName);

        // 获取本地地址
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            requestParam.put("url", address.getHostAddress()+":"+serverPort);

            // 发送请求  服务名称+本地地址
            HttpUtil.postByJson(jobServerUrl + "/base/serviceDestroy", JsonUtil.objectToJson(requestParam));

        } catch (UnknownHostException e) {
            e.printStackTrace();
            requestParam.put("url", null);
        }

    }

}
