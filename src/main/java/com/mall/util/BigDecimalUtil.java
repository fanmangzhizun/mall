package com.mall.util;

import java.math.BigDecimal;

/**
 * Created by faithpercious on 2017/10/23.
 */
public class BigDecimalUtil {

    private  BigDecimalUtil() {}
    public  static  BigDecimal add(double b1,double b2){
        BigDecimal a1=new BigDecimal(Double.toString(b1));
        BigDecimal a2=new BigDecimal(Double.toString(b2));
        return  a1.add(a2);
    }
    public  static  BigDecimal sub(double b1,double b2){
        BigDecimal a1=new BigDecimal(Double.toString(b1));
        BigDecimal a2=new BigDecimal(Double.toString(b2));
        return  a1.subtract(a2);
    }
    public  static  BigDecimal mut(double b1,double b2){
        BigDecimal a1=new BigDecimal(Double.toString(b1));
        BigDecimal a2=new BigDecimal(Double.toString(b2));
        return  a1.multiply(a2);
    }
    public  static  BigDecimal div(double b1,double b2){
        BigDecimal a1=new BigDecimal(Double.toString(b1));
        BigDecimal a2=new BigDecimal(Double.toString(b2));
        return  a1.divide(a2);
    }





}
