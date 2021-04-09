package com.l.cloud.job.api;

import com.l.cloud.job.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/base")
public class BaseApi {

    @Autowired
    private BaseService baseService;

    @PostMapping("/register")
    public String register(@RequestBody String data){
        return baseService.registerV2(data);
    }

    @PostMapping("/heartCheck")
    public String heartCheck(@RequestBody String data){
        return baseService.heartCheck(data);
    }

    @PostMapping("/serviceDestroy")
    public String serviceDestroy(@RequestBody String data){
        return baseService.serviceDestroy(data);
    }

}
