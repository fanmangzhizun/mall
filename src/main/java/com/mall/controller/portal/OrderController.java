package com.mall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by faithpercious on 2017/10/25.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static  final Logger logger=LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;


@RequestMapping("create.do")
@ResponseBody
    public ServerResponse createOrder(HttpSession session,Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }


    @RequestMapping("cancel.do")
    @ResponseBody
    public  ServerResponse cancel(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(),orderNo);
    }


    @RequestMapping("getCartOrder.do")
    @ResponseBody
    public ServerResponse getCartOrder(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iOrderService.getCartOrder(user.getId());
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse OrderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,@RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return  iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, HttpServletRequest request,Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(), ResponseCode.NEED_LOGN.getDesc());
        }
        String url = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(orderNo,user.getId(),url);
    }

    @RequestMapping("aliPay_callback.do")
    @ResponseBody
    public Object aliPayCallback(HttpServletRequest request){
        Map<String,String > params= Maps.newHashMap();

        Map requestParams =request.getParameterMap();
        for (Iterator iterator=requestParams.keySet().iterator();iterator.hasNext();){
            String name= (String) iterator.next();
            String[] values= (String[]) requestParams.get(name);
            String valueStr="";
            for (int i=0;i<values.length;i++){
                valueStr=(i==values.length-1)?valueStr+values[1]:valueStr+values[1]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
      //验证回调，是不是支付宝发的，
        params.remove("sign_type");
        try {
            boolean aliPayRSACheckedV2= AlipaySignature.rsaCheckV2(params, Configs.getPublicKey(),"utf-8",Configs.getSignType());
            if (!aliPayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求，验证不通过,继续恶意请求则使用网警辅助");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常",e);
        }

        //todo 验证各种数据
       ServerResponse serverResponse=iOrderService.aliPayCallBack(params);
        if (serverResponse.isSuccess()){
            return Const.AliPayCallback.RESPONNSE_SUCCESS;
        }
        return Const.AliPayCallback.RESPONNSE_FAILED;
    }

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccessData(true);
        }
        return ServerResponse.createBySuccessData(false);
    }







































































}
