参考文章
- glue  新型的分布式平台



https://blog.csdn.net/Zereao/article/details/82722523?utm_source=blogxgwz7

https://www.holddie.com/2018/03/08/xxl-job-yuan-ma-yi-ri-you/#!

http://www.gomoving.cn/?p=83

https://gitbook.cn/books/5bc7ddfe8a600f7aafe09b02/index.html
## 源码阅读
### 总的思路
分为调度器和执行器
调度器 和 执行器都有各自的两个后台线程 不断的跑着 扫描更新数据库的标

启动都是通过Spring的事件函数 start 和 destory
来启动相应的线程和关闭

#### 调度器(core端支持 example端启动)
以spring-example-exector为例子

配置相关启动的 bean 
com.learn.job.core.executor.JobSpringExecutor
在其 start方法中的start()开始正式干活儿

  JobFileAppender.initLogPath(logPath); 初始化日志目录
  
  然后他们之间会有个远程调用关系
  
  参考文档
  http://www.xuxueli.com/xxl-rpc/#/‘
  为了让core端把相关的信息调度和触发信息放到数据库中
  应该这些操作都是调度器 man端处理的
  


## 最近总结
其实对于这种多线程的调试 可以采取log 输出日志的方式查看进度
之前的问题多少RPC传输的基础类序列化没有加上
以及Jetty连接的端口与IP不一致的情况



