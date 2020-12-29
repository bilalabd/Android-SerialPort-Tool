package com.licheedev.serialtool.activity;

/**
 * Created by vavisa1 on 2/14/18.
 */

public enum ApiUrl {

    SCALE_URL;

    public String url() {
        switch (this) {
            case SCALE_URL: return "http://api.smartlifesys.com/barcodes";
            //case SCALE_URL: return "http://localhost:52222/barcodes";
            default: return "";
        }
    }

}
