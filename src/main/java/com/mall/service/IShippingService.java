package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;

/**
 * Created by faithpercious on 2017/10/25.
 */
public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse delete(Integer userId,Integer shippingId);
    ServerResponse update(Integer userId,Shipping shippingId);
    ServerResponse search(Integer userId,Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

}
