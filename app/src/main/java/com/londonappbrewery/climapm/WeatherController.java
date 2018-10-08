package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    private static final String TAG = "Clima";
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    private static final String APP_ID = "b86b81bdf5420345b256dc9120b55884";
    // Time between location updates (5000 milliseconds or 5 seconds)
    private static final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    private static final float MIN_DISTANCE = 1000;
    // permission request code
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String[] LOCATION_PERMISSIOINS = {Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int CHANGE_CITY_ACTIVITY_REQUEST_CODE = 101;

    // get location from cell tower and Wi-Fi network
    private static final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    private LocationManager locationManager;
    private LocationListener locationListener;

    boolean useCurrentLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = findViewById(R.id.tempTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivityForResult(intent, CHANGE_CITY_ACTIVITY_REQUEST_CODE);
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged() callback received.");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                if (useCurrentLocation) {
                    requestWeatherReport(longitude, latitude);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "onProviderDisabled() callback received.");
            }
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setUpWeatherManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWeatherFromLastLocation();
    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    private void setUpWeatherManager() {

        if (locationManager == null) {
            Log.d(TAG, "Location manager is not initialized!");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), LOCATION_PERMISSIOINS[0])
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIOINS, LOCATION_REQUEST_CODE);
            return;
        }

        locationManager.requestLocationUpdates(
                LOCATION_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                locationListener);
    }

    private void setWeatherFromLastLocation() {
        if (!useCurrentLocation) return;

        if (locationManager == null) {
            Log.d(TAG, "Location manager is not initialized!");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), LOCATION_PERMISSIOINS[0])
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIOINS, LOCATION_REQUEST_CODE);
            return;
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
        if (lastKnownLocation == null) {
            return;
        }

        double longitude = lastKnownLocation.getLongitude();
        double latitude = lastKnownLocation.getLatitude();

        requestWeatherReport(String.valueOf(longitude), String.valueOf(latitude));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (LOCATION_REQUEST_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted.");
                setUpWeatherManager();
            } else {
                Log.d(TAG, "Location permission denied.");
            }
        }
    }

    private void requestWeatherReport(final String longitude, final String latitude) {
        if (longitude == null || latitude == null) return;
        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("lon", longitude);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    public void letsDoSomeNetworking(RequestParams params) {
        if (params == null) return;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "Success, JSON:" + response.toString());
                WeatherDataModel weatherDataModel = WeatherDataModel.fromJson(response);
                updateUI(weatherDataModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, "Fail " + errorResponse.toString());
                Log.d(TAG, "Status code: " + statusCode);
                Toast.makeText(WeatherController.this, "Request failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(final WeatherDataModel weatherDataModel) {
        if (weatherDataModel == null) return;
        final String temperatureStr = weatherDataModel.getmTemperature() + "°C";
        mTemperatureLabel.setText(temperatureStr);
        mCityLabel.setText(weatherDataModel.getmCity());
        final int iconResourceID = getResources().getIdentifier(
                weatherDataModel.getmIconName(),
                "drawable",
                getPackageName());
        mWeatherImage.setImageResource(iconResourceID);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (CHANGE_CITY_ACTIVITY_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                if (data != null) {
                    final String city = data.getStringExtra(Intent.EXTRA_TEXT);
                    if (city != null) {
                        getWeatherForNewCity(city);
                        useCurrentLocation = false;
                    } else {
                        useCurrentLocation = true;
                    }
                } else {
                    useCurrentLocation = true;
                }
            } else {
                useCurrentLocation = true;
            }
        }
    }
}
