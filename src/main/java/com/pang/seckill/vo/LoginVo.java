package com.pang.seckill.vo;

import com.pang.seckill.validator.IsMobile;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;
    @NotNull
    private String password;
}
