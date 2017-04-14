package com.twofan_weather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuyifan on 12/04/17.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;
    }

}
