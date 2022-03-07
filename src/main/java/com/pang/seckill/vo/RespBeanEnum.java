package com.pang.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString@Getter
@AllArgsConstructor
public enum RespBeanEnum {
    //服务端异常
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登录异常
    LOGIN_ERROR(500210,"登录用户名或者密码有误"),
    MOBILE_ERROR(500211,"手机号码格式出错"),
    MOBILE_NOTEXIT(500213,"手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500214,"密码更新失败"),
    SESSION_ERROR(500215,"用户不存在"),
    //全局异常
    BIND_ERROR(500212,"参数校验异常"),
    ACCESS_LIMIT_REAHCED(500216,"连接超时"),
    ERROR_CAPTCHA(500217,"验证码出错"),
    ACCESS_LIMIT_REACHED(500218,"访问过于频繁"),
    //订单模块
    ORDER_NOT_EXITS(500300,"订单信息不存在"),

    //秒杀模块
    EMPTY_STOCK(500500,"空库存"),
    REPEAT_ERROR(500501,"重复购买"),
    REQUEST_ILLEGAL(500502,"请求非法，请重新尝试")
    ;
    private final  Integer code;
    private final String message;

}
