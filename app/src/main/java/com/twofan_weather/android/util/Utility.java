package com.twofan_weather.android.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.twofan_weather.android.db.City;
import com.twofan_weather.android.db.County;
import com.twofan_weather.android.db.Province;
import com.twofan_weather.android.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by liuyifan on 11/04/17.
 */

public class Utility {

    private static int cityId;

    private static int provinceId;

    private static String url = "http://guolin.tech/api/china/";

    /***
     * 解析处理服务器返回的省级数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provincesObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provincesObject.getString("name"));
                    province.setProvinceCode(provincesObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /***
     * 解析处理服务器返回的市级数据
     * @param response
     * @return
     */
    public static boolean handleCityResponse(String response,int provinceId) {
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /***
     * 解析处理服务器返回的县级数据
     * @param response
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId) {
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return  new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    /***
     * 持久化所有的城市
     */
    public static void persistenceAllCities() {
        DataSupport.deleteAll(City.class);

        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                String provinceInfos = response.body().string();
                try {
                    JSONArray allProvinces = new JSONArray(provinceInfos);
                    for(int i=0;i<allProvinces.length();i++){
                        JSONObject provincesObject = allProvinces.getJSONObject(i);
                        provinceId = provincesObject.getInt("id");
                        HttpUtil.sendOkHttpRequest(url+provinceId, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String cityInfos = response.body().string();
                                try {
                                    JSONArray allCities = new JSONArray(cityInfos);
                                    for(int i=0;i<allCities.length();i++){
                                        JSONObject citiesObject = allCities.getJSONObject(i);
                                        cityId = citiesObject.getInt("id");
                                        HttpUtil.sendOkHttpRequest(url+provinceId+"/"+cityId, new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                e.printStackTrace();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                String countyInfos = response.body().string();
                                                try {
                                                    JSONArray allCounties = new JSONArray(countyInfos);
                                                    for(int i=0;i<allCounties.length();i++){
                                                        JSONObject countyObject = allCounties.getJSONObject(i);
                                                        County county = new County();
                                                        county.setCountyName(countyObject.getString("name"));
                                                        county.setWeatherId(countyObject.getString("weather_id"));
                                                        county.setCityId(cityId);
                                                        county.save();
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    }



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public static String getWeatherIdByCityName(String name) {
        County county = DataSupport.where("countyName=?", name).findFirst(County.class);
        return county == null ? "" : county.getWeatherId();
    }
}
