package com.example.localitation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvLatitude, tvLongitude;
    private EditText etLatitude, etLongitude;
    private Marker marker;
    private final Handler handler = new Handler();
    private Runnable locationUpdater;
    private static final int LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        mapView = findViewById(R.id.mapView);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        Button btnNavigate = findViewById(R.id.btnNavigate);

        // Configurar MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Configurar actualizaciones de ubicación
        locationUpdater = new Runnable() {
            @Override
            public void run() {
                // Verifica los permisos antes de solicitar actualizaciones de ubicación
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdate();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, REQUEST_LOCATION_PERMISSION_CODE);
                }
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        };
        handler.post(locationUpdater);

        // Botón para redireccionar a ubicación ingresada
        btnNavigate.setOnClickListener(v -> {
            String latitudeText = etLatitude.getText().toString().trim();
            String longitudeText = etLongitude.getText().toString().trim();

            // Validar si los campos están vacíos
            if (latitudeText.isEmpty() || longitudeText.isEmpty()) {
                // Mostrar un mensaje indicando que se deben ingresar los valores
                Toast.makeText(MainActivity.this, "Por favor ingresa latitud y longitud.", Toast.LENGTH_SHORT).show();
                return;  // Salir del método si los campos están vacíos
            }

            // Validar si la latitud y longitud son válidas
            try {
                // Aceptar números negativos y decimales
                double lat = Double.parseDouble(latitudeText);
                double lng = Double.parseDouble(longitudeText);

                // Validar que la latitud esté entre -90 y 90, y longitud entre -180 y 180
                if (lat < -90 || lat > 90) {
                    Toast.makeText(MainActivity.this, "Latitud debe estar entre -90 y 90.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (lng < -180 || lng > 180) {
                    Toast.makeText(MainActivity.this, "Longitud debe estar entre -180 y 180.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Mover el mapa a la ubicación ingresada
                moveToLocation(new LatLng(lat, lng));
            } catch (NumberFormatException e) {
                // Si ocurre una excepción al convertir los valores a double (por ejemplo, texto no numérico)
                Toast.makeText(MainActivity.this, "Por favor ingresa valores válidos de latitud y longitud.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocation(location);
                }
            }
        }, getMainLooper());
    }

    @SuppressLint("SetTextI18n")
    private void updateLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        tvLatitude.setText("Latitud: " + location.getLatitude());
        tvLongitude.setText("Longitud: " + location.getLongitude());

        if (marker != null) marker.remove();
        marker = googleMap.addMarker(new MarkerOptions().position(userLocation).title("Ubicación actual"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private void moveToLocation(LatLng latLng) {
        if (marker != null) marker.remove();
        marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Nueva ubicación"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Verifica los permisos antes de pedir la ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdate();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        handler.removeCallbacks(locationUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        handler.removeCallbacks(locationUpdater);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // Manejo de la respuesta de solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso es concedido, solicita las actualizaciones de ubicación
                requestLocationUpdate();
            } else {
                // Si el permiso es denegado, muestra un mensaje o maneja el error
            }
        }
    }
}
