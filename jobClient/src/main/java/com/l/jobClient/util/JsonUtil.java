package com.l.jobClient.util;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

    /**
     * 功能描述: 对象转json字符串
     * @Author  lyd
     * @Date  2021/2/18 15:28
     * @return
     **/
    public static String objectToJson(Object data){
        return JSONObject.toJSONString(data);
    }

    /**
     * 功能描述:  json转对象
     * @Author  lyd
     * @Date  2021/2/18 15:29
     * @return
     **/
    public static <T> T jsonToObject(String str, Class<T> clazz){
        return JSONObject.parseObject(str, clazz);
    }

}
