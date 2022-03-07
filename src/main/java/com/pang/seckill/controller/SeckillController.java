package com.pang.seckill.controller;

import com.pang.seckill.config.AccessLimit;
import com.pang.seckill.exception.GlobalException;
import com.pang.seckill.pojo.*;
import com.pang.seckill.rabbitmq.MQSender;
import com.pang.seckill.service.*;
import com.pang.seckill.utils.JsonUtil;
import com.pang.seckill.vo.GoodsVo;
import com.pang.seckill.vo.RespBean;
import com.pang.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Controller
@RequestMapping(value = "/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //通过redis进行操作
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //通过内存标记，减少redis的访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId),
                Collections.EMPTY_LIST);
        if (stock < 0) {
            System.out.println("here");
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
//        orderService.seckill(user,goods);
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.Object2JsonStr(seckillMessage));
        return RespBean.success(0);
    }

    /**
     * @param user
     * @param goodsId
     * @return orderId: 成功  -1：秒杀失败 0：排队中
     */
    @ResponseBody
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    public RespBean result(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        System.out.println(orderId);
        return RespBean.success(orderId);
    }


    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {

        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Exprise", 0);
        //生成验证码 将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败" + e.getMessage());
        }

    }

    /**
     * 系统初始化 把商品库存数量加载到redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }

//
//@Controller
//@RequestMapping("/seckill")
//public class SeckillController {
//    @Autowired
//    private IGoodsService goodsService;
//    @Autowired
//    private ISeckillOrderService seckillOrderService;
//    @Autowired
//    private IOrderService orderService;
//
//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model, User user, Long goodsId) {
//        System.out.println("daozhele");
//        if (user == null) {
//            return "login";
//        }
//        model.addAttribute("user", user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "seckillFail";
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goods.getId()));
//        System.out.println(seckillOrder);
//        if (seckillOrder != null) {
//            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
//            return "seckillFail";
//        }
//        Order order = orderService.seckill(user, goods);
//        System.out.println("this is order" + order);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goods);
//        return "orderDetail";
//    }

//    @Controller
//    @RequestMapping("/seckill")
//    public class seckillController {
//
//        @Autowired
//        private IGoodsService goodsService;
//        @Autowired
//        private ISeckillOrderService seckillOrderService;
//        @Autowired
//        private IOrderService orderService;
//
//        @RequestMapping("/doSeckill")
//        public String doSeckill(Model model, User user, int goodsid) {
//            if (user == null) {
//                return "login";
//            }
//            model.addAttribute("user", user);
//            GoodsVo goods = goodsService.findGoodsVoByGoodsId((long) goodsid);
//
//            //判断库存
//            if (goods.getStockCount() < 1) {
//                model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK);
//                return "seckillFail";
//            }
//            //判断是否重复抢购
//            SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<>().eq("user_id", user.getId()).eq("goods_id", goodsid));
//            if (seckillOrder != null) {
//                model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR);
//                return "seckillFail";
//            }
//            Order order = orderService.seckill(user, goods);
//            model.addAttribute("order", order);
//            model.addAttribute("goods", goods);
//            return "orderDetail";
//        }
//    }
}
