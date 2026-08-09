package com.delivo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.delivo.constant.MessageConstant;
import com.delivo.dto.UserLoginDTO;
import com.delivo.entity.User;
import com.delivo.exception.LoginFailedException;
import com.delivo.mapper.UserMapper;
import com.delivo.properties.WeChatProperties;
import com.delivo.service.UserService;
import com.delivo.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * 
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED());
        }

        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        // 如果是新用户，自动完成注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        // 返回这个用户对象
        return user;
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * 
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 调用微信接口服务，获得当前微信用户的openid
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        // Mock login for development
        if ("123456".equals(code)) {
            return "mock-openid-for-dev";
        }

        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }

    /**
     * 账号登录
     *
     * @param userAccountLoginDTO
     * @return
     */
    @Override
    public User accountLogin(com.delivo.dto.UserAccountLoginDTO userAccountLoginDTO) {
        String username = userAccountLoginDTO.getUsername();
        String password = userAccountLoginDTO.getPassword();

        // 根据用户名查询
        User user = userMapper.getByUsername(username);

        if (user == null) {
            throw new com.delivo.exception.AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND());
        }

        // 密码比对
        // 进行md5加密，然后再进行比对
        password = org.springframework.util.DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new com.delivo.exception.PasswordErrorException(MessageConstant.PASSWORD_ERROR());
        }

        // 账号没有状态控制，所以省略了状态校验

        return user;
    }

    /**
     * 账号注册
     *
     * @param userRegisterDTO
     */
    @Override
    public void register(com.delivo.dto.UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        User user = userMapper.getByUsername(username);

        if (user != null) {
            throw new com.delivo.exception.BaseException(MessageConstant.ACCOUNT_ALREADY_EXISTS());
        }

        User newUser = new User();
        org.springframework.beans.BeanUtils.copyProperties(userRegisterDTO, newUser);

        // 密码进行MD5加密
        String password = org.springframework.util.DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        newUser.setPassword(password);

        // 其他字段处理
        newUser.setCreateTime(LocalDateTime.now());

        // 插入数据库
        userMapper.insert(newUser);
    }
}
