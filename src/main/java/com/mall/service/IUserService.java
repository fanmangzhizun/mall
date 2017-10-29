package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.User;

/**
 * Created by faithpercious on 2017/10/14.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<User> Register(User user);
    ServerResponse<User> check_valid(String str,String type);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String question,String answer);
    ServerResponse<String> forgetReplacePassword(String username,String passwordNew,String forgetToken);
    ServerResponse<String> ReplacePassword(String passwordold,String passwordNew,User user);
    ServerResponse<User> updateInformation(User user);
    ServerResponse<User> getInformation(Integer id);
    ServerResponse checkAdmin(User user);
}
