package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.vo.CartVo;

/**
 * Created by faithpercious on 2017/10/23.
 */
public interface ICartService {
    ServerResponse<CartVo>  add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> delete(Integer userId,String productIds);
    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> SelectOrUnSelect(Integer userId, Integer productId, Integer checked);
    ServerResponse<Integer> getCartProduct(Integer userId);
    ServerResponse<CartVo> CartList(Integer userId);
}
