package com.l.cloud.job.entity;

import lombok.Data;

import java.util.Set;

/**
 * 功能描述: 执行器(服务列表)
 * @Author  lyd
 * @Date  2021/3/16 16:52
 * @return
 **/
@Data
public class JobGroup{

    private String appName;

    private String name;

    private Set<String> onlineAddress;

}
