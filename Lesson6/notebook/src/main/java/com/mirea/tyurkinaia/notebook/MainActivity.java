package com.mirea.tyurkinaia.notebook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private EditText filenameEditText;
    private EditText quoteEditText;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filenameEditText = findViewById(R.id.filenameEditText);
        quoteEditText = findViewById(R.id.quoteEditText);
        statusTextView = findViewById(R.id.statusTextView);
        Button saveButton = findViewById(R.id.saveButton);
        Button loadButton = findViewById(R.id.loadButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFile();
            }
        });
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFromFile();
            }
        });
    }
    private void saveToFile() {
        String filename = filenameEditText.getText().toString().trim();
        String quote = quoteEditText.getText().toString().trim();

        if (filename.isEmpty() || quote.isEmpty()) {
            showStatus("Заполните все поля");
            return;
        }
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        try {
            File documentsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            if (!documentsDir.exists()) {
                documentsDir.mkdirs();
            }
            File file = new File(documentsDir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            osw.write(quote);
            osw.close();
            fos.close();

            showStatus("Файл сохранен: " + file.getAbsolutePath());
            Toast.makeText(this, "Файл сохранен успешно", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            showStatus("Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void loadFromFile() {
        String filename = filenameEditText.getText().toString().trim();

        if (filename.isEmpty()) {
            showStatus("Введите название файла");
            return;
        }
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        try {
            File documentsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            File file = new File(documentsDir, filename);

            if (!file.exists()) {
                showStatus("Файл не найден: " + filename);
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();
            isr.close();
            fis.close();

            quoteEditText.setText(content.toString().trim());
            showStatus("Файл загружен: " + file.getAbsolutePath());
            Toast.makeText(this, "Файл загружен успешно", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            showStatus("Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showStatus(String message) {
        statusTextView.setText(message);
        statusTextView.setVisibility(View.VISIBLE);
    }

}