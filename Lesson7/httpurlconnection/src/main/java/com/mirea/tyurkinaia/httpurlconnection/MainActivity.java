package com.mirea.tyurkinaia.httpurlconnection;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private EditText editTextLatitude, editTextLongitude;
    private EditText editTextLatitudeDisplay, editTextLongitudeDisplay;
    private EditText editTextTemperature, editTextWindSpeed, editTextWindDirection;
    private EditText editTextTime, editTextWeatherCode;
    private Button buttonGetWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String latitudeStr = editTextLatitude.getText().toString().trim();
                String longitudeStr = editTextLongitude.getText().toString().trim();

                if (latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Введите широту и долготу", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);
                    if (latitude < -90 || latitude > 90) {
                        Toast.makeText(MainActivity.this,
                                "Широта должна быть от -90 до 90", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (longitude < -180 || longitude > 180) {
                        Toast.makeText(MainActivity.this,
                                "Долгота должна быть от -180 до 180", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (checkInternetConnection()) {
                        new GetWeatherTask().execute(latitudeStr, longitudeStr);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this,
                            "Введите корректные числовые значения", Toast.LENGTH_SHORT).show();
                }
            }
        });
        editTextLatitude.setText("52.52");
        editTextLongitude.setText("13.41");
    }

    private void initViews() {
        textViewStatus = findViewById(R.id.textViewStatus);

        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);

        editTextLatitudeDisplay = findViewById(R.id.editTextLatitudeDisplay);
        editTextLongitudeDisplay = findViewById(R.id.editTextLongitudeDisplay);

        editTextTemperature = findViewById(R.id.editTextTemperature);
        editTextWindSpeed = findViewById(R.id.editTextWindSpeed);
        editTextWindDirection = findViewById(R.id.editTextWindDirection);
        editTextTime = findViewById(R.id.editTextTime);
        editTextWeatherCode = findViewById(R.id.editTextWeatherCode);

        buttonGetWeather = findViewById(R.id.buttonGetWeather);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = null;
        if (connectivityManager != null) {
            networkinfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkinfo != null && networkinfo.isConnected()) {
            return true;
        } else {
            Toast.makeText(MainActivity.this, "Нет интернета", Toast.LENGTH_SHORT).show();
            textViewStatus.setText("Нет интернет-соединения");
            return false;
        }
    }

    private class GetWeatherTask extends AsyncTask<String, Void, String> {
        private String latitude;
        private String longitude;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewStatus.setText("Загружаем данные о погоде...");
            clearWeatherFields();
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length < 2) return "error";

            latitude = params[0];
            longitude = params[1];

            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                    "&longitude=" + longitude + "&current_weather=true";

            try {
                return downloadDataFromUrl(weatherUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("error")) {
                textViewStatus.setText("Ошибка при загрузке погоды");
                return;
            }

            try {
                JSONObject weatherJson = new JSONObject(result);

                editTextLatitudeDisplay.setText(latitude);
                editTextLongitudeDisplay.setText(longitude);

                if (weatherJson.has("current_weather")) {
                    JSONObject currentWeather = weatherJson.getJSONObject("current_weather");

                    double temperature = currentWeather.optDouble("temperature", 0);
                    double windSpeed = currentWeather.optDouble("windspeed", 0);
                    double windDirection = currentWeather.optDouble("winddirection", 0);
                    int weatherCode = currentWeather.optInt("weathercode", 0);
                    String time = currentWeather.optString("time", "");

                    editTextTemperature.setText(String.format(Locale.getDefault(), "%.1f °C", temperature));
                    editTextWindSpeed.setText(String.format(Locale.getDefault(), "%.1f км/ч", windSpeed));
                    editTextWindDirection.setText(String.format(Locale.getDefault(), "%.1f°", windDirection));

                    if (!time.isEmpty()) {
                        try {
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                            Date date = isoFormat.parse(time);
                            if (date != null) {
                                editTextTime.setText(displayFormat.format(date));
                            } else {
                                editTextTime.setText(time);
                            }
                        } catch (Exception e) {
                            editTextTime.setText(time);
                        }
                    }

                    String weatherDescription = getWeatherDescription(weatherCode);
                    editTextWeatherCode.setText(weatherCode + " - " + weatherDescription);

                    textViewStatus.setText("Погода успешно загружена!");
                    String usedUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                            "&longitude=" + longitude + "&current_weather=true";
                    Toast.makeText(MainActivity.this,
                            "Использован запрос: " + usedUrl, Toast.LENGTH_LONG).show();

                } else {
                    textViewStatus.setText("Данные о текущей погоде не найдены в ответе");
                    editTextTemperature.setText("Нет данных");
                    editTextWindSpeed.setText("Нет данных");
                    editTextWindDirection.setText("Нет данных");
                    editTextTime.setText("Нет данных");
                    editTextWeatherCode.setText("Нет данных");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                textViewStatus.setText("Ошибка при разборе данных о погоде");
                Toast.makeText(MainActivity.this,
                        "Ошибка JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private String getWeatherDescription(int weatherCode) {
            // Описания погоды по кодам WMO Weather interpretation codes
            switch (weatherCode) {
                case 0: return "Ясно";
                case 1: return "В основном ясно";
                case 2: return "Переменная облачность";
                case 3: return "Пасмурно";
                case 45: return "Туман";
                case 48: return "Туман с инеем";
                case 51: return "Легкая морось";
                case 53: return "Умеренная морось";
                case 55: return "Сильная морось";
                case 56: return "Легкая ледяная морось";
                case 57: return "Сильная ледяная морось";
                case 61: return "Небольшой дождь";
                case 63: return "Умеренный дождь";
                case 65: return "Сильный дождь";
                case 66: return "Легкий ледяной дождь";
                case 67: return "Сильный ледяной дождь";
                case 71: return "Небольшой снег";
                case 73: return "Умеренный снег";
                case 75: return "Сильный снег";
                case 77: return "Снежные зерна";
                case 80: return "Небольшой ливень";
                case 81: return "Умеренный ливень";
                case 82: return "Сильный ливень";
                case 85: return "Небольшой снегопад";
                case 86: return "Сильный снегопад";
                case 95: return "Гроза";
                case 96: return "Гроза с небольшим градом";
                case 99: return "Гроза с сильным градом";
                default: return "Неизвестный код погоды";
            }
        }
    }

    // Метод для загрузки данных через HttpURLConnection
    private String downloadDataFromUrl(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();

            // Настраиваем соединение
            connection.setReadTimeout(100000); // 100 секунд
            connection.setConnectTimeout(100000); // 100 секунд
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            // Добавляем заголовки для корректной работы с API
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "HttpURLConnectionApp/1.0");

            // Получаем код ответа
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Читаем данные из потока
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, length);
                }

                bos.close();
                data = bos.toString("UTF-8");

            } else {
                // Если ошибка, читаем сообщение об ошибке
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    ByteArrayOutputStream errorBos = new ByteArrayOutputStream();
                    byte[] errorBuffer = new byte[1024];
                    int errorLength;

                    while ((errorLength = errorStream.read(errorBuffer)) != -1) {
                        errorBos.write(errorBuffer, 0, errorLength);
                    }

                    errorBos.close();
                    data = "HTTP Error " + responseCode + ": " + errorBos.toString("UTF-8");
                } else {
                    data = "HTTP Error " + responseCode + ": " + connection.getResponseMessage();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;

        } finally {
            // Закрываем соединение и потоки
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return data;
    }

    private void clearWeatherFields() {
        editTextLatitudeDisplay.setText("");
        editTextLongitudeDisplay.setText("");
        editTextTemperature.setText("");
        editTextWindSpeed.setText("");
        editTextWindDirection.setText("");
        editTextTime.setText("");
        editTextWeatherCode.setText("");
    }
}