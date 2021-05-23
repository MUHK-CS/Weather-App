package hk.edu.ouhk.s313f_pj;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    final String APP_ID = "c811cbe5a39d7107aad8fa609ebc5f48";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";


    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;

    final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
    String gps = LocationManager.GPS_PROVIDER;

    TextView CityName, weatherState, Temp, minTemp, maxTemp;
    ImageView weatherIcon;

    RelativeLayout CityFinder;
    RelativeLayout acitvityMain;

    LocationManager Location_M;
    LocationListener Location_L;
    SwipeListener swipeListener;

    private String lon, lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherState = findViewById(R.id.weatherCondition);
        Temp = findViewById(R.id.temp);
        minTemp = findViewById(R.id.min_temp);
        maxTemp = findViewById(R.id.max_temp);
        weatherIcon = findViewById(R.id.weatherIcon);
        CityFinder = findViewById(R.id.cityFinder);
        CityName = findViewById(R.id.cityName);
        acitvityMain = findViewById(R.id.relative_layout);
        swipeListener = new SwipeListener(acitvityMain);



        CityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, cityFinder.class);
                startActivity(intent);
            }
        });

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

                                            Intent intent = new Intent(MainActivity.this, cityFinder.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(MainActivity.this, Forecast.class);
                                            intent.putExtra("lon", lon);
                                            intent.putExtra("lat", lat);
                                            startActivity(intent);

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

    @Override
    protected void onResume() {
        super.onResume();
        Intent mIntent = getIntent();
        String city = mIntent.getStringExtra("City");
        if (city != null) {
            getWeatherForNewCity(city);
        } else {
            getWeatherForCurrentLocation();
        }

    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("units", "metric");
        params.put("appid", APP_ID);
        System.out.println(params);
        Networking(params);

    }


    private void getWeatherForCurrentLocation() {

        Location_M = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location_L = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params = new RequestParams();
                params.put("lat", Latitude);
                params.put("lon", Longitude);
                params.put("appid", APP_ID);
                params.put("units", "metric");
                System.out.println(params);
                Networking(params);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Location_M.requestLocationUpdates(gps, MIN_TIME, MIN_DISTANCE, Location_L);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getWeatherForCurrentLocation();
            } else {
            }
        }
    }


    private void Networking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Weather data = Weather.fromJson(response);

                updateUI(data);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });

    }

    private void updateUI(Weather weather) {
        Temp.setText(weather.getTemp());
        minTemp.setText(weather.getminTemp());
        maxTemp.setText(weather.getmaxTemp());
        CityName.setText(weather.getcity());
        weatherState.setText(weather.getWeatherType());
        int resourceID = getResources().getIdentifier(weather.geticon(), "drawable", getPackageName());
        weatherIcon.setImageResource(resourceID);

        lon = weather.getLon();
        lat = weather.getLat();

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        Temp.setAnimation(animation);
        weatherIcon.setAnimation(animation);
        minTemp.setAnimation(animation);
        maxTemp.setAnimation(animation);
        CityName.setAnimation(animation);
        weatherState.setAnimation(animation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Location_M != null) {
            Location_M.removeUpdates(Location_L);
        }
    }
}