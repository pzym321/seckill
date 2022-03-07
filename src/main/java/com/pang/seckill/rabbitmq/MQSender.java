package com.pang.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName MQSender
 * @Description 消息发送者
 **/
@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    //发送秒杀信息
    public void sendSeckillMessage(String msg){
        log.info("发送消息："+msg);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",msg);
    }


//
//    public void send(Object msg) {
//        log.info("发送消息:" + msg);
//        rabbitTemplate.convertAndSend("fanoutExchange","", msg);
//    }

}
