package com.cyh;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:01
 */
public class BusinessApplication {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("business-dubbo.xml");
        BusinessService businessService = (BusinessService) context.getBean("business");
        businessService.purchase("cyh", "lightSuite", 5);
    }

}
