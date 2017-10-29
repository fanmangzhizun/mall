package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Shipping;
import com.mall.pojo.User;
import com.mall.service.IShippingService;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by faithpercious on 2017/10/25.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {
@Autowired
private IShippingService iShippingService;


@RequestMapping("add.do")
@ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse del(HttpSession session, Integer shippingId ){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iShippingService.delete(user.getId(),shippingId);
    }


    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }


    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse search(HttpSession session, Integer shippingId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iShippingService.search(user.getId(),shippingId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,HttpSession session){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),ResponseCode.NEED_LOGN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }


























































































}
