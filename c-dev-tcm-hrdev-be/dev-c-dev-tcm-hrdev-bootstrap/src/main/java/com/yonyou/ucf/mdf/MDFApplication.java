package com.yonyou.ucf.mdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;


/**
 * spring-boot 入口类
 */
@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RabbitAutoConfiguration.class})
@ComponentScan(basePackages = {"com.yonyou", "com.yonyoucloud"})
@ImportResource(locations = {"classpath*:/config/applicationContext*.xml", "classpath*:/spring-sub/applicationContext*.xml"})
public class MDFApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext run = SpringApplication.run(MDFApplication.class, args);
        finishInfoPrint();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 注意这里要指向原先用main方法执行的Application启动类
        SpringApplicationBuilder applicationBuilder = builder.sources(MDFApplication.class);
        finishInfoPrint();
        return applicationBuilder;
    }

    private static void finishInfoPrint() {
        System.out.println();
        System.out.println("-----------------------------------");
        System.out.println("YPD脚手架服务启动完成!!");
        System.out.println("-----------------------------------");
    }
}
