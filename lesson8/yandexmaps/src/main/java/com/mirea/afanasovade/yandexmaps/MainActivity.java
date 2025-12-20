package com.mirea.afanasovade.yandexmaps;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.mirea.afanasovade.yandexmaps.databinding.ActivityMainBinding;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.user_location.UserLocationLayer;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MapView mapView;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private UserLocationLayer userLocationLayer;

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            enableUserLocationLayer();
        }
    }

    private void enableUserLocationLayer() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.contentMain.mapview;
        mapView.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856),
                        11.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocationLayer();
        }
    }


    @Override
    protected void onStart() {
        requestLocationPermissions();
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }
}