package com.l.cloud.job.service;

import com.l.cloud.common.bean.Constants;
import com.l.cloud.common.entity.ResultEntity;
import com.l.cloud.common.util.JsonUtil;
import com.l.cloud.common.util.ResultUtil;
import com.l.cloud.common.util.StringUtil;
import com.l.cloud.job.dao.TaskDao;
import com.l.cloud.job.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseService {

    @Autowired
    private RestTemplate restTemplate;

    private Map<String, Map<String, Map<String, String>>> jobs = new HashMap<>();

    private Map<String, Set<String>> registerInstances = new HashMap<>();

    private ConcurrentHashMap<String, Map<String, Date>> connectServices = new ConcurrentHashMap<>();

    /**
     * 功能描述: 服务注册
     * @Author  lyd
     * @Date  2021/3/15 14:54
     * @return
     **/
    @Deprecated
    public String register(String data){
        log.info("接收到注册请求参数：{}", data);

        if(data == null){
            return "success";
        }

        //
        Map<String, Object> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        // 服务名
        String serverName = StringUtil.getString(dataMap.get("serverName"));

        Map<String, Map<String, String>> class_method = (Map<String, Map<String, String>>) dataMap.get("class_method");

        jobs.put(serverName, class_method);

        return "success";
    }

    /**
     * 功能描述: 新版服务注册
     * @Author  lyd
     * @Date  2021/3/23 9:57
     * @return
     **/
    public String registerV2(String data){

        // 解析收到的数据
        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        // 服务名称
        String serverName = dataMap.get("serverName");

        // 服务地址
        String address = dataMap.get("address");

        // 如果这个服务有注册过的信息
        if(registerInstances.containsKey(serverName)){
            Set<String> onlineAddress = registerInstances.get(serverName);
            onlineAddress.add(address);
        }else{
            Set<String> onlineAddress = new HashSet<>();
            onlineAddress.add(address);
            registerInstances.put(serverName, onlineAddress);
        }

        return "success";
    }


    /**
     * 功能描述: 心跳检查
     * @Author  lyd
     * @Date  2021/3/15 12:16
     * @return
     **/
    public String heartCheck(String data){

        log.info("接收到心跳请求参数：{}", data);

        if(data == null){
            return "fail";
        }

        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        // 解析请求参数
        String serverName = dataMap.get("serverName");
        String url = dataMap.get("url");

        // 判断路径是否为空
        if(url == null || url.trim().equals("")){
            return "heart check failed, the service url must not be null";
        }

        // 判断是否有键
        if(connectServices.containsKey(serverName)){

            // 获取实例列表
            Map<String, Date> urls = connectServices.get(serverName);

            // 首次收到心跳信息
            if(urls == null){
                urls = new HashMap<>();
                urls.put(url, new Date());

                // 不是首次但值是空的
            }else if(urls.isEmpty()){
                urls.put(url, new Date());
            }else{

                // 更新时间
                if(urls.containsKey(url)){
                    urls.put(url, new Date());
                }
            }

        }else{

            // 创建map 保存服务实例信息
            Map<String, Date> servicesMap = new HashMap<>();
            servicesMap.put(url, new Date());
            connectServices.put(serverName, servicesMap);
        }

        // 判断接收到的心跳信息是否在注册列表里
//        if(!registerInstances.containsKey(serverName)){

            Set<String> address = registerInstances.get(serverName);

            // 服务地址为空
            if(address == null || address.isEmpty()){
                address = new HashSet<>();
            }
            address.add(url);

            registerInstances.put(serverName, address);
//        }


        return "success";
    }


    /**
     * 功能描述: 立刻执行一次
     * @Author  lyd
     * @Date  2021/3/15 14:54
     * @return
     **/
    public String runNow(String serverName, String handlerValue){

        if(!jobs.containsKey(serverName)){
            return "fail";
        }

        // 获取服务启动是注册的信息
        Map<String, Map<String, String>> serverJob = jobs.get(serverName);

        // 注解所在的类和方法
        Map<String, String> class_method = serverJob.get(handlerValue);

        Map<String, String> params = new HashMap<>();
        params.putAll(class_method);
        params.put("annotationValue", handlerValue);

        String result = restTemplate.postForObject("http://"+ serverName + "/job/invoke", JsonUtil.objectToJson(params), String.class);

        log.info("接口调用结果 = {}", result);

        return result;
    }

    /**
     * 功能描述: 获取当前连接服务实例
     * @Author  lyd
     * @Date  2021/3/15 14:51
     * @return
     **/
    public ConcurrentHashMap<String, Map<String, Date>> getConnectServices(){
        return this.connectServices;
    }

    /**
     * 功能描述: 获取注册实例信息
     * @Author  lyd
     * @Date  2021/3/23 11:04
     * @return
     **/
    public Map<String, Set<String>> getRegisterInstances(){
        return this.registerInstances;
    }

    /**
     * 功能描述: 服务销毁立即移除心跳信息
     * @Author  lyd
     * @Date  2021/3/15 15:35
     * @return
     **/
    public String serviceDestroy(String data) {

        log.info("接收到服务销毁请求参数：{}", data);

        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        String serverName = dataMap.get("serverName");

        String url = dataMap.get("url");

        removeInstances(serverName, url);

        return "success";
    }

    /**
     * 功能描述: 移除实例信息
     * @Author  lyd
     * @Date  2021/3/23 10:44
     * @return
     **/
    public void removeInstances(String serverName, String url){

        Map<String, Date> serverMap = connectServices.get(serverName);

        if(serverMap != null && !serverMap.isEmpty()){

            for (String key : serverMap.keySet()){

                if(key.equals(url)){
                    serverMap.remove(key);
                }
            }

        }

        // 移除保存的信息
        Set<String> onlineAddress = registerInstances.get(serverName);

        if(onlineAddress != null && !onlineAddress.isEmpty()){
            onlineAddress.remove(url);
        }

    }
}
