package com.pang.seckill.vo;

import com.pang.seckill.pojo.User;
import lombok.Data;

/**
 * @ClassName DetailVo
 * @Description 详情返回对象
 * @Author pang
 **/
@Data
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;
    private int secKillStatus;
    private int remainSeconds;
}
