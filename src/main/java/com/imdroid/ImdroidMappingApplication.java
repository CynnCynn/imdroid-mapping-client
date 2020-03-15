package com.imdroid;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.imdroid.dao.mapper")
public class ImdroidMappingApplication {

    public static void main(String[] args) {
//        ConfigurableApplicationContext run = SpringApplication.run(ImdroidMappingApplication.class, args);
        SpringApplication springApplication = new SpringApplication(ImdroidMappingApplication.class);
//        修改启动时控制台打印的图标
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
    }
}
