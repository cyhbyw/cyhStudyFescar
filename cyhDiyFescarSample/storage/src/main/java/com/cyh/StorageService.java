package com.cyh;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:02
 */
public interface StorageService {

    /**
     * 扣减库存
     *
     * @param commodityCode 商品编号
     * @param count         扣减数量
     */
    void deduct(String commodityCode, int count);

}
