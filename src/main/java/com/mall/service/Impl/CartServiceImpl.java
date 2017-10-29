package com.mall.service.Impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CartMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Cart;
import com.mall.pojo.Product;
import com.mall.service.ICartService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.CartProductVo;
import com.mall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;


/**
 * Created by faithpercious on 2017/10/23.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {
    //对于购物车相当于一列一列的实现最好集合封装返回
    //cartVo这是通常所见的购物车

    //全选
    //全反选

    //单独选
    //单独反选

    //查询当前用户的购物车里面的产品数量,如果一个产品有10个,那么数量就是10.
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    //需要用户id拿到用户的购物车，购物车单独设置对象，再判断此产品是否被用户选择过
    //商业金额计算使用Bigdecimal这个玩意
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart == null) {//用户未选择过该产品
            Cart cartItem = new Cart();//进行购物车对象的初始化
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setUserId(userId);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);//插入此购物车对象
        } else {
            count = count + cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
    return  this.list(userId);
    }

    public ServerResponse<CartVo> delete(Integer userId,String productIds){
        //分割 productIds
        List<String> productList= Splitter.on(",").splitToList(productIds);//可能一次删除多个商品
        if (CollectionUtils.isEmpty(productList)){
                return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.DeleteCartProductByProductIds(userId,productList);
        return   list(userId);
    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart!=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return   list(userId);
    }

    public ServerResponse<CartVo> SelectOrUnSelect(Integer userId, Integer productId, Integer checked){
        cartMapper.selectOrUnSelect(userId,productId,checked);
        return   list(userId);
    }

    public ServerResponse<Integer> getCartProduct(Integer userId){
        if (userId==null) return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return ServerResponse.createBySuccessData(cartMapper.getCartProductCount(userId));
    }

    public ServerResponse<CartVo> CartList(Integer userId){
        if (userId==null) return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return  list(userId);
    }

    //实现一个方法实现返回值的序列化
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setProductId(cartItem.getProductId());
                cartProductVo.setUserId(cartItem.getUserId());
                Product product = productMapper.selectByPrimaryKey(cartProductVo.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
                    //将对象转为cartProductVo
                    //开始搞价格

                    //判断库存
                    int buyLimitCount;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAILED);
                        //购物车进行有效存储
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mut(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());

                    //判断勾选状态
                    if (cartItem.getChecked() == Const.Cart.CHECKED) {
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                    }

                }
                cartProductVoList.add(cartProductVo);
            }

            cartVo.setCartTotalPrice(cartTotalPrice);
            cartVo.setCartProductVoList(cartProductVoList);
            cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
            cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        }
        return cartVo;
    }
//进行是否全选的判断
  private boolean getAllCheckedStatus(Integer userId){
        if (userId==null){
            return  false;
        }
        return cartMapper.selectCartProductStatusByUserId(userId)==0;
    }

    private  ServerResponse<CartVo> list(Integer userId){
      CartVo cartVo =this.getCartVoLimit(userId);
      return  ServerResponse.createBySuccessData(cartVo);
    }



}
