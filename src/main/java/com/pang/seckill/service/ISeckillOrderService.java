package com.pang.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.seckill.pojo.SeckillOrder;
import com.pang.seckill.pojo.User;


/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
