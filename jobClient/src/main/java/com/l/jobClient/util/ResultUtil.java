package com.l.jobClient.util;


/**
 * @author: lyd
 * @create: 2020-12-21 17:05
 **/

public class ResultUtil {

    public static ResultEntity result(Integer code, String msg, Object data) {
        return new ResultEntity(code, msg, data);
    }

    public static ResultEntity success(Object data) {
        return new ResultEntity(200, "", data);
    }

    public static ResultEntity error(String msg) {
        return new ResultEntity(500, msg, null);
    }

    public static ResultEntity noPermissions(String msg) {
        return new ResultEntity(403, msg, null);
    }

    public static ResultEntity noLogin(String msg) {
        return new ResultEntity(401, msg, null);
    }

}
