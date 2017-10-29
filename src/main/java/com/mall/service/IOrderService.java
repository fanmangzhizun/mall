package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Order;
import com.mall.vo.OrderVo;

import java.util.Map;

/**
 * Created by faithpercious on 2017/10/25.
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);
    ServerResponse aliPayCallBack(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
    ServerResponse createOrder(Integer userId,Integer shippingId);
    ServerResponse<String> cancelOrder(Integer userId,Long orderNo);
    ServerResponse getCartOrder(Integer userId);
    ServerResponse<OrderVo>  getOrderDetail(Integer userId, Long orderNo);
    ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> manageList(Integer pageNum,Integer pageSize);
    ServerResponse<PageInfo> searchOrder(Long orderNo,int pageNum,int pageSize);
    ServerResponse<OrderVo> manageOrderDetail(Long orderNo);
    ServerResponse manageSendGoods(Long orderNo);
}
