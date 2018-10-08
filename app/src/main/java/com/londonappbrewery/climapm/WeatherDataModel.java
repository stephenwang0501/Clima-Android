package com.londonappbrewery.climapm;

import org.json.JSONObject;

public class WeatherDataModel {

    private String mTemperature;
    private String mCity;
    private String mIconName;
    private int mCondition;

    // TODO: Create a WeatherDataModel from a JSON:
    public static WeatherDataModel fromJson(JSONObject jsonObject) {
        if (jsonObject == null) return null;
        WeatherDataModel weatherDataModel = new WeatherDataModel();
        try {
            weatherDataModel.mCity = jsonObject.getString("name");
            weatherDataModel.mCondition =
                    jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherDataModel.mIconName = updateWeatherIcon(weatherDataModel.mCondition);
            final double tempRes = (jsonObject.getJSONObject("main").getDouble("temp") - 273.15) * 9.0 / 5.0;
            final int tempInt = (int) Math.rint(tempRes);
            weatherDataModel.mTemperature = String.valueOf(tempInt);
        } catch (Exception e) {
            e.printStackTrace();
            weatherDataModel = null;
        }
        return weatherDataModel;
    }

    private static String updateWeatherIcon(int condition) {
        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }

        return "dunno";
    }

    public String getmTemperature() {
        return mTemperature;
    }

    public String getmCity() {
        return mCity;
    }

    public String getmIconName() {
        return mIconName;
    }
}
