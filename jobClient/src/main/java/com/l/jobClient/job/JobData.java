package com.l.jobClient.job;

import com.l.jobClient.annotation.JobSelf;
import com.l.jobClient.exceptions.UniqueRepeatException;
import com.l.jobClient.util.HttpUtil;
import com.l.jobClient.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class JobData implements InitializingBean {

    @Autowired
    private ApplicationContext applicationContext;

    private ConcurrentHashMap<String, HashMap<String, String>> jobMethod = new ConcurrentHashMap<>();

    @Value("${job.server.url:127.0.0.1}")
    private String jobServerUrl;

    @Value("${spring.application.name}")
    private String serverName;

    @Value("${server.port:80}")
    private String serverPort;

    // 是否注册成功
    private boolean registerSuccess = false;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    ScheduledFuture<?> scheduledFuture = null;

    @Override
    public void afterPropertiesSet(){

        if(!serverName.equals("job")){

            try {
                // 根据注解获取bean
                String[] classes = applicationContext.getBeanNamesForAnnotation(Service.class);

                if(classes.length > 0) {

                    for (String clazz : classes) {

                        // 从spring 上下文中获取类，否则属性会注入不进去
                        Class c = applicationContext.getBean(clazz).getClass();

                        String className = c.getName();

                        // 获取类中的所有方法
                        Method[] methods = c.getMethods();

                        HashMap<String, String> class_method = null;
                        // 循环method
                        for (Method method : methods) {

                            // 判断是否含有注解
                            JobSelf jobSelf = method.getAnnotation(JobSelf.class);

                            // 有注解就把信息保存下来
                            if (jobSelf != null) {

                                if (jobMethod.contains(jobSelf.value())) {
                                    throw new UniqueRepeatException("注解值重复：" + jobSelf.value());
                                }

                                class_method = new HashMap<>();
                                class_method.put("className", className);
                                class_method.put("methodName", method.getName());
                                jobMethod.put(jobSelf.value(), class_method);
                            }

                        }

                    }

                    Map<String, Object> requestParam = new HashMap<>();
                    requestParam.put("serverName", serverName);
//                    requestParam.put("class_method", jobMethod);
                    String address = getLocalInetAddress();
                    requestParam.put("address", address+":"+serverPort);

                    registerJobMsg(requestParam);

                    if(!registerSuccess){
                        this.scheduledRegister(requestParam);
                    }

                }

            }catch (UniqueRepeatException e){
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 功能描述:  定时取调用注册
     * @Author  lyd
     * @Date  2021/3/11 17:05
     * @return
     **/
    public void scheduledRegister(Map<String, Object> requestParam){
        try {

            Object c = applicationContext.getBean(Class.forName("com.l.jobClient.job.JobData"));

            Method method = this.getClass().getMethod("registerJobMsg", Map.class);

            scheduledFuture = threadPoolTaskScheduler.schedule(()->{

                // 调用方法
                try {
                    method.invoke(c, requestParam);
                } catch (InvocationTargetException | IllegalAccessException exception){
                    exception.printStackTrace();
                    log.error("方法调用失败：{}", exception.getMessage());
                }

                // 触发器
            }, new CronTrigger("0/10 * * * * ? "));

        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述:  注册到任务调度中心
     * @Author  lyd
     * @Date  2021/3/11 17:36
     * @return
     **/
    public String registerJobMsg(Map<String, Object> requestParam){

        String result = HttpUtil.postByJson(jobServerUrl + "/base/register", JsonUtil.objectToJson(requestParam));

        log.info("result = {}", result);

        if(!result.equals("success")){
            log.error(">>> register job failed {}", result);
            return "fail";
        }

        registerSuccess = true;

        if(registerSuccess){
            if(scheduledFuture != null){
                scheduledFuture.cancel(true);
            }
            log.info(">>> register job success");

                scheduledHeartCheck();
            return "success";
        }

        return "success";
    }


    /**
     * 功能描述:  定时健康检查
     * @Author  lyd
     * @Date  2021/3/11 17:38
     * @return
     **/
    public void scheduledHeartCheck(){

        try {

            Object c = applicationContext.getBean(Class.forName("com.l.jobClient.job.JobData"));

            Method method = this.getClass().getMethod("heartCheck");

            ScheduledFuture heartCheckScheduledFuture = threadPoolTaskScheduler.schedule(()->{

                // 调用方法
                try {
                    method.invoke(c);
                } catch (InvocationTargetException | IllegalAccessException exception){
                    exception.printStackTrace();
                    log.error("方法调用失败：{}", exception.getMessage());
                }

                // 触发器
            }, new PeriodicTrigger(30000, TimeUnit.MILLISECONDS));

        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * 功能描述:  心跳检查
     * @Author  lyd
     * @Date  2021/3/15 11:50
     * @return
     **/
    public void heartCheck(){

        Map<String, String> requestParam = new HashMap<>();
        requestParam.put("serverName", serverName);

        // 获取本地地址
        try {
            String address = getLocalInetAddress();
            requestParam.put("url", address+":"+serverPort);

            // 发送请求  服务名称+本地地址
            String result = HttpUtil.postByJson(jobServerUrl + "/base/heartCheck", JsonUtil.objectToJson(requestParam));

            if(!result.equals("success")){
                log.error(">>>>>> heart check failed, server error message = {}", result);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            requestParam.put("url", null);
            log.error(">>>>>>> heart check failed exception in getting local address! {}", e.getMessage());
        }

    }

    /**
     * 功能描述: 获取本地ip地址
     * @Author  lyd
     * @Date  2021/3/23 9:45
     * @return
     **/
    private String getLocalInetAddress() throws UnknownHostException {

        InetAddress address = InetAddress.getLocalHost();

        String hostAddress = address.getHostAddress();

        return hostAddress;
    }

    /**
     * 功能描述: 获取注解信息
     * @Author  lyd
     * @Date  2021/3/23 14:28
     * @return
     **/
    public ConcurrentHashMap getJobMethod(){
        return this.jobMethod;
    }

}
