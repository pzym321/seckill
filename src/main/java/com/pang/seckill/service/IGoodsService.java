package com.pang.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pang.seckill.pojo.Goods;
import com.pang.seckill.vo.GoodsVo;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
