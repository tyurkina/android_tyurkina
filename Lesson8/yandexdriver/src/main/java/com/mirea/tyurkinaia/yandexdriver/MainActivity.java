package com.mirea.tyurkinaia.yandexdriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.Toast;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.directions.driving.DrivingRouterType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;
import com.yandex.mapkit.layers.ObjectEvent;

import java.util.ArrayList;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        UserLocationObjectListener,
        DrivingSession.DrivingRouteListener {

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    private final String MAPKIT_API_KEY = "5d7c4a59-a1cc-4b2a-82b8-67d1343f68a7";
    private Point userLocation = null;
    private final Point PARENTS_HOUSE_LOCATION = new Point(53.267852, 50.193411);
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF00BBBB, 0xFF0000FF};

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private boolean locationPermissionGranted = false;
    private boolean isFirstLocationUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().setRotateGesturesEnabled(false);

        if (checkLocationPermissions()) {
            locationPermissionGranted = true;
            loadUserLocationLayer();
        } else {
            requestLocationPermissions();
        }

        addParentsHouseMarker();

        mapView.getMap().move(
                new CameraPosition(PARENTS_HOUSE_LOCATION, 12.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0.5f),  // Используем анимацию вместо null
                null  // cameraCallback остается null
        );
    }

    private void addParentsHouseMarker() {
        PlacemarkMapObject marker = mapObjects.addPlacemark(
                PARENTS_HOUSE_LOCATION,
                ImageProvider.fromResource(this, R.drawable.icon)
        );
        marker.addTapListener(new MapObjectTapListener() {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                showParentsHouseInfo();
                return true;
            }
        });
    }

    private void showParentsHouseInfo() {
        String info = "Дом родителей\n" +
                "Адрес: Самара, ул. 9 просека\n";

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }

    private void submitRouteRequest() {
        if (userLocation == null) {
            Toast.makeText(this, "Определяем ваше местоположение...", Toast.LENGTH_SHORT).show();
            return;
        }
        clearRoutes();

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(4);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();

        // Начальная точка - местоположение пользователя
        requestPoints.add(new RequestPoint(
                userLocation,
                RequestPointType.WAYPOINT,
                null,
                null
        ));

        // Конечная точка - дом родителей
        requestPoints.add(new RequestPoint(
                PARENTS_HOUSE_LOCATION,
                RequestPointType.WAYPOINT,
                null,
                null
        ));
        drivingSession = drivingRouter.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                this
        );
    }

    private void clearRoutes() {
        List<MapObject> objectsToRemove = new ArrayList<>();
        for (MapObject obj : objectsToRemove) {
            mapObjects.remove(obj);
        }
        addParentsHouseMarker();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
        // Добавляем новые маршруты на карту
        for (int i = 0; i < routes.size() && i < colors.length; i++) {
            DrivingRoute route = routes.get(i);
            mapObjects.addPolyline(route.getGeometry()).setStrokeColor(colors[i]);
        }

        if (!routes.isEmpty() && userLocation != null) {
            Point center = new Point(
                    (userLocation.getLatitude() + PARENTS_HOUSE_LOCATION.getLatitude()) / 2,
                    (userLocation.getLongitude() + PARENTS_HOUSE_LOCATION.getLongitude()) / 2
            );

            double latDiff = Math.abs(userLocation.getLatitude() - PARENTS_HOUSE_LOCATION.getLatitude());
            double lonDiff = Math.abs(userLocation.getLongitude() - PARENTS_HOUSE_LOCATION.getLongitude());
            double maxDiff = Math.max(latDiff, lonDiff);
            float zoom = (float) (14 - Math.min(maxDiff * 30, 6));

            mapView.getMap().move(
                    new CameraPosition(center, zoom, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 0.5f),
                    null
            );
        }

        Toast.makeText(this, "Построено " + routes.size() + " маршрута(ов) до дома родителей", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = "Неизвестная ошибка при построении маршрута";
        if (error instanceof RemoteError) {
            errorMessage = "Ошибка сервера при построении маршрута";
        } else if (error instanceof NetworkError) {
            errorMessage = "Проблемы с сетью. Проверьте подключение к интернету";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            loadUserLocationLayer();
            Toast.makeText(this, "Разрешения получены. Определяем ваше местоположение...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Для построения маршрута нужны разрешения на местоположение", Toast.LENGTH_LONG).show();
            mapView.getMap().move(
                    new CameraPosition(PARENTS_HOUSE_LOCATION, 12.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 0.5f),
                    null
            );
        }
    }

    private void loadUserLocationLayer() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.5)),
                new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.83))
        );
        userLocationView.getArrow().setIcon(ImageProvider.fromResource(this, R.drawable.icon));
        userLocationView.getPin().setIcon(ImageProvider.fromResource(this, R.drawable.icon));
        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE & 0x99ffffff);
        userLocation = userLocationView.getPin().getGeometry();
        if (isFirstLocationUpdate && userLocation != null) {
            isFirstLocationUpdate = false;
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Центрируем карту на пользователе
                    mapView.getMap().move(
                            new CameraPosition(userLocation, 15.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 0.5f),  // Добавляем анимацию
                            null
                    );

                    // Строим маршрут
                    submitRouteRequest();
                }
            }, 1000);
        }
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
        userLocation = userLocationView.getPin().getGeometry();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}