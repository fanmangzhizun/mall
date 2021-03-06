package com.mall.service.Impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.dao.*;
import com.mall.pojo.*;
import com.mall.service.IFileService;
import com.mall.service.IOrderService;
import com.mall.util.BigDecimalUtil;
import com.mall.util.DateTimeUtil;
import com.mall.util.FTPUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.OrderItemVo;
import com.mall.vo.OrderProductVo;
import com.mall.vo.OrderVo;
import com.mall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.omg.PortableInterceptor.INACTIVE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by faithpercious on 2017/10/25.
 */
@Service("iOrderService")
public class IOrderServiceImpl implements IOrderService {


@Autowired
private PayInfoMapper payInfoMapper;
@Autowired
private ProductMapper productMapper;
@Autowired
private ShippingMapper shippingMapper;

    private static AlipayTradeService tradeService;


    static{
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private Logger logger= LoggerFactory.getLogger(IOrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;

    public ServerResponse pay(Long orderNo,Integer userId,String path){
        Map<String,String> resultMap= Maps.newHashMap();
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("该用户没有此订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));



        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("faithmall扫码支付").append("订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单：").append(outTradeNo).append("购买商品共:").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList=orderItemMapper.getByUserIdAndOrderNo(userId,orderNo);
        for (OrderItem orderItem : orderItemList){
            GoodsDetail goodsDetail=GoodsDetail.newInstance(orderItem.getId().toString(),orderItem.getProductName(), BigDecimalUtil.mut(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                // .setNotifyUrl("http://www.test-notify-url.com")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder=new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }


                // 需要修改为运行机器上的路径
                String qrPath = String.format(path+"/qr-%s.png",
                        response.getOutTradeNo());
                String qrFilename=String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getOutTradeNo(),256,qrPath);
                File targetFile=new File(path,qrFilename);
                try{
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                }catch (IOException e){
                    logger.error("上传二维码异常",e);
                }
                logger.info("qrPath:" + qrPath);
                String qrUrl= PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccessData(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");


            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");


            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }


    }


    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliPayCallBack(Map<String,String> params){
        Long orderNo= Long.parseLong(params.get("out_trade_no"));
        String tradeNo=params.get("trade_no");
        String tradeStatus=params.get("trade_status");
        Order order=orderMapper.selectByOrderNo(orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("非信仰商城的订单，请忽略");
        }
        if (order.getOrderNo()>= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }
        if (Const.AliPayCallback.RESPONNSE_SUCCESS.equals(tradeStatus)){
                order.setCreateTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
                order.setStatus(Const.OrderStatusEnum.PAID.getCode());
                orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo=new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order==null){
            return  ServerResponse.createByErrorMessage("用户没有此订单");
        }
        if (order.getStatus()>=Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return   ServerResponse.createByError();
    }



    //生成订单
    public ServerResponse createOrder(Integer userId,Integer shippingId){
        //获取购物车中需生成订单的对象
        List<Cart> cartList=cartMapper.selectCheckedByUserId(userId);
        //计算出总金额
        ServerResponse serverResponse=this.getCartOrderItem(userId,cartList);//拿到购物车的订单
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList= (List<OrderItem>) serverResponse.getData();
        BigDecimal payment=this.getOrderTotalPrice(orderItemList);
        //生成订单
        Order order=this.assembleOrder(userId,shippingId,payment);
        if(order==null){
            return ServerResponse.createByErrorMessage("生成订单错误");
        }
        //判断错误原因是否是不存在需要购买的商品
        if (CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //放入orderItemMapper表中
        orderItemMapper.BatchInsert(orderItemList);
        //成功后减少库存
       this.reduceProduct(orderItemList);
        //清理购物车
        this.cleanCart(cartList);

        OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccessData(orderVo);
    }


    //取消订单
    public ServerResponse<String> cancelOrder(Integer userId,Long orderNo){
        //实现订单号对应id
        //用id找到订单最后删了
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order==null){
            return ServerResponse.createByErrorMessage("该用户无此订单");
        }
        if (order.getStatus()!=Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }
        Order updateOrder=new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int resultCount=orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (resultCount>0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    //根据购物车生成订单数据
    public  ServerResponse getCartOrder(Integer userId){
        OrderProductVo orderProductVo=new OrderProductVo();
        List<Cart> cartList=cartMapper.selectCheckedByUserId(userId);
        ServerResponse serverResponse=this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess()){
            return ServerResponse.createByError();
        }
        List<OrderItem> orderItemList= (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        BigDecimal payments=new BigDecimal("0");
        for (OrderItem orderItem:orderItemList){
           payments=BigDecimalUtil.add(payments.doubleValue(),orderItem.getTotalPrice().doubleValue());//计算总价
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payments);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccessData(orderProductVo);
    }


    //获取订单详情
    public  ServerResponse<OrderVo>  getOrderDetail(Integer userId,Long orderNo){
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order==null){
            return  ServerResponse.createByErrorMessage("没有找到该订单");
        }
        List<OrderItem> orderItemList=orderItemMapper.getByUserIdAndOrderNo(userId,orderNo);
        OrderVo orderVo=assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccessData(orderVo);
    }

    //获取订单列表
    public  ServerResponse<PageInfo> getOrderList(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList=this.assembleOrderVoList(orderList,userId);
        PageInfo pageResult=new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccessData(pageResult);
    }


    private  ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList=Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cartItem:cartList){
            OrderItem orderItem=new OrderItem();
            Product product=productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStatusItem.ON_SALE.getCode()!=product.getStatus()){
                return ServerResponse.createByErrorMessage("产品:"+product.getName()+"不是在线销售状态");
            }
            if (cartItem.getQuantity()>product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mut(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
    return ServerResponse.createBySuccessData(orderItemList);
    }


    //计算订单总价
    private BigDecimal  getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment=new BigDecimal("0");
        for (OrderItem orderItem:orderItemList){
            BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }


    //生成订单号
    private long generateOrderNo(){
        long currentTime=System.currentTimeMillis();
        currentTime+=currentTime+new Random().nextInt(1000);
        return currentTime;
    }


    //装配订单对象
    private  Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order=new Order();
        long orderNo=this.generateOrderNo();
        order.setPayment(payment);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);//邮费
        order.setPaymentType(Const.PayPlatformEnum.ALIPAY.getCode());
        order.setShippingId(shippingId);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        int rowCount=orderMapper.insert(order);
        if (rowCount>0){
            return order;
        }
        return null;
    }

    //减少库存
    private void reduceProduct(List<OrderItem> orderItemList){
        for (OrderItem orderItem:orderItemList){
            Product product=productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    //清理购物车
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }


    private  List<OrderVo>  assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList=Lists.newArrayList();
        for (Order order:orderList){
            List<OrderItem> orderItemList=Lists.newArrayList();
            if (userId==null){
                orderItemList=orderItemMapper.getByOrderNo(order.getOrderNo());
            }
            else orderItemList=orderItemMapper.getByUserIdAndOrderNo(userId,order.getOrderNo());

            OrderVo orderVo=assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return  orderVoList;
    }







    //生成OrderVo对象操作
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo=new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPayment(order.getPayment());
        orderVo.setStatus(order.getStatus());
        orderVo.setPaymentTypeDesc(Const.PayPlatformEnum.codeof(order.getPaymentType()).getValue());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeof(order.getStatus()).getValue());

        Shipping shipping=shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            //放入shippingVo对象
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }//收货地可以是空，若第一次使用，选择商品后进行下单后可以之后进行创建收货地
        orderVo.setPaymentTime(DateTimeUtil.DateTostr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.DateTostr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.DateTostr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.DateTostr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.DateTostr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        for (OrderItem orderItem:orderItemList){
            OrderItemVo orderItemVo=assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }


    //生成ShippingVo对象
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }
    //生成OrderItemVo对象
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.DateTostr(orderItem.getCreateTime()));
        return orderItemVo;
    }




    //backend

    //列表页
    public ServerResponse<PageInfo> manageList(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=orderMapper.selectAllOrder();
        List<OrderVo> orderVoList=this.assembleOrderVoList(orderList,null);
        PageInfo pageResult=new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccessData(pageResult);
    }

    //搜索
    public  ServerResponse<PageInfo> searchOrder(Long orderNo,int pageNum,int pageSize){
            PageHelper.startPage(pageNum,pageSize);
           Order order= orderMapper.selectByOrderNo(orderNo);
            if (order!=null){
                List<OrderItem> orderItemList=orderItemMapper.getByOrderNo(orderNo);
               OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
                PageInfo pageResult=new PageInfo(Lists.newArrayList(order));
                pageResult.setList(Lists.newArrayList(order));
                return  ServerResponse.createBySuccessData(pageResult);
            }
            else return  ServerResponse.createByErrorMessage("此订单号不存在");
    }


    //查看订单详情
    public  ServerResponse<OrderVo> manageOrderDetail(Long orderNo){
        Order order =orderMapper.selectByOrderNo(orderNo);
        if (order==null) return ServerResponse.createByErrorMessage("此订单号不存在");
        List<OrderItem> orderItemList=orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo=this.assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccessData(orderVo);

    }


    //发货管理
    public  ServerResponse manageSendGoods(Long orderNo){
        Order order=orderMapper.selectByOrderNo(orderNo);
        if (order!=null) {
            if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                order.setShippingId(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("此订单号不存在");
    }

}
