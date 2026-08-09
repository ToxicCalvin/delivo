package com.delivo.controller.user;

import com.delivo.constant.JwtClaimsConstant;
import com.delivo.dto.UserLoginDTO;
import com.delivo.entity.User;
import com.delivo.properties.JwtProperties;
import com.delivo.result.Result;
import com.delivo.service.UserService;
import com.delivo.utils.JwtUtil;
import com.delivo.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "Client-Side User API")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录
     * 
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("WeChat Login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录：{}", userLoginDTO.getCode());

        // 微信登录
        User user = userService.wxLogin(userLoginDTO);

        // 为微信用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 账号密码登录
     * 
     * @param userAccountLoginDTO
     * @return
     */
    @PostMapping("/accountLogin")
    @ApiOperation("Account Password Login")
    public Result<UserLoginVO> accountLogin(@RequestBody com.delivo.dto.UserAccountLoginDTO userAccountLoginDTO) {
        log.info("C端用户账号密码登录：{}", userAccountLoginDTO.getUsername());

        // 账号密码登录
        User user = userService.accountLogin(userAccountLoginDTO);

        // 为该用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     * 
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("User Registration")
    public Result register(@RequestBody com.delivo.dto.UserRegisterDTO userRegisterDTO) {
        log.info("C端用户注册：{}", userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success();
    }
}
