package com.twofan_weather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuyifan on 12/04/17.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;
    }

}
