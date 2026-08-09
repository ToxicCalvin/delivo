package com.delivo.service;

import com.delivo.dto.UserLoginDTO;
import com.delivo.entity.User;

public interface UserService {

    /**
     * 微信登录
     * 
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 账号密码登录
     * 
     * @param userAccountLoginDTO
     * @return
     */
    User accountLogin(com.delivo.dto.UserAccountLoginDTO userAccountLoginDTO);

    /**
     * 用户注册
     * 
     * @param userRegisterDTO
     */
    void register(com.delivo.dto.UserRegisterDTO userRegisterDTO);
}
