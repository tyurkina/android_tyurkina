package com.mirea.tyurkinaia.osmmaps;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.Animation;
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
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;

import com.mirea.tyurkinaia.osmmaps.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        UserLocationObjectListener,
        DrivingSession.DrivingRouteListener {

    private MapView mapView = null;
    private ActivityMainBinding binding;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;
    private List<PlacemarkMapObject> markers = new ArrayList<>();
    private Point startPoint, endPoint;
    private String startPointName, endPointName;
    private FloatingActionButton fabLocation, fabRoute;

    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private boolean isFirstLocationUpdate = true;
    private Point userLocation = null;

    private int[] colors = {
            Color.parseColor("#FF0000"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#FF00FF"),
            Color.parseColor("#00FFFF")
    };

    private static final String YANDEX_MAPKIT_API_KEY = "3f789182-85fb-4813-92aa-9ac503cd39cd";

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissionsResult -> {
                        boolean allGranted = true;
                        for (Boolean isGranted : permissionsResult.values()) {
                            if (!isGranted) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            setupLocationOverlay();
                        } else {
                            Toast.makeText(this, "Некоторые разрешения не предоставлены", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Инициализация MapKit перед super.onCreate()
        MapKitFactory.setApiKey(YANDEX_MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        fabLocation = binding.fabLocation;
        fabRoute = binding.fabRoute;

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        mapView.getMap().setZoomGesturesEnabled(true);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().setScrollGesturesEnabled(true);

        initializeMap();
        setupListeners();
        checkPermissions();

        FloatingActionButton fabClearRoutes = binding.fabClearRoutes;
        fabClearRoutes.setOnClickListener(v -> {
            clearRoutes();
            resetSelectedPoints();
            Toast.makeText(this, "Маршруты очищены, точки сброшены", Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeMap() {
        // Устанавливаем начальную позицию карты на Москву
        mapView.getMap().move(
                new CameraPosition(
                        new Point(55.7558, 37.6173),
                        12.0f,
                        0.0f,
                        0.0f
                ),
                new Animation(Animation.Type.SMOOTH, 1),
                null
        );

        mapView.getMap().setZoomGesturesEnabled(true);
        mapView.getMap().setRotateGesturesEnabled(true);
        mapView.getMap().setScrollGesturesEnabled(true);

        addInterestingPlaces();
    }

    private void setupListeners() {
        fabLocation.setOnClickListener(v -> {
            if (userLocationLayer != null && userLocationLayer.cameraPosition() != null) {
                mapView.getMap().move(
                        userLocationLayer.cameraPosition(),
                        new Animation(Animation.Type.SMOOTH, 1),
                        null
                );
            } else {
                Toast.makeText(this, "Местоположение не определено", Toast.LENGTH_SHORT).show();
            }
        });

        fabRoute.setOnClickListener(v -> {
            if (startPoint != null && endPoint != null) {
                submitRouteRequest();
            } else {
                String message = "Выберите точки маршрута:\n";
                if (startPoint == null) message += "• Начальную точку\n";
                if (endPoint == null) message += "• Конечную точку\n";
                message += "\nНажмите на маркер и выберите в диалоговом окне";

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addInterestingPlaces() {
        markers.clear();
        createMarker(
                new Point(55.7539, 37.6208),
                "Красная площадь",
                "Главная площадь Москвы, объект Всемирного наследия ЮНЕСКО",
                R.drawable.ic_landmark
        );

        createMarker(
                new Point(55.8265, 37.6385),
                "ВДНХ",
                "Выставка достижений народного хозяйства, крупнейший экспоцентр России",
                R.drawable.ic_park
        );

        createMarker(
                new Point(55.7039, 37.5288),
                "МГУ",
                "Московский государственный университет, главное здание",
                R.drawable.ic_university
        );

        createMarker(
                new Point(55.7295, 37.6010),
                "Парк Горького",
                "Центральный парк культуры и отдыха имени Горького",
                R.drawable.ic_park
        );

        createMarker(
                new Point(55.7495, 37.5399),
                "Москва-Сити",
                "Московский международный деловой центр",
                R.drawable.ic_skyscraper
        );

        createMarker(
                new Point(55.717934, 37.551932),
                "Лужники",
                "Главный стадион Москвы",
                R.drawable.ic_luzha
        );

        Log.d("Markers", "Добавлено " + markers.size() + " маркеров");
    }

    private PlacemarkMapObject createMarker(Point position, String title, String description, int iconResId) {
        PlacemarkMapObject marker = mapObjects.addPlacemark(position);
        marker.setUserData(new MarkerData(title, description, iconResId));
        try {
            if (iconResId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconResId);
                if (bitmap != null) {
                    marker.setIcon(ImageProvider.fromBitmap(bitmap));
                    com.yandex.mapkit.map.IconStyle iconStyle = new com.yandex.mapkit.map.IconStyle();
                    iconStyle.setAnchor(new PointF(0.5f, 1.0f));
                    iconStyle.setScale(0.5f);
                    marker.setIconStyle(iconStyle);

                } else {
                    setDefaultIcon(marker);
                }
            } else {
                setDefaultIcon(marker);
            }
        } catch (Exception e) {
            setDefaultIcon(marker);
        }

        marker.addTapListener(new MapObjectTapListener() {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                if (mapObject instanceof PlacemarkMapObject) {
                    PlacemarkMapObject clickedMarker = (PlacemarkMapObject) mapObject;
                    MarkerData data = (MarkerData) clickedMarker.getUserData();
                    if (data != null) {
                        showMarkerDialog(clickedMarker, data);
                    }
                }
                return true;
            }
        });

        markers.add(marker);
        return marker;
    }

    private void setDefaultIcon(PlacemarkMapObject marker) {
        int size = 50;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.RED);

        marker.setIcon(ImageProvider.fromBitmap(bitmap));

        com.yandex.mapkit.map.IconStyle iconStyle = new com.yandex.mapkit.map.IconStyle();
        iconStyle.setAnchor(new PointF(0.5f, 0.5f));
        iconStyle.setScale(1.0f);
        marker.setIconStyle(iconStyle);
    }

    private void showMarkerDialog(PlacemarkMapObject marker, MarkerData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(data.getTitle())
                .setMessage(data.getDescription())
                .setPositiveButton("Сделать начальной точкой", (dialog, which) -> {
                    setStartPoint(marker, data);
                })
                .setNegativeButton("Сделать конечной точкой", (dialog, which) -> {
                    setEndPoint(marker, data);
                })
                .setNeutralButton("Отмена", null)
                .show();
    }

    private void setStartPoint(PlacemarkMapObject marker, MarkerData data) {
        startPoint = marker.getGeometry();
        startPointName = data.getTitle();

        try {
            int size = 60;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.GREEN);

            marker.setIcon(ImageProvider.fromBitmap(bitmap));

            com.yandex.mapkit.map.IconStyle iconStyle = new com.yandex.mapkit.map.IconStyle();
            iconStyle.setAnchor(new PointF(0.5f, 0.5f));
            iconStyle.setScale(1.2f);
            marker.setIconStyle(iconStyle);
        } catch (Exception e) {
            Log.e("Marker", "Ошибка установки иконки старта", e);
        }

        Toast.makeText(this, "Начальная точка: " + data.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d("Route", "Установлена начальная точка: " + data.getTitle());

        if (endPoint != null) {
            Toast.makeText(this, "Готово к построению маршрута: " + startPointName + " → " + endPointName,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setEndPoint(PlacemarkMapObject marker, MarkerData data) {
        endPoint = marker.getGeometry();
        endPointName = data.getTitle();

        try {
            int size = 60;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLUE);

            marker.setIcon(ImageProvider.fromBitmap(bitmap));

            com.yandex.mapkit.map.IconStyle iconStyle = new com.yandex.mapkit.map.IconStyle();
            iconStyle.setAnchor(new PointF(0.5f, 0.5f));
            iconStyle.setScale(1.2f);
            marker.setIconStyle(iconStyle);
        } catch (Exception e) {
            Log.e("Marker", "Ошибка установки иконки конца", e);
        }

        Toast.makeText(this, "Конечная точка: " + data.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d("Route", "Установлена конечная точка: " + data.getTitle());

        if (startPoint != null) {
            Toast.makeText(this, "Готово к построению маршрута: " + startPointName + " → " + endPointName,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void submitRouteRequest() {
        if (startPoint == null || endPoint == null) {
            String message = "Выберите обе точки маршрута:\n";
            if (startPoint == null) message += "• Начальную точку\n";
            if (endPoint == null) message += "• Конечную точку\n";

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

        String routeInfo = String.format("Строим маршрут: %s → %s",
                startPointName != null ? startPointName : "Точка 1",
                endPointName != null ? endPointName : "Точка 2");

        Toast.makeText(this, routeInfo, Toast.LENGTH_SHORT).show();
        Log.d("Route", "Построение маршрута от: " + startPoint + " до: " + endPoint);

        clearRoutes();

        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(4);

        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                startPoint,
                RequestPointType.WAYPOINT,
                null,
                null
        ));
        requestPoints.add(new RequestPoint(
                endPoint,
                RequestPointType.WAYPOINT,
                null,
                null
        ));

        Toast.makeText(this, "Построение маршрута...", Toast.LENGTH_SHORT).show();

        drivingSession = drivingRouter.requestRoutes(
                requestPoints,
                drivingOptions,
                vehicleOptions,
                this
        );
    }

    private void clearRoutes() {
        mapObjects.clear();
        markers.clear();
        addInterestingPlaces();
        if (startPoint != null) {
            for (PlacemarkMapObject marker : markers) {
                Point point = marker.getGeometry();
                if (point.getLatitude() == startPoint.getLatitude() &&
                        point.getLongitude() == startPoint.getLongitude()) {
                    MarkerData data = (MarkerData) marker.getUserData();
                    setStartPoint(marker, data);
                    break;
                }
            }
        }

        if (endPoint != null) {
            for (PlacemarkMapObject marker : markers) {
                Point point = marker.getGeometry();
                if (point.getLatitude() == endPoint.getLatitude() &&
                        point.getLongitude() == endPoint.getLongitude()) {
                    MarkerData data = (MarkerData) marker.getUserData();
                    setEndPoint(marker, data);
                    break;
                }
            }
        }
    }

    private void resetSelectedPoints() {
        startPoint = null;
        endPoint = null;
        startPointName = null;
        endPointName = null;
        for (PlacemarkMapObject marker : markers) {
            MarkerData data = (MarkerData) marker.getUserData();
            if (data != null) {
                try {
                    if (data.getIconResId() != 0) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), data.getIconResId());
                        if (bitmap != null) {
                            marker.setIcon(ImageProvider.fromBitmap(bitmap));
                            com.yandex.mapkit.map.IconStyle iconStyle = new com.yandex.mapkit.map.IconStyle();
                            iconStyle.setAnchor(new PointF(0.5f, 1.0f));
                            iconStyle.setScale(0.5f);
                            marker.setIconStyle(iconStyle);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Marker", "Ошибка восстановления иконки", e);
                }
            }
        }
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> routes) {
        if (routes.isEmpty()) {
            Toast.makeText(this, "Маршруты не найдены", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < routes.size() && i < colors.length; i++) {
            DrivingRoute route = routes.get(i);
            PolylineMapObject polyline = mapObjects.addPolyline(route.getGeometry());
            polyline.setStrokeColor(colors[i]);
            polyline.setStrokeWidth(5.0f);

            double distanceKm = route.getMetadata().getWeight().getDistance().getValue() / 1000.0;
            long timeMinutes = (long) (route.getMetadata().getWeight().getTime().getValue() / 60.0);

            String routeInfo = String.format("Маршрут %d: %.1f км, %d мин", i + 1, distanceKm, timeMinutes);
            polyline.setUserData(routeInfo);

            final int routeIndex = i;
            polyline.addTapListener(new MapObjectTapListener() {
                @Override
                public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                    if (mapObject instanceof PolylineMapObject) {
                        String info = (String) mapObject.getUserData();
                        Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        if (startPoint != null && endPoint != null) {
            Point center = new Point(
                    (startPoint.getLatitude() + endPoint.getLatitude()) / 2,
                    (startPoint.getLongitude() + endPoint.getLongitude()) / 2
            );

            double latDiff = Math.abs(startPoint.getLatitude() - endPoint.getLatitude());
            double lonDiff = Math.abs(startPoint.getLongitude() - endPoint.getLongitude());
            double maxDiff = Math.max(latDiff, lonDiff);
            float zoom = (float) (14 - Math.min(maxDiff * 30, 6));

            mapView.getMap().move(
                    new CameraPosition(center, zoom, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 1.0f),
                    null
            );
        }

        showRoutesInfo(routes.size());
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

        drawSimpleRoute();
    }

    private void drawSimpleRoute() {
        if (startPoint != null && endPoint != null) {
            List<Point> points = new ArrayList<>();
            points.add(startPoint);
            points.add(endPoint);

            PolylineMapObject polyline = mapObjects.addPolyline(new com.yandex.mapkit.geometry.Polyline(points));
            polyline.setStrokeColor(Color.BLUE);
            polyline.setStrokeWidth(3.0f);
            polyline.setUserData("Упрощенный маршрут");

            Toast.makeText(this, "Используется упрощенный маршрут", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRoutesInfo(int routeCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Маршруты построены")
                .setMessage("Найдено " + routeCount + " маршрута(ов).\n\n" +
                        "От: " + (startPointName != null ? startPointName : "Начальная точка") + "\n" +
                        "До: " + (endPointName != null ? endPointName : "Конечная точка") + "\n\n" +
                        "Красный: Основной маршрут\n" +
                        "Зеленый: Альтернативный маршрут 1\n" +
                        "Синий: Альтернативный маршрут 2\n" +
                        "Пурпурный: Альтернативный маршрут 3\n\n" +
                        "Коснитесь линии маршрута для получения информации.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            setupLocationOverlay();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void setupLocationOverlay() {
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);

        if (mapView.getWidth() > 0 && mapView.getHeight() > 0) {
            userLocationLayer.setAnchor(
                    new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.5)),
                    new PointF((float)(mapView.getWidth() * 0.5), (float)(mapView.getHeight() * 0.83))
            );
        }
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        Log.d("MapKit", "Location marker added");

        userLocation = userLocationView.getPin().getGeometry();

        if (isFirstLocationUpdate && userLocation != null) {
            isFirstLocationUpdate = false;

            if (endPoint != null) {
                startPoint = userLocation;
                startPointName = "Ваше местоположение";

                mapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitRouteRequest();
                    }
                }, 2000);
            }
        }
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {
        Log.d("MapKit", "Location marker removed");
    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {
        userLocation = userLocationView.getPin().getGeometry();
        Log.d("MapKit", "Location updated: " + userLocation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    private static class MarkerData {
        private String title;
        private String description;
        private int iconResId;

        public MarkerData(String title, String description, int iconResId) {
            this.title = title;
            this.description = description;
            this.iconResId = iconResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getIconResId() {
            return iconResId;
        }
    }
}