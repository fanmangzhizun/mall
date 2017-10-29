package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICartService;
import com.mall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by faithpercious on 2017/10/23.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService iCartService;

    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpSession session,Integer productId, Integer count){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
     return iCartService.add(user.getId(),productId,count);
    }

    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(HttpSession session,String productIds){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.delete(user.getId(),productIds);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session,Integer productId, Integer count){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.update(user.getId(),productId,count);
    }


    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse selectAll(HttpSession session,Integer productId, Integer checked){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.SelectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("UnSelect_all.do")
    @ResponseBody
    public ServerResponse UnSelectAll(HttpSession session,Integer productId, Integer checked){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.SelectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session,Integer productId, Integer checked){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.SelectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }
    @RequestMapping("UnSelect.do")
    @ResponseBody
    public ServerResponse UnSelect(HttpSession session,Integer productId, Integer checked){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.SelectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    @RequestMapping("getCount.do")
    @ResponseBody
    public ServerResponse getCartProductCount(HttpSession session){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());        }
        return iCartService.getCartProduct(user.getId());
    }
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iCartService.CartList(user.getId());
    }



































































}
