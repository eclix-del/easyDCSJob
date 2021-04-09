package com.l.cloud.job.service;

import com.l.cloud.common.bean.Constants;
import com.l.cloud.common.entity.ResultEntity;
import com.l.cloud.common.util.JsonUtil;
import com.l.cloud.common.util.ResultUtil;
import com.l.cloud.common.util.StringUtil;
import com.l.cloud.job.dao.TaskDao;
import com.l.cloud.job.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private TaskDao taskDao;

    private Integer defaultCurrentPage = 1;

    private Integer defaultPageSize = 10;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BaseService baseService;

    /**
     * 功能描述: 列表数据
     * @Author  lyd
     * @Date  2021/3/22 14:45
     * @return
     **/
    public ResultEntity getList(Integer currentPage, Integer pageSize, String handler, String name, Integer status, String jobHandler){

        if(currentPage == null){
            currentPage = defaultCurrentPage;
        }

        if(pageSize == null){
            pageSize = defaultPageSize;
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, sort);

        Specification specification = (root, criteriaQuery, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.equal(root.get("flag"), Constants.NORMAL));

            if(StringUtil.isNotEmpty(handler)){
                predicates.add(criteriaBuilder.equal(root.get("handler"), handler));
            }

            if(StringUtil.isNotEmpty(name)){
                predicates.add(criteriaBuilder.equal(root.get("name"), name));
            }

            if(StringUtil.isNotEmpty(status)){
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if(StringUtil.isNotEmpty(jobHandler)){
                predicates.add(criteriaBuilder.equal(root.get("annotationValue"), jobHandler));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };

        Page<Task> taskPage = taskDao.findAll(specification, pageable);

        Map< String, Object> result = new HashMap<>();
        result.put("totalSize", taskPage.getTotalElements());
        result.put("list", taskPage.getContent());


        return ResultUtil.success(result);

    }

    /**
     * 功能描述: 保存任务信息
     * @Author  lyd
     * @Date  2021/3/22 14:52
     * @return
     **/
    @Transactional
    public ResultEntity add(String handler, String name, String annotationValue, String expressionValue, String expressionType, Long id) {

        Task task = new Task();
        task.setHandler(handler);
        task.setName(name);
        task.setAnnotationValue(annotationValue);
        task.setExpressionValue(expressionValue);
        task.setExpressionType(expressionType);
        task.setId(id);

        taskDao.save(task);

        return ResultUtil.success("保存成功");
    }

    /**
     * 功能描述:  删除
     * @Author  lyd
     * @Date  2021/3/22 17:55
     * @return
     **/
    @Transactional
    public ResultEntity delete(Long id) {
        taskDao.putFlagById(Constants.DELETE, id);

        removeScheduled(id);

        // TODO 发送删除定时任务请求至客户端

        return ResultUtil.success("删除成功");
    }



    /**
     * 功能描述: 创建定时任务
     * @Author  lyd
     * @Date  2021/3/25 17:17
     * @return
     **/
    @Transactional
    public ResultEntity addScheduled(Long id) {

        try {

            Optional<Task> taskOptional = taskDao.findById(id);

            if(!taskOptional.isPresent()){
                return ResultUtil.error("任务调度失败");
            }

            Task task = taskOptional.get();

            task.getHandler();
            task.getAnnotationValue();

            Map<String, String> params = new HashMap<>();
            params.put("handlerValue", task.getAnnotationValue());
            params.put("expressionValue", task.getExpressionValue());
            params.put("expressionType", task.getExpressionType());

            String result = restTemplate.postForObject("http://"+ task.getHandler() + "/job/addSchedule", JsonUtil.objectToJson(params), String.class);

            log.info("result ==={} ", result);

            ResultEntity resultEntity = JsonUtil.jsonToObject(result, ResultEntity.class);

            if(resultEntity.getCode() == 200){
                task.setStatus(Constants.RUNNING);
                taskDao.save(task);
                return ResultUtil.success("success");
            }

            return ResultUtil.error("fail");
        }catch (Exception e){
            log.error("添加定时任务时异常==={}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.error(e.getMessage());
        }

    }

    /**
     * 功能描述: 移除定时任务
     * @Author  lyd
     * @Date  2021/3/26 10:08
     * @return
     **/
    @Transactional
    public ResultEntity removeScheduled(Long id) {

        try {

            Optional<Task> taskOptional = taskDao.findById(id);

            if(!taskOptional.isPresent()){
                return ResultUtil.error("任务调度失败");
            }

            Task task = taskOptional.get();

            task.getHandler();
            task.getAnnotationValue();

            Map<String, String> params = new HashMap<>();
            params.put("handlerValue", task.getAnnotationValue());
            params.put("expressionValue", task.getExpressionValue());
            params.put("expressionType", task.getExpressionType());

            String result = restTemplate.postForObject("http://"+ task.getHandler() + "/job/removeSchedule", JsonUtil.objectToJson(params), String.class);

            log.info("result === {} ", result);

            ResultEntity resultEntity = JsonUtil.jsonToObject(result, ResultEntity.class);

            if(resultEntity.getCode() == 200){
                task.setStatus(Constants.STOP);
                taskDao.save(task);
                return ResultUtil.success("success");
            }

            return ResultUtil.error("fail");
        }catch (Exception e){
            log.error("添加定时任务时异常==={}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.error(e.getMessage());
        }

    }


    /**
     * 功能描述: 执行一次
     * @Author  lyd
     * @Date  2021/3/23 11:34
     * @return
     **/
    public ResultEntity runNowV2(String serverName, String handlerValue){

        Map<String, Set<String>> registerInstances =  baseService.getRegisterInstances();

        if(!registerInstances.containsKey(serverName)){
            return ResultUtil.error("执行器未挂载");
        }

        if(registerInstances.get(serverName).isEmpty()){
            return ResultUtil.error("执行器当前可用实例为空");
        }

        Map<String, String> params = new HashMap<>();

        params.put("handlerValue", handlerValue);

        String result = restTemplate.postForObject("http://"+ serverName + "/job/invoke", JsonUtil.objectToJson(params), String.class);

        log.info("接口调用结果 = {}", result);

        return ResultUtil.success(result);
    }
}
