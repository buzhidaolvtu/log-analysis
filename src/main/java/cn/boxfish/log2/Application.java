package cn.boxfish.log2;

import cn.boxfish.log2.storage.LogPipeline;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ComponentScan(basePackages = "cn.boxfish.log2")
@ImportResource(value="classpath:spring-all.xml")
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        LogPipeline bean = context.getBean(LogPipeline.class);
        bean.transform(null);

    }
}
