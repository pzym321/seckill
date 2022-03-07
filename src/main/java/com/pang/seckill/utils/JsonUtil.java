package com.pang.seckill.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName JsonUtil
 * @Description json工具类
 **/
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换成json字符串
     * @param obj
     * @return
     */
    public static String Object2JsonStr(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json转换成对象
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T>T jsonStr2Object(String jsonStr,Class<T> clazz){
        try {
            return objectMapper.readValue(jsonStr.getBytes("UTF-8"),clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json转换成list
     * @param jsonStr
     * @param beantype
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(String jsonStr,Class<T> beantype){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class,beantype);
        try {
            List<T> list = objectMapper.readValue(jsonStr,javaType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
