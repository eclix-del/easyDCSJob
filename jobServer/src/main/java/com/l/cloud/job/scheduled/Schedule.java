package com.l.cloud.job.scheduled;

import com.l.cloud.job.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Schedule {

    @Autowired
    private BaseService baseService;

    /**
     * 功能描述: 定时检查已注册实例
     * @Author  lyd
     * @Date  2021/3/15 14:57
     * @return
     **/
    @Scheduled(fixedRate = 30000)
    public void checkConnectServices(){

        ConcurrentHashMap<String, Map<String, Date>> services = baseService.getConnectServices();

        if(!services.isEmpty()){

            for(Map.Entry<String, Map<String, Date>> mapEntry : services.entrySet()){

                Map<String, Date> urlDateMap = mapEntry.getValue();

                if(!urlDateMap.isEmpty()){

                    for (Map.Entry<String, Date> entry: urlDateMap.entrySet()){

                        if(getDiff(entry.getValue()) > 30000){
                            baseService.removeInstances(mapEntry.getKey(), entry.getKey());
                            log.info("remove time out instance {} ", entry.getKey());
                        }

                    }

                }

            }

        }

    }

    /**
     * 功能描述: 计算时间差
     * @Author  lyd
     * @Date  2021/3/15 15:04
     * @return
     **/
    private long getDiff(Date date){
        long diff = new Date().getTime() - date.getTime();
        return diff;
    }

}
