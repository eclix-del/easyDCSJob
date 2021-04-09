package com.l.jobClient.job;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.l.jobClient"})
public class JobInit {

    public JobInit(){
        System.out.println("开启任务调度");
    }

}
