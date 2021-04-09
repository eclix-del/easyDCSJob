package com.l.jobClient.annotation;

import com.l.jobClient.job.JobInit;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 是否开启任务调度功能
 * @Author  lyd
 * @Date  2021/3/22 14:16
 * @return
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JobInit.class)
public @interface EnableJobScheduled {
}
