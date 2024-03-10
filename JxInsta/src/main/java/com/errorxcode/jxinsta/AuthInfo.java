package com.errorxcode.jxinsta;

import org.jetbrains.annotations.NotNull;

public class AuthInfo {
    public String crsf;
    public String token;
    public String cookie;
    public String authorization;
    public JxInsta.LoginType loginType;

    public static AuthInfo forMobile(@NotNull String bearerToken) {
        var info = new AuthInfo();
        info.token = bearerToken;
        info.authorization = bearerToken;
        info.loginType = JxInsta.LoginType.APP_AUTHENTICATION;
        return info;
    }

    @Override
    public String toString() {
        return "AuthInfo{" +
                "crsf='" + crsf + '\'' +
                ", token='" + token + '\'' +
                ", cookie='" + cookie + '\'' +
                ", authorization='" + authorization + '\'' +
                ", loginType=" + loginType +
                '}';
    }
}
