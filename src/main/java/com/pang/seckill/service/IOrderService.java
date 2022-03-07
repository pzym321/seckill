package com.pang.seckill.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.seckill.pojo.Order;
import com.pang.seckill.pojo.User;
import com.pang.seckill.vo.GoodsVo;
import com.pang.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);

    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId,String path);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
