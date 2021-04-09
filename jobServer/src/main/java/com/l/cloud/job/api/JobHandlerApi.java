package com.l.cloud.job.api;

import com.l.cloud.common.annotation.RequirePermission;
import com.l.cloud.common.entity.ResultEntity;
import com.l.cloud.job.service.JobHandlerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "执行器管理")
@RestController
@RequestMapping("/handler")
@RequirePermission("job_handler")
public class JobHandlerApi {

    @Autowired
    private JobHandlerService jobHandlerService;

    @ApiOperation("查询列表数据")
    @GetMapping("/getList")
    @RequirePermission("getList")
    public ResultEntity getList(){
        return jobHandlerService.getList();
    }

}
