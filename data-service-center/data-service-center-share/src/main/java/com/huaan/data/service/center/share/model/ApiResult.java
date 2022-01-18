package com.huaan.data.service.center.share.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用的http请求返回
 *
 * @author fengzheng
 * @since 2020/10/21
 */
@Getter
@Setter
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = -3715769113625033403L;

    private boolean success;

    private String code;

    private T data;

    private String message;

    public static ApiResult success(Object data) {
        ApiResult result = new ApiResult();
        result.setSuccess(true);
        result.setCode("200");
        result.setData(data);
        result.setMessage("请求成功");
        return result;
    }

    public static ApiResult success() {
        ApiResult result = new ApiResult();
        result.setSuccess(true);
        result.setCode("200");
        result.setMessage("请求成功");
        return result;
    }

    public static ApiResult fail(String code, String msg) {
        ApiResult result = new ApiResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }
}