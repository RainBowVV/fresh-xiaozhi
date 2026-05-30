package com.xiaozhi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xiaozhi.mapper")
@EnableScheduling
public class FreshXiaozhiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FreshXiaozhiApplication.class, args);
    }
}
