package com.example.tomatomall.exception;

public class TomatoMallException extends RuntimeException {
    private Integer code;
    private String message;

    public TomatoMallException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public static TomatoMallException nameOrPasswordError() {
        return new TomatoMallException(400, "用户名或密码错误!");
    }

    public static TomatoMallException userNotExists() {
        return new TomatoMallException(404, "用户不存在");
    }

    public static TomatoMallException passwordError() {
        return new TomatoMallException(400, "密码错误");
    }

    public static TomatoMallException userAlreadyExists() {
        return new TomatoMallException(400, "用户名已被使用，请尝试其他用户名");
    }

    public static TomatoMallException productNotExists() {
        return new TomatoMallException(404, "商品不存在");
    }

    public static TomatoMallException invalidPhoneNumber() {
        return new TomatoMallException(400, "手机号格式不正确");
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
