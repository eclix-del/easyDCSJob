package com.l.cloud.job.api;

import com.l.cloud.common.annotation.RequirePermission;
import com.l.cloud.common.annotation.ValidNumber;
import com.l.cloud.common.annotation.ValidString;
import com.l.cloud.common.entity.ResultEntity;
import com.l.cloud.job.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "任务管理")
@RestController
@RequestMapping("/task")
@RequirePermission("job_task")
public class TaskApi {

    @Autowired
    private TaskService taskService;

    @RequirePermission("getList")
    @ApiOperation("查询列表数据")
    @GetMapping("/getList")
    public ResultEntity getList(@RequestParam(value = "currentPage", required = false) Integer currentPage, @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                @ValidString(value = "handler", require = false) String handler, @ValidString(value = "name", require = false) String name,
                                @RequestParam(value = "status", required = false) Integer status, @ValidString(value = "jobHandler", require = false) String jobHandler){
        return taskService.getList(currentPage, pageSize, handler, name, status, jobHandler);
    }

    @ApiOperation("添加任务")
    @RequirePermission("add")
    @PostMapping("/add")
    public ResultEntity add(String handler, String name, String annotationValue, String expressionValue, String expressionType, Long id){
        return taskService.add(handler, name, annotationValue, expressionValue, expressionType, id);
    }

    @ApiOperation("删除任务")
    @RequirePermission("delete")
    @PostMapping("/delete")
    public ResultEntity delete(@ValidNumber(value = "id", require = true) Long id){
        return taskService.delete(id);
    }

    @ApiOperation("执行一次")
    @RequirePermission("runNow")
    @PostMapping("/runNow")
    public ResultEntity runNow(String serverName, String handlerValue){
        return taskService.runNowV2(serverName, handlerValue);
    }

    @ApiOperation("启动任务")
    @RequirePermission("start")
    @PostMapping("/start/{id}")
    public ResultEntity addScheduled(@PathVariable Long id){
        return taskService.addScheduled(id);
    }

    @ApiOperation("停止任务")
    @RequirePermission("stop")
    @PostMapping("/stop/{id}")
    public ResultEntity removeScheduled(@PathVariable Long id){
        return taskService.removeScheduled(id);
    }

}
