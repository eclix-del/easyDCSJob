package com.l.jobClient.api;

import com.l.jobClient.job.ScheduledJob;
import com.l.jobClient.util.ResultEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "任务调度接口")
@RestController
@RequestMapping("/job")
public class JobHandlerApi {

    @Autowired
    private ScheduledJob scheduledJob;

    /**
     * 功能描述: 执行任务
     * @Author  lyd
     * @Date  2021/3/11 14:11
     * @return
     **/
    @ApiOperation("执行一次")
    @PostMapping("/invoke")
    public String invoke(@RequestBody String data){
        return scheduledJob.runNowV2(data);
    }

    /**
     * 功能描述: 添加定时任务
     * @Author  lyd
     * @Date  2021/3/11 17:57
     * @return
     **/
    @ApiOperation("设置定时任务")
    @PostMapping("/addSchedule")
    public ResultEntity addSchedule(@RequestBody String data){
        return scheduledJob.addThreadSchedule(data);
    }

    @ApiOperation("移除定时任务")
    @PostMapping("/removeSchedule")
    public ResultEntity removeSchedule(@RequestBody String data){
        return scheduledJob.removeSchedule(data);
    }

}
