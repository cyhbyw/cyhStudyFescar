package com.cyh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fescar.core.context.RootContext;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:02
 */
public class StorageServiceImpl implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deduct(String commodityCode, int count) {
        LOGGER.info("Storage Service Begin. commodityCode: {}, count: {}, xid: {}", commodityCode, count,
                RootContext.getXID());
        jdbcTemplate.update("update storage_tbl set count = count - ? where commodity_code = ?",
                new Object[] {count, commodityCode});
        LOGGER.info("Storage Service End. xid: {}", RootContext.getXID());
    }
}
