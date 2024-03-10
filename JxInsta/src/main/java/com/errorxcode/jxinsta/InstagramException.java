package com.errorxcode.jxinsta;

public class InstagramException extends Exception {
    private final Reasons reason;

    public InstagramException(String message,Reasons reasons) {
        super(message);
        this.reason = reasons;
    }

    public InstagramException(String message) {
        super(message);
        this.reason = Reasons.UNKNOWN;
    }

    public Reasons getReason() {
        return reason;
    }

    public static enum Reasons{
        UNKNOWN, UNKNOWN_LOGIN_ERROR,
        INVALID_CREDENTIAL, INVALID_LOGIN_TYPE,INCORRECT_PASSWORD, INCORRECT_USERNAME,
        LOGIN_EXPIRED, CHECKPOINT_REQUIRED, RATE_LIMITED,TWO_FACTOR_REQUIRED,
        CSRF_AUTHENTICATION_FAILED,
    }
}
