package com.errorxcode.jxinsta;

import okhttp3.ResponseBody;

public interface RequestCallback {
    void onSuccess(ResponseBody response);
    void onFailure(Exception e);
}
