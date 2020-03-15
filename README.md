# mapping

#### 项目介绍
测绘系统平台，通过对三维扫描仪采集的房间数据，计算出所需的房间指标

#### 软件架构
软件架构说明
    
    (1)此项目使用了springboot框架，数据库连接池使用的是druid，json解析使用的是fastjson
    
    (2)数据库使用的是mysql、容器是springboot内置的tomcat
    
    (3)图片展示工具使用的是jfreechart，矩阵运算包jama，串口工具包rxtx
    
    (4)用于加速开发的包lombok。注：按https://www.cnblogs.com/hackyo/p/7998485.html里配置好相应插件，否则项目报错
 

#### 使用说明

    (1)日志目录:application.yml文件中的logging-file里修改
    
    (2)数据库配置:spring-datasource里的username、password修改为自己的
    
    (3)打包:右边侧边栏maven-package即可。注：打包前单元测试（com.imdroid.ImdroidMappingApplicationTests）里的方法如果不能正常运行，会报错，将该文件中所有注解注释掉即可。
    
    

#### 参与贡献

1.点击克隆/下载->HTTPS->复制

2.点击Intellij IDEA的VCS->Checkout from Version Control->粘贴刚刚复制的url

3.如果加新的功能，先创建新的分支，不直接在主支上修改。修复主支的bug才在主支上去操作。

