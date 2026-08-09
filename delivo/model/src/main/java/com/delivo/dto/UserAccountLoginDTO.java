package com.delivo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "C端用户账号登录")
public class UserAccountLoginDTO implements Serializable {

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("密码")
    private String password;

}
