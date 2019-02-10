package com.cyh;

import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.alibaba.fescar.core.context.RootContext;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:04
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order create(String userId, String commodityCode, int orderCount) {
        LOGGER.info("Order Service Begin. userId: {}, commodityCode: {}, orderCount: {}, xid: {}", userId,
                commodityCode, orderCount, RootContext.getXID());

        int orderMoney = orderCount * 200;
        Order order = new Order();
        order.userId = userId;
        order.commodityCode = commodityCode;
        order.count = orderCount;
        order.money = orderMoney;

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

        LOGGER.info("Order Service End. Created order: {}", order);
        return order;
    }
}
