// Result.java
package com.ai.study.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

/**
 * 成功响应的静态方法
 * @param data 返回的数据
 * @param <T> 泛型类型
 * @return 返回一个包含成功状态和数据的结果对象
 */
    public static <T> Result<T> success(T data) {
    // 创建一个新的Result对象
        Result<T> result = new Result<>();
    // 设置状态码为0，表示成功
        result.setCode(0);
    // 设置消息为"success"
        result.setMessage("success");
    // 设置返回的数据
        result.setData(data);
    // 返回结果对象
        return result;
    }

/**
 * 创建一个表示错误结果的通用方法
 * @param <T> 泛型类型，表示返回结果中数据的类型
 * @param message 错误信息描述
 * @return 返回一个包含错误信息的Result对象
 */
    public static <T> Result<T> error(String message) {
    // 创建一个新的Result对象
        Result<T> result = new Result<>();
    // 设置错误代码为1
        result.setCode(1);
    // 设置错误信息
        result.setMessage(message);
    // 返回包含错误信息的Result对象
        return result;
    }
}