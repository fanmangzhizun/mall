package com.mall.service.Impl;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.common.Tokencahe;
import com.mall.dao.UserMapper;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by faithpercious on 2017/10/14.
 */
@Service("iUserService")
public class UserServiceImpl  implements IUserService{
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount=userMapper.CheckUserName(username);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //todo 密码登录使用MD5进行加密
        String md5Password=MD5Util.MD5EncodeUtf8(password);
        User user=userMapper.selectLogin(username, md5Password);
        if (user==null){
            return  ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessData("登录成功",user);
    }

    public ServerResponse<User> Register(User user){
        ServerResponse validResponse=this.check_valid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return  validResponse;
        }
        validResponse=this.check_valid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return  validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount=userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<User> check_valid(String str,String type){
        if (StringUtils.isNotBlank(str)) {
            if (Const.USERNAME.equals(type)){
            int resultCount = userMapper.CheckUserName(str);
            if (resultCount > 0) {
                return ServerResponse.createByErrorMessage("用户名已存在");
            }
        }
            if (Const.EMAIL.equals(type)) {
                if (StringUtils.isNotBlank(type)) {
                    int resultCount = userMapper.CheckEmail(str);
                    if (resultCount > 0) {
                        return ServerResponse.createByErrorMessage("email已存在");
                    }
                }
            }
        }
        else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }


    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validresponse=this.check_valid(username,Const.USERNAME);
        if (validresponse.isSuccess()){
                return ServerResponse.createByErrorMessage("用户不存在");//因为校验的成功状态是当用户不存在时，故此时需进行取反操作
        }
        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return  ServerResponse.createBySuccessData(question);
        }
            return  ServerResponse.createByErrorMessage("找回的密保问题为空");
    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        ServerResponse validresponse=this.check_valid(username,Const.USERNAME);
        if (validresponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");//因为校验的成功状态是当用户不存在时，故此时需进行取反操作
        }
        int resultCount=userMapper.checkAnswer(username, question, answer);
       if (resultCount>0){
           //用户问题及密码验证正确
          String forgetToken= UUID.randomUUID().toString();
           Tokencahe.setkey(Tokencahe.TOKEN_PREFIX+username,forgetToken);
           return ServerResponse.createBySuccessData(forgetToken);
       }
       return ServerResponse.createByErrorMessage("用户验证失败");
    }

   public ServerResponse<String> forgetReplacePassword(String username,String passwordNew,String forgetToken){
        //使用token进行验证，之后进行修改密码
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，需传递token");
        }
        ServerResponse validResponse=this.check_valid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token=Tokencahe.getkey(Tokencahe.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(forgetToken,token)){
            String MD5password=MD5Util.MD5EncodeUtf8(passwordNew);
            int resultcount=userMapper.updatePasswordByUsername(username,MD5password);
            if (resultcount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }
        else return ServerResponse.createByErrorMessage("token验证失败");
        return ServerResponse.createByErrorMessage("修改密码失败");
        }



    public ServerResponse<String> ReplacePassword(String passwordOld,String passwordNew,User user){
        //使用一个检查密码和用户id的方法进行判定，之后进行操作
        String MD5PassWordOld=MD5Util.MD5EncodeUtf8(passwordOld);
//        user=userMapper.selectByPrimaryKey(user.getId());   不需要传值
        int resultCount=userMapper.checkPassword(MD5PassWordOld,user.getId());//验证旧密码
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));//改变用户新密码
        int updateCount =userMapper.updateByPrimaryKeySelective(user);//验证成功后进行修改密码的操作
        if (updateCount>0){
            return  ServerResponse.createBySuccessMessage("更改密码成功");
        }
       return  ServerResponse.createByErrorMessage("更改密码失败");
    }




    public ServerResponse<User> updateInformation(User user){
        //已经处于登录状态，只需进行更改信息操作，无需验证
        //用户名不可更改
        //更改邮箱需验证，是否被其他用户使用
        int resultcount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultcount>0){
            return ServerResponse.createByErrorMessage("该邮箱已被注册，请选择合适的邮箱");
        }
        User updateuser=new User();
        updateuser.setId(user.getId());//将Id进行重新赋值
        updateuser.setPhone(user.getPhone());
        updateuser.setAnswer(user.getAnswer());
        updateuser.setQuestion(user.getQuestion());
        updateuser.setEmail(user.getEmail());
        int updatecount=userMapper.updateByPrimaryKeySelective(updateuser);
        if (updatecount>0){
            return ServerResponse.createBySuccessData("更新信息成功",updateuser);
        }
        return ServerResponse.createByErrorMessage("更新信息失败");
    }

    public ServerResponse<User> getInformation(Integer id){
        User user=userMapper.selectByPrimaryKey(id);
        if (user==null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);//密码为何要置空
        return  ServerResponse.createBySuccessData(user);
    }


   //检验是否是管理员的操作
    public ServerResponse checkAdmin(User user){
        if (user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
    }

