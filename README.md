### jdbc 监控

使用agent的方式监控当前java 程序的 sql执行情况，并计算执行时间 

![preview](./img/preview0.png)

目前只支持 mysql5.* 和8.* 的驱动程序，如果想支持其他 sql,
参见 `monitor-agent`模块下的 `xyz.thetbw.monitor.jdbc.agent.transformers`包

#### 使用方法
* 下载release jar文件
* `java -jar xxx.jar`执行下载的jar 文件
* 在打开浏览器(或者手动打开 http://127.0.0.1:10086),选择需要监控的程序
* 注： 需要监控的程序需要使用的是 mysql 5.* 或 8.*的驱动，不然会连接失败



#### 目前并不算完善，没有完整测试，目前已知以下问题

* 连接过程中如果本服务器断开，就不能再次连接了，必须重启目标程序
* 和上个问题类型，断开连接后会恢复被修改的class,不过可能并未清理干净
* 目前仅在 oracle jdk8 上测试通过，其他版本可能连接失败
* 在windows上出现过不显示进程列表的问题，jps也不显示进程列表，应该是jvm的问题，把jar包从d盘复制到桌面莫名又好了
* ...


#### 如果你想自己编译，还有如下问题，见 [monitor-server](./monitor-server/README.md)

#### 其他 
> 原本开发这个程序的目的是方便开发过程中复制正在执行的sql，用于排查问题。然后看了mysql驱动的源码才发现mysql驱动自带性能监控，
> spring 也可以使用好几种方式来打印执行的 sql 😂