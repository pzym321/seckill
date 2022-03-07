package com.pang.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName GoodsVo
 * @Description 商品返回对象
 * @Author cheny
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo {
    private Long id;
    private String goodsName;
    private BigDecimal goods_price;
    private String goodsImg;

    private BigDecimal seckillPrice;

    private Integer stockCount;

    private Date startDate;

    private Date endDate;

}
