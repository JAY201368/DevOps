package com.example.tomatomall.exception;

public class TomatoMallException extends RuntimeException {
    public TomatoMallException(String message) {
        super(message);
    }

    public static TomatoMallException nameOrPasswordError() {
        return new TomatoMallException("用户名或密码错误!");
    }

    public static TomatoMallException nameAlreadyExists() {
        return new TomatoMallException("用户名已存在");
    }

    public static TomatoMallException userNotExists() {
        return new TomatoMallException("用户不存在");
    }

    public static TomatoMallException invalidPhoneNumber() {
        return new TomatoMallException("手机号格式不正确");
    }
}
