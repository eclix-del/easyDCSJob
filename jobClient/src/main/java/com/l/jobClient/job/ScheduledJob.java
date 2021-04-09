package com.l.jobClient.job;

import com.l.jobClient.annotation.JobSelf;
import com.l.jobClient.util.JsonUtil;
import com.l.jobClient.util.ResultEntity;
import com.l.jobClient.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class ScheduledJob {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    ApplicationContext applicationContext;

    // 创建的定时任务对象map
    private ConcurrentHashMap<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap();

    @Autowired
    private JobData jobData;

    /**
     * 功能描述:  增加定时任务
     * @Author  lyd
     * @Date  2021/3/11 17:46
     * @return
     **/
    public ResultEntity addThreadSchedule(String data){

        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        String expressionValue = dataMap.get("expressionValue");
        String expressionType = dataMap.get("expressionType");

        Trigger trigger = null;

        // 判读触发器类型是cron还是fixed
        if(expressionType.equals("cron")){
            trigger = new CronTrigger(expressionValue);
        }else if(expressionType.equals("fixed")){
            trigger = new PeriodicTrigger(Long.valueOf(expressionValue));
        }else{
            return ResultUtil.error("触发器类型错误，只支持cron和fixed");
        }

        String annotationValue = dataMap.get("handlerValue");


        try {

            ConcurrentHashMap<String, HashMap<String, String>> jobMethod = jobData.getJobMethod();

            HashMap<String, String> class_method = jobMethod.get(annotationValue);

            String className = class_method.get("className");
            String methodName = class_method.get("methodName");

            // 获取bena实例
            Object obj = applicationContext.getBean(Class.forName(className));

            Class c = obj.getClass();

            Method[] methods = c.getMethods();

            Method m = null;
            for (Method method : methods){
                if(method.getName().equals(methodName)){
                    m = method;
                    break;
                }
            }

            if(m != null){

                Method finalM = m;
                ScheduledFuture scheduledFuture =  threadPoolTaskScheduler.schedule(()->{
                    try {
                        finalM.invoke(obj);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }, trigger);

                scheduledFutureMap.put(className+ "_" + methodName, scheduledFuture);

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return ResultUtil.success("success");
    }


    /**
     * 功能描述: 执行一次
     * @Author  lyd
     * @Date  2021/3/11 17:55
     * @return
     **/
    public String runNow(String data){
        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        String className = dataMap.get("className");
        String methodName = dataMap.get("methodName");
        String annotationValue = dataMap.get("annotationValue");

        try {

            // 根据类名获取类
            Object c = applicationContext.getBean(Class.forName(className));

            // 获取方法信息
            Method[] methods =  c.getClass().getMethods();

            for (Method m : methods){

                // 没有注解直接pass
                JobSelf jobSelf = m.getAnnotation(JobSelf.class);

                if(jobSelf == null){
                    continue;
                }

                // 方法名匹配并且注解值匹配
                if(m.getName().equals(methodName) && jobSelf.value().equals(annotationValue)){
                    // 执行方法并且跳出循环
                    m.invoke(c);
                    return "success";
                }

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "fail";
    }

    /**
     * 功能描述: 立刻执行任务
     * @Author  lyd
     * @Date  2021/3/23 14:48
     * @return
     **/
    public String runNowV2(String data){

        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        String annotationValue = dataMap.get("handlerValue");

        try {

            ConcurrentHashMap<String, HashMap<String, String>> jobMethod = jobData.getJobMethod();

            HashMap<String, String> class_method = jobMethod.get(annotationValue);

            String className = class_method.get("className");
            String methodName = class_method.get("methodName");

            // 根据类名获取类
            Object c = applicationContext.getBean(Class.forName(className));

            // 获取方法信息
            Method[] methods = c.getClass().getMethods();

            for (Method m : methods){

                // 没有注解直接pass
                JobSelf jobSelf = m.getAnnotation(JobSelf.class);

                if(jobSelf == null){
                    continue;
                }

                // 方法名匹配并且注解值匹配
                if(m.getName().equals(methodName) && jobSelf.value().equals(annotationValue)){
                    // 执行方法并且跳出循环
                    m.invoke(c);
                    return "success";
                }

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "fail";
    }

    /**
     * 功能描述: 移除定时任务
     * @Author  lyd
     * @Date  2021/3/26 10:42
     * @return
     **/
    public ResultEntity removeSchedule(String data) {

        Map<String, String> dataMap = JsonUtil.jsonToObject(data, HashMap.class);

        String annotationValue = dataMap.get("handlerValue");

        ConcurrentHashMap<String, HashMap<String, String>> jobMethod = jobData.getJobMethod();

        HashMap<String, String> class_method = jobMethod.get(annotationValue);

        String className = class_method.get("className");
        String methodName = class_method.get("methodName");

        // 判断是否有定时任务
        if(scheduledFutureMap.containsKey(className + "_" + methodName)){
            ScheduledFuture scheduledFuture = scheduledFutureMap.get(className + "_" + methodName);
            scheduledFuture.cancel(true);
        }

        return ResultUtil.success("success");
    }
}
