package com.cyh;

import java.io.Serializable;

/**
 * @author: CYH
 * @date: 2019/2/8 0008 17:03
 */
public class Order implements Serializable {

    /**
     * The Id.
     */
    public long id;
    /**
     * The User id.
     */
    public String userId;
    /**
     * The Commodity code.
     */
    public String commodityCode;
    /**
     * The Count.
     */
    public int count;
    /**
     * The Money.
     */
    public int money;

    @Override
    public String toString() {
        return "Order{" + "id=" + id + ", userId='" + userId + '\'' + ", commodityCode='" + commodityCode + '\''
                + ", count=" + count + ", money=" + money + '}';
    }
}
