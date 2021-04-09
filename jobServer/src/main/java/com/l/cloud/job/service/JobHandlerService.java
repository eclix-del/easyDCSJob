package com.l.cloud.job.service;

import com.l.cloud.common.entity.ResultEntity;
import com.l.cloud.common.util.ResultUtil;
import com.l.cloud.job.entity.JobGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobHandlerService {

    @Autowired
    private BaseService baseService;

    /**
     * 功能描述: 获取执行器列表
     * @Author  lyd
     * @Date  2021/3/16 17:25
     * @return
     **/
    public ResultEntity getList(){

        // 获取当前有心跳的实例
        Map<String, Set<String>> instances = baseService.getRegisterInstances();

        // 执行器列表
        List<JobGroup> groups = new ArrayList<>();

        // 循环取出实例
        for (Map.Entry<String, Set<String>> service : instances.entrySet()){

            JobGroup jobGroup = new JobGroup();
            jobGroup.setAppName(service.getKey());
            jobGroup.setOnlineAddress(service.getValue());

            groups.add(jobGroup);

        }

        return ResultUtil.success(groups);
    }

}
