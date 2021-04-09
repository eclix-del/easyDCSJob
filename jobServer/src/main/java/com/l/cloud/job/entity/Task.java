package com.l.cloud.job.entity;

import com.l.cloud.common.entity.CommonEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "task")
public class Task extends CommonEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "handler", columnDefinition = "varchar(20) COMMENT '执行器' ")
    private String handler;

    @Column(name = "name", columnDefinition = "varchar(20) COMMENT '任务名称' ")
    private String name;

    @Column(name = "annotation_value", columnDefinition = "varchar(20) COMMENT '注解值' " )
    private String annotationValue;

    @Column(name = "expression_value", columnDefinition = "varchar(50) COMMENT '定时任务表达式值' ")
    private String expressionValue;

    @Column(name = "expression_type", columnDefinition = "varchar(20) COMMENT '表达式类型'" )
    private String expressionType;

    @Column(name = "status", columnDefinition = "tinyint(1) COMMENT '状态 1.启动  2.停止' ")
    private Integer status = 2;

}
