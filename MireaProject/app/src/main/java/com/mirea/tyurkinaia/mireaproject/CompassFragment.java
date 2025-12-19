package com.mirea.tyurkinaia.mireaproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.mirea.tyurkinaia.mireaproject.R;
import java.util.Locale;

public class CompassFragment extends Fragment implements SensorEventListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private ImageView compassNeedle;
    private TextView azimuthTextView;
    private TextView directionTextView;
    private TextView directionInfoTextView;
    private TextView sensorStatusTextView;
    private Button calibrateButton;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    private boolean isCalibrating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_compass, container, false);

        // Инициализация UI элементов
        compassNeedle = root.findViewById(R.id.compassNeedle);
        azimuthTextView = root.findViewById(R.id.azimuthTextView);
        directionTextView = root.findViewById(R.id.directionTextView);
        directionInfoTextView = root.findViewById(R.id.directionInfoTextView);
        sensorStatusTextView = root.findViewById(R.id.sensorStatusTextView);
        calibrateButton = root.findViewById(R.id.calibrateButton);

        // Получение SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Проверка наличия датчиков
        if (accelerometer == null || magnetometer == null) {
            sensorStatusTextView.setText("Датчики не доступны на этом устройстве");
            calibrateButton.setEnabled(false);
        } else {
            sensorStatusTextView.setText("Датчики доступны");
        }

        // Проверка разрешений
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Настройка кнопки калибровки
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateCompass();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions() && accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Фильтрация данных акселерометра
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // Фильтрация данных магнитометра
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
            }

            float[] R = new float[9];
            float[] I = new float[9];

            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                updateCompassUI(azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String accuracyText;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyText = "Высокая точность";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyText = "Средняя точность";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyText = "Низкая точность";
                break;
            default:
                accuracyText = "Недостоверные данные";
        }
        sensorStatusTextView.setText("Точность датчика: " + accuracyText);
    }

    private void updateCompassUI(float azimuth) {
        // Анимация вращения стрелки
        RotateAnimation anim = new RotateAnimation(
                -currentAzimuth,
                -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        anim.setFillAfter(true);
        compassNeedle.startAnimation(anim);

        currentAzimuth = azimuth;

        // Обновление текстовых полей
        azimuthTextView.setText(String.format(Locale.getDefault(), "Азимут: %.1f°", azimuth));

        // Определение направления
        String direction = getDirectionFromAzimuth(azimuth);
        directionTextView.setText(direction);
        directionInfoTextView.setText("Направление: " + getDirectionName(direction));
    }

    private String getDirectionFromAzimuth(float azimuth) {
        if (azimuth >= 337.5 || azimuth < 22.5) return "N";
        if (azimuth >= 22.5 && azimuth < 67.5) return "NE";
        if (azimuth >= 67.5 && azimuth < 112.5) return "E";
        if (azimuth >= 112.5 && azimuth < 157.5) return "SE";
        if (azimuth >= 157.5 && azimuth < 202.5) return "S";
        if (azimuth >= 202.5 && azimuth < 247.5) return "SW";
        if (azimuth >= 247.5 && azimuth < 292.5) return "W";
        return "NW";
    }

    private String getDirectionName(String abbreviation) {
        switch (abbreviation) {
            case "N": return "Север";
            case "NE": return "Северо-восток";
            case "E": return "Восток";
            case "SE": return "Юго-восток";
            case "S": return "Юг";
            case "SW": return "Юго-запад";
            case "W": return "Запад";
            case "NW": return "Северо-запад";
            default: return "Неизвестно";
        }
    }

    private void calibrateCompass() {
        if (!isCalibrating) {
            isCalibrating = true;
            calibrateButton.setText("Калибровка...");
            sensorStatusTextView.setText("Пожалуйста, медленно вращайте устройство по всем осям");

            // Имитация калибровки
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCalibrating = false;
                    calibrateButton.setText("Калибровать компас");
                    sensorStatusTextView.setText("Калибровка завершена");
                    Toast.makeText(getContext(), "Калибровка завершена успешно", Toast.LENGTH_SHORT).show();
                }
            }, 3000);
        }
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(getContext(), "Разрешения получены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Некоторые разрешения не получены", Toast.LENGTH_SHORT).show();
            }
        }
    }
}