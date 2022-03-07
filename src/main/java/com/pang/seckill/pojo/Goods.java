package com.pang.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String goods_id;
    private String goods_name;
    private double goods_price;
    private String goods_img;
    private int stock_cnt;

}
