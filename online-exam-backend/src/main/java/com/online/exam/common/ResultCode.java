package com.online.exam.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    TOKEN_EXPIRED(401, "Token已过期"),
    USER_EXIST(1001, "用户名已存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_NOT_FOUND(1003, "用户不存在"),
    EXAM_ALREADY_SUBMITTED(2001, "考试已提交"),
    EXAM_TIME_OUT(2002, "考试已超时"),
    PAPER_NOT_FOUND(2003, "试卷不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
