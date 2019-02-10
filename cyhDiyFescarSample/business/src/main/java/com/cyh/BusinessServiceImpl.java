package com.cyh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fescar.core.context.RootContext;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 18:13
 */
public class BusinessServiceImpl implements BusinessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessServiceImpl.class);

    private OrderService orderService;
    private StorageService storageService;

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @GlobalTransactional(timeoutMills = 300000, name = "cyh-dubbo-demo-tx")
    @Override
    public void purchase(String userId, String commodityCode, int orderCount) {
        LOGGER.info("Purchase begin. userId: {}, commodityCode: {}, orderCount: {}, xid: {}", userId, commodityCode,
                orderCount, RootContext.getXID());
        storageService.deduct(commodityCode, orderCount);
        orderService.create(userId, commodityCode, orderCount);
        System.out.println(1 / 0);
        LOGGER.info("Purchase end.");
    }
}
