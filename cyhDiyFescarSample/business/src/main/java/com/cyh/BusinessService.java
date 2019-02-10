package com.cyh;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 18:12
 */
public interface BusinessService {
    /**
     * 用户订购商品
     *
     * @param userId        用户ID
     * @param commodityCode 商品编号
     * @param orderCount    订购数量
     */
    void purchase(String userId, String commodityCode, int orderCount);
}
