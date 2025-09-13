package com.example.tomatomall.vo;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResultVO<T> implements Serializable {

    private String code;

    private String msg;

    private T data;

    public static <T> ResultVO<T> buildSuccess(T result) {
        return new ResultVO<T>("200", null, result);
    }

    public static <T> ResultVO<T> buildFailure(String msg, String code) {
        return new ResultVO<T>(code, msg, null);
    }
}
