package com.mall.controller.portal;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by faithpercious on 2017/10/14.
 */
@Controller
@RequestMapping("/user/")

public class UserController {
    @Autowired
    private IUserService iUserService;
    //登录接口
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
       ServerResponse<User> response=iUserService.login(username, password);
       if (response.isSuccess()){
           session.setAttribute(Const.CURRENT_USER,response.getData());
       }
       return  response;
    }
    //注销接口
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return  ServerResponse.createBySuccess();
    }
    //注册接口
    @RequestMapping(value="regist.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> Register(User user){
        return iUserService.Register(user);
    }
    //校验接口
    @RequestMapping(value="check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> check_valid(String str,String type){
        return  iUserService.check_valid(str, type);
    }
    //获取用户信息接口
    @RequestMapping(value="get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user!=null){
            return ServerResponse.createBySuccessData(user);
        }
        else return ServerResponse.createByErrorMessage("该用户尚未登录，无法获取信息");
    }
    //用户忘记密码获取密码问题的接口
    @RequestMapping(value="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }


    //用户验证密码问题的检查接口
    @RequestMapping(value="forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> forgetCheckQuestion(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }
    //忘记密码状态时重置密码的功能
    @RequestMapping(value="forget_Replace_Password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetReplacePassword(String username,String passwordNew,String forgetToken){
        //需要使用验证密码的接口进行确认
        return iUserService.forgetReplacePassword( username, passwordNew, forgetToken);
    }
    //登录状态时修改密码的接口
    //通过验证用户id避免横向越权

    @RequestMapping(value="Replace_Password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> ReplacePassword(String passwordOld,String passwordNew,HttpSession session){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return  ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.ReplacePassword(passwordOld,passwordNew,user);
    }

    //更新用户信息的操作接口
    @RequestMapping(value="Update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        User currentUser= (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return  ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());//通过currentUser给user进行id赋值，不理解
        user.setUsername(currentUser.getUsername());
        ServerResponse<User>response=iUserService.updateInformation(user);
        if (response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return  response;
    }

    //获取用户信息接口
    @RequestMapping(value="get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        User currentUser= (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
