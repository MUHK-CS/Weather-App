package hk.edu.ouhk.s313f_pj;

import android.util.Log;

import hk.edu.ouhk.s313f_pj.Forecast;
import org.json.JSONException;
import org.json.JSONObject;

public class Weather {

    private String temp, mintemp, maxtemp, icon, city, WeatherType, lon, lat;
    private int Condition;

    public static Weather fromJson(JSONObject httpresponse) {

        try {
            Weather data = new Weather();


            data.city = httpresponse.getString("name");
            data.Condition = httpresponse.getJSONArray("weather").getJSONObject(0).getInt("id");
            data.WeatherType = httpresponse.getJSONArray("weather").getJSONObject(0).getString("main");
            data.icon = updateWeatherIcon(data.Condition);
            double tempResult = httpresponse.getJSONObject("main").getDouble("temp") ;
            double minTemp = httpresponse.getJSONObject("main").getDouble("temp_min") ;
            double maxTemp = httpresponse.getJSONObject("main").getDouble("temp_max") ;
            
            int roundedTemp = (int) Math.rint(tempResult);
            int roundedMinTemp = (int) Math.rint(minTemp);
            int roundedMaxTemp = (int) Math.rint(maxTemp);

            data.lon = httpresponse.getJSONObject("coord").getString("lon");
            data.lat = httpresponse.getJSONObject("coord").getString("lat");

            data.temp = Integer.toString(roundedTemp);
            data.mintemp = Integer.toString(roundedMinTemp);
            data.maxtemp = Integer.toString(roundedMaxTemp);


            return data;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }

    public String getLon() {

        return lon;
    }
    public String getLat() {

        return lat;
    }

    public String getTemp() {

        return temp + "°C";
    }

    public String getminTemp() {
        return "ᐁ" + mintemp + "°C";
    }

    public String getmaxTemp() {
        return "ᐃ" + maxtemp + "°C";
    }

    public String geticon() {

        return icon;
    }

    public String getcity() {

        return city;
    }

    public String getWeatherType() {

        return WeatherType;
    }


    private static String updateWeatherIcon(int weatherID) {
        if (weatherID >= 0 && weatherID <= 300) {
            return "thunderstorm1";
        } else if (weatherID >= 300 && weatherID < 500) {
            return "lightrain";
        } else if (weatherID >= 500 && weatherID < 600) {
            return "shower";
        } else if (weatherID >= 600 && weatherID < 700) {
            return "snow2";
        } else if (weatherID >= 701 && weatherID <= 771) {
            return "fog";
        } else if (weatherID >= 772 && weatherID < 800) {
            return "overcast";
        } else if (weatherID == 800) {
            return "sunny";
        } else if (weatherID >= 801 && weatherID <= 804) {
            return "cloudy";
        }
     else if (weatherID >= 900 && weatherID <= 902) {
        return "thunderstorm1";
    }
        if (weatherID == 903) {
        return "snow1";
    }
        if (weatherID == 904) {
        return "sunny";
    }
        if (weatherID >= 905 && weatherID <= 1000) {
        return "thunderstrom2";
    }
        return "dunno";


    }


}
