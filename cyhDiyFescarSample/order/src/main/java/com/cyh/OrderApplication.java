package com.cyh;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:01
 */
public class OrderApplication {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("order-dubbo.xml");
        System.in.read();
    }

}
