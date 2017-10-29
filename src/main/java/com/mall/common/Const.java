package com.mall.common;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by faithpercious on 2017/10/15.
 */
public class Const {
    public  static final String CURRENT_USER="currentUser";
    public  static final String USERNAME="username";
    public  static final String EMAIL="email";

    public interface Cart{
        int CHECKED=1;
        int UN_CHECKED=0;
        String LIMIT_NUM_FAILED="LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
    }


    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC= Sets.newHashSet("price_desc","price_asc");
    }

    public interface Role{
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1;//管理员
    }

    public enum  ProductStatusItem{
        ON_SALE(1,"在线");
        private String value;
        private int code;

        ProductStatusItem( int code,String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已支付"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_FAILED(60,"订单失效");
        private Integer code;
        private String value;
        OrderStatusEnum(Integer code,String value) {
            this.code=code;
            this.value=value;
        }
        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }


        public static OrderStatusEnum codeof(int code){
            for (OrderStatusEnum orderStatusItem:values()){
                if (orderStatusItem.getCode()==code)
                    return orderStatusItem;
            }
            throw  new  RuntimeException("没有找到对应的枚举");
        }


    }

    public interface AliPayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY="WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS="TRADE_SUCCESS";
        String RESPONNSE_SUCCESS="success";
        String  RESPONNSE_FAILED="failed";
    }

    public enum  PayPlatformEnum{
        ALIPAY(1,"支付宝");
        PayPlatformEnum(Integer code,String value) {
            this.code=code;
            this.value=value;
        }
        private Integer code;
        private String value;

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }


        public static PayPlatformEnum codeof(int code){
            for (PayPlatformEnum payPlatformEnum:values()){
                if (payPlatformEnum.getCode()==code)
                    return payPlatformEnum;
            }
            throw  new  RuntimeException("没有找到对应的枚举");
        }
    }












































































































































}
