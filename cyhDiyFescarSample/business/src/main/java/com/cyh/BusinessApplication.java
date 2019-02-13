package com.cyh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:01
 */
public class BusinessApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessApplication.class);

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("business-dubbo.xml");
        BusinessService businessService = (BusinessService) context.getBean("business");
        LOGGER.info("业务调用开始");
        businessService.purchase("cyh", "lightSuite", 5);
        LOGGER.info("业务调用结束");
    }

}
