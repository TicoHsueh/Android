package com.hb.elevator;

import com.chinamobile.iot.onenet.OneNetApiCallback;

public class DataCallBack implements OneNetApiCallback {
    String response = "+";
    @Override
    public void onSuccess(String response) {
        this.response = response;
    }

    @Override
    public void onFailed(Exception e) {
        response = "Failed";
    }

    public String getResponse(){
        return this.response;
    }
}
