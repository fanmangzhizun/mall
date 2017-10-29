package com.mall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.dao.ShippingMapper;
import com.mall.pojo.Shipping;
import com.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by faithpercious on 2017/10/25.
 */

@Service("ishipppingService")
public class IShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;



    public ServerResponse add(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        int rowCount=shippingMapper.insert(shipping);
        if (rowCount>0){
            Map result=new HashMap();
            result.put("ShippingId",shipping.getId());
            return  ServerResponse.createBySuccessData("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址成功");
    }

    public  ServerResponse delete(Integer userId,Integer shippingId){
        int rowCount=shippingMapper.deleteByShippingIdAndUserId(userId ,shippingId);
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return  ServerResponse.createByErrorMessage("删除地址失败");
    }

    public  ServerResponse update(Integer userId,Shipping shipping){
        shipping.setUserId(userId);
        int rowCount=shippingMapper.updateByShippingAndUserId(shipping);
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("修改地址成功");
        }
        return  ServerResponse.createByErrorMessage("修改地址失败");
    }


    public  ServerResponse search(Integer userId,Integer shippingId){
        Shipping shipping=shippingMapper.selectByShippingAndUserId(userId,shippingId);
        if (shipping==null){
            return  ServerResponse.createByErrorMessage("查询地址失败");
        }
        return ServerResponse.createBySuccessData("查询地址成功",shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList= shippingMapper.selectShippingByUserId(userId);
        PageInfo  pageInfo=new PageInfo(shippingList);
        return  ServerResponse.createBySuccessData(pageInfo);
    }







}
