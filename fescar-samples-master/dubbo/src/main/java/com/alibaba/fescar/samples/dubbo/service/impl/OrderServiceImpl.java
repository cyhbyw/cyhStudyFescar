/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.fescar.samples.dubbo.service.impl;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.alibaba.fescar.core.context.RootContext;
import com.alibaba.fescar.samples.dubbo.Order;
import com.alibaba.fescar.samples.dubbo.service.AccountService;
import com.alibaba.fescar.samples.dubbo.service.OrderService;

/**
 * Please add the follow VM arguments:
 * <pre>
 *     -Djava.net.preferIPv4Stack=true
 * </pre>
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private AccountService accountService;

    private JdbcTemplate jdbcTemplate;

    @Override
    public Order create(String userId, String commodityCode, int orderCount) {
        LOGGER.info("Order Service Begin. userId: {}, commodityCode: {}, orderCount: {}, xid: {}", userId,
                commodityCode, orderCount, RootContext.getXID());

        // 计算订单金额
        int orderMoney = calculate(commodityCode, orderCount);

        // 从账户余额扣款
        accountService.debit(userId, orderMoney);

        Order order = new Order();
        order.userId = userId;
        order.commodityCode = commodityCode;
        order.count = orderCount;
        order.money = orderMoney;

        LOGGER.info(
                "Order Service SQL: insert into order_tbl (user_id, commodity_code, count, money) values ({}, {}, {}, {})",
                userId, commodityCode, orderCount, orderMoney);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(
                    "insert into order_tbl (user_id, commodity_code, count, money) values (?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setObject(1, order.userId);
            preparedStatement.setObject(2, order.commodityCode);
            preparedStatement.setObject(3, order.count);
            preparedStatement.setObject(4, order.money);
            return preparedStatement;
        }, keyHolder);
        order.id = keyHolder.getKey().longValue();

        LOGGER.info("Order Service End... Created {}", order);
        return order;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private int calculate(String commodityId, int orderCount) {
        return 300 * orderCount;
    }

}
