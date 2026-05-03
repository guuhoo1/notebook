package com.notebook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个人记事本应用启动类
 * 
 * @author notebook
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.notebook.mapper")
public class NotebookApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotebookApplication.class, args);
        System.out.println("======================================");
        System.out.println("  Notebook Server Started Successfully");
        System.out.println("  API: http://localhost:8080/api");
        System.out.println("======================================");
    }
}
