package com.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Order;
import com.mall.pojo.User;
import com.mall.service.IOrderService;
import com.mall.service.IUserService;
import com.mall.service.Impl.IOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * Created by faithpercious on 2017/10/27.
 */

@Controller
@RequestMapping("/manage/order")
public class OrderMangerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;


    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> manageOrderList(HttpSession session,@RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,@RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageList(pageNum,pageSize);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }



    //搜索订单功能
    @RequestMapping("search.do")
    @ResponseBody
    public  ServerResponse manageSearchOrder(HttpSession session,Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.searchOrder(orderNo,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("无权限操作");
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public  ServerResponse  manageOrderDetail(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageOrderDetail(orderNo);
        }
        return ServerResponse.createByErrorMessage("无权限操作");
    }

    @RequestMapping("send.do")
    @ResponseBody
    public ServerResponse manageSendGoods(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iOrderService.manageSendGoods(orderNo);
        }
        return ServerResponse.createByErrorMessage("无权限操作");
    }
}
