package com.tk.yupicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.xml.ws.soap.MTOM;

@SpringBootApplication
@EnableAsync
@MapperScan("com.tk.yupicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YuPictureBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuPictureBackendApplication.class, args);
    }
}

