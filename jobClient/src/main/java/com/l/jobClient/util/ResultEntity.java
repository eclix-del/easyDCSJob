package com.l.jobClient.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: lyd
 * @create: 2020-12-21 17:04
 **/

@Getter
@Setter
@AllArgsConstructor
public class ResultEntity {

    private Integer code;

    private String msg;

    private Object data;

}
