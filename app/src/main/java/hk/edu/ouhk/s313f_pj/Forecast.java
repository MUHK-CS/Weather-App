package hk.edu.ouhk.s313f_pj;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;


public class Forecast extends AppCompatActivity {

    final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/onecall";
    final String APP_ID = "c811cbe5a39d7107aad8fa609ebc5f48";

    private String temp, mintemp,maxtemp, icon, city, WeatherType, lon, lat;
    private int Condition;
    private long dt;
    private static String[] MAXTEMP = new String[7];
    private static String[] MINTEMP = new String[7];
    private static String[] DATE = new String[7];
    private static int[] ICON = new int[7];
    final LoadingDialog loadingDialog = new LoadingDialog(Forecast.this);

    ListView listView;

    TextView date1,maxTemp,minTemp;
    RelativeLayout RLforecast;
    SwipeListener swipeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Intent xIntent = getIntent();
        lon = xIntent.getStringExtra("lon");
        lat = xIntent.getStringExtra("lat");

        minTemp = findViewById(R.id.row_mintemp);
        maxTemp = findViewById(R.id.row_maxtemp);

        date1 = findViewById(R.id.date);
        RLforecast = findViewById(R.id.forecast_rl);
        swipeListener = new SwipeListener(RLforecast);

        listView = findViewById(R.id.listview);
        MyAdapter adapter = new MyAdapter(this,DATE,MINTEMP,MAXTEMP,ICON);
        listView.setAdapter(adapter);
        loadingDialog.startLoadingDialog();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                loadingDialog.dismissDialog();

            }
        }, 5000);

    }



    private class SwipeListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        SwipeListener(View view) {
            int threshold = 100;
            int velocity_threshold = 100;

            GestureDetector.SimpleOnGestureListener listener =
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                            float xDiff = e2.getX() - e1.getY();
                            float yDiff = e2.getY() - e1.getY();
                            try {
                                if (Math.abs(xDiff) > Math.abs(yDiff)) {
                                    if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                        if (xDiff < 0) {
                                            finish();
                                        }
                                        return true;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                    };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    }

    public static Forecast fromJson(JSONObject httpresponse) {

        try {
            Forecast data = new Forecast();


            for (int count = 1; count < 8; count++){
                data.dt = httpresponse.getJSONArray("daily").getJSONObject(count).getLong("dt");
                double min_temp = httpresponse.getJSONArray("daily").getJSONObject(count).getJSONObject("temp").getDouble("min") ;
                double max_temp = httpresponse.getJSONArray("daily").getJSONObject(count).getJSONObject("temp").getDouble("max") ;
                data.Condition = httpresponse.getJSONArray("daily").getJSONObject(count).getJSONArray("weather").getJSONObject(0).getInt("id");// dllm
                int roundedMaxTemp = (int) Math.rint(max_temp);
                int roundedMinTemp = (int) Math.rint(min_temp);
                data.maxtemp = Integer.toString(roundedMaxTemp);
                data.mintemp = Integer.toString(roundedMinTemp);

                updateWeatherIcon(data.Condition, ICON,count);

                MAXTEMP[count-1] = data.maxtemp + "°C";
                MINTEMP[count-1] = data.mintemp + "°C";
                DATE[count-1]= data.getDate();

            }

            return data;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        getWeatherForNewCity();

    }

    private void getWeatherForNewCity() {
        RequestParams params = new RequestParams();
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("units","metric");
        params.put("exclude", "minutely,hourly");
        params.put("appid", APP_ID);

        Networking(params);

    }
    private void Networking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(FORECAST_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Forecast data = Forecast.fromJson(response);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        String rdate[];
        String mxTemp[];
        String mnTemp[];
        int happy[];

        MyAdapter (Context c,String rdate[] ,String temp2[],String temp[],int happy[]) {
            super(c, R.layout.row,R.id.row_maxtemp, temp);
            this.context = c;
            this.rdate = rdate;
            this.mxTemp = temp;
            this.mnTemp = temp2;
            this.happy = happy;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row, parent, false);

            TextView row_maxtemp = row.findViewById(R.id.row_maxtemp);
            TextView row_mintemp = row.findViewById(R.id.row_mintemp);
            TextView row_date = row.findViewById(R.id.date);
            ImageView row_icon = row.findViewById(R.id.ricon);
            row_maxtemp.setText(mxTemp[position]);
            row_mintemp.setText(mnTemp[position]);
            row_date.setText(rdate[position]);
            row_icon.setImageResource(happy[position]);

            return row;
        }
    }
    private static void updateWeatherIcon(int weatherID, int[]array, int count) {
        if (weatherID >= 0 && weatherID < 300) {
            array[count-1]= R.drawable.thunderstorm2;
        } else if (weatherID >= 300 && weatherID <500) {
            array[count-1]= R.drawable.lightrain;
        } else if (weatherID >= 500 && weatherID < 600) {
            array[count-1]= R.drawable.shower;
        } else if (weatherID >= 600 && weatherID <= 700) {
            array[count-1]= R.drawable.snow2;
        } else if (weatherID >= 701 && weatherID <= 771) {
            array[count-1]= R.drawable.fog;
        } else if (weatherID >= 772 && weatherID < 800) {
            array[count-1]= R.drawable.overcast;
        } else if (weatherID == 800) {
            array[count-1]= R.drawable.sunny;
        } else if (weatherID >= 801 && weatherID <= 804) {
            array[count-1]= R.drawable.cloudy;
        }         else if (weatherID >= 900 && weatherID <= 902) {
        array[count-1]= R.drawable.thunderstorm1;
    }
        if (weatherID == 903) {
        array[count-1]= R.drawable.snow1;
    }
        if (weatherID == 904) {
        array[count-1]= R.drawable.sunny;
    }
        if (weatherID >= 905 && weatherID <= 1000) {
        array[count-1]= R.drawable.thunderstorm2;
    }

    }

    public String getDate() {
        Date date = new Date(dt*1000L);
        SimpleDateFormat jdf = new SimpleDateFormat("dd/MM");
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String java_date = jdf.format(date);
        return java_date;
    }


}

