package com.cyh;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:03
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param commodityCode 商品编号
     * @param orderCount    订购数量
     * @return 生成的订单 order
     */
    Order create(String userId, String commodityCode, int orderCount);

}
