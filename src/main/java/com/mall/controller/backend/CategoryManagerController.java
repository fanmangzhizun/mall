package com.mall.controller.backend;

import com.google.common.collect.Sets;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Category;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by faithpercious on 2017/10/17.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {
    //后台管理员类
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String CategoryName,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        //验证用户是否存在
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录");
        }
        //验证用户是否是管理员
        if (iUserService.checkAdmin(user).isSuccess()){
                //用iCategory进行分类的添加
           return iCategoryService.addCategory(CategoryName,parentId);
        }
        return ServerResponse.createByErrorMessage("无权限进行操作");
    }

    @RequestMapping(value = "update_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session,String CategoryNewName,Integer CategoryId){
        //更新品类名称
        //验证用户是否存在
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录");
        }
        //验证用户是否是管理员
        if (iUserService.checkAdmin(user).isSuccess()){
            //用iCategory进行品名的更新
            return iCategoryService.updateCategoryName( CategoryNewName,CategoryId);
        }
        return ServerResponse.createByErrorMessage("无权限进行操作");
    }

    @RequestMapping(value = "get_category_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer CategoryId){
        //查询品类信息
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录");
        }
        //验证用户是否是管理员
        if (iUserService.checkAdmin(user).isSuccess()){
            //用iCategory进行的获取信息
            return iCategoryService.getCategory(CategoryId);
        }
        return ServerResponse.createByErrorMessage("无权限进行操作");
    }


    @RequestMapping(value = "get_children_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategoryChildren(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录,请登录");
        }
        //验证用户是否是管理员
        if (iUserService.checkAdmin(user).isSuccess()){
            //用iCategory
            return iCategoryService.selectCategoryAndChildrenById(categoryId);//返回子结点的信息
        }
        return ServerResponse.createByErrorMessage("无权限进行操作");
    }


}
