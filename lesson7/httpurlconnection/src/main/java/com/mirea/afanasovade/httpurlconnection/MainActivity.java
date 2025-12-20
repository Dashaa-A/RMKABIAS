package com.mirea.afanasovade.httpurlconnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button buttonGetInfo;
    private TextView textViewIP;
    private TextView textViewCity;
    private TextView textViewRegion;
    private TextView textViewCountry;
    private TextView textViewLocation;
    private TextView textViewWeather;

    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGetInfo = findViewById(R.id.buttonGetInfo);
        textViewIP = findViewById(R.id.textViewIP);
        textViewCity = findViewById(R.id.textViewCity);
        textViewRegion = findViewById(R.id.textViewRegion);
        textViewCountry = findViewById(R.id.textViewCountry);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewWeather = findViewById(R.id.textViewWeather);

        buttonGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null) {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }

                if (networkinfo != null && networkinfo.isConnected()) {
                    new DownloadPageTask().execute("https://ipinfo.io/json");
                } else {
                    Toast.makeText(MainActivity.this, "Нет интернета", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewIP.setText("Загружаем...");
            textViewCity.setText("-");
            textViewRegion.setText("-");
            textViewCountry.setText("-");
            textViewLocation.setText("-");
            textViewWeather.setText("-");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, result);
            try {
                JSONObject responseJson = new JSONObject(result);

                String ip = responseJson.getString("ip");
                String city = responseJson.getString("city");
                String region = responseJson.getString("region");
                String country = responseJson.getString("country");
                String loc = responseJson.getString("loc");

                textViewIP.setText(ip);
                textViewCity.setText(city);
                textViewRegion.setText(region);
                textViewCountry.setText(country);
                textViewLocation.setText(loc);

                String[] coordinates = loc.split(",");
                latitude = coordinates[0];
                longitude = coordinates[1];

                new DownloadWeatherTask().execute(
                        "https://api.open-meteo.com/v1/forecast?latitude=" +
                                latitude + "&longitude=" + longitude + "&current_weather=true"
                );

            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Weather: " + result);
            try {
                JSONObject responseJson = new JSONObject(result);
                JSONObject currentWeather = responseJson.getJSONObject("current_weather");

                String temperature = currentWeather.getString("temperature");
                String windspeed = currentWeather.getString("windspeed");

                String weatherInfo = "Temperature: " + temperature + "°C\n" +
                        "Wind speed: " + windspeed + " km/h";

                textViewWeather.setText(weatherInfo);

            } catch (JSONException e) {
                e.printStackTrace();
                textViewWeather.setText("Error loading weather");
            }
            super.onPostExecute(result);
        }
    }

    private String downloadIpInfo(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = inputStream.read()) != -1) {
                    bos.write(read);
                }
                bos.close();
                data = bos.toString();
            } else {
                data = connection.getResponseMessage() + ". Error Code: " + responseCode;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }
}