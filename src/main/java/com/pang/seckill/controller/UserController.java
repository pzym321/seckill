package com.pang.seckill.controller;

import com.pang.seckill.pojo.User;
import com.pang.seckill.rabbitmq.MQSender;
import com.pang.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    @ResponseBody
    @RequestMapping("info")
    public RespBean info(User user) {
        return RespBean.success(user);
    }


//    @ResponseBody
//    @RequestMapping("/mq")
//    public void mq() {
//        mqSender.send("hello");
//    }
//    @ResponseBody
//    @RequestMapping("/mq/fanout")
//    public void mq01() {
//        mqSender.send("hello");
//    }
}
