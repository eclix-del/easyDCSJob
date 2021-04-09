# job
根据自己的想法写的简单分布式任务调度。

注册中心使用nacos，使用eureka的话修改依赖已经配置文件可以无缝替换。

具体使用：
  1. 运行jobServer,
  2. 在要使用任务调度的项目中添加jobCLient依赖，然后在SpringApplication上添加@EnableJobScheduled注解开启任务调度功能
