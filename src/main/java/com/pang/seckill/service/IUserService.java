package com.pang.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.seckill.pojo.User;
import com.pang.seckill.vo.LoginVo;
import com.pang.seckill.vo.RespBean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IUserService extends IService<User> {

    public RespBean doLogin(LoginVo vo, HttpServletRequest request, HttpServletResponse response);

    public User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);

    RespBean updatePassword(String userticket,String password,HttpServletRequest request,HttpServletResponse response);
}
