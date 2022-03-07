package com.pang.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pang.seckill.pojo.Goods;
import com.pang.seckill.vo.GoodsVo;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long vo);
}
