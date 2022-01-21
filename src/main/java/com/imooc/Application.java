package com.imooc;/**
 * @Title: project
 * @Package * @Description:     * @author CodingSir
 * @date 2022/1/149:28
 */

import com.github.tobato.fastdfs.FdfsClientConfig;
import com.imooc.netty.WSServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author zkjy
 * @version 1.0
 * @description zkjy
 * @updateUser
 * @createDate 2022/1/14 9:28
 * @updateDate 2022/1/14 9:28
 **/
@SpringBootApplication
@MapperScan(basePackages = "com.imooc.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
@Import(FdfsClientConfig.class)
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class Application {

    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    public static void main(String[] args) {

        new WSServer().start();

        SpringApplication.run(Application.class, args);
    }
}
