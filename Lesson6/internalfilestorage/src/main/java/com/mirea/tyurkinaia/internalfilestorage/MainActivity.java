package com.mirea.tyurkinaia.internalfilestorage;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etDateName, etDescription;
    private TextView tvResult;
    private Button btnSave, btnRead, btnListFiles;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FILE_NAME = "memorable_dates.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etDateName = findViewById(R.id.etDateName);
        etDescription = findViewById(R.id.etDescription);
        tvResult = findViewById(R.id.tvResult);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFile();
            }
        });
    }

    private void saveToFile() {
        String dateName = etDateName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (dateName.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        String content = String.format(Locale.getDefault(),
                "=== Памятная дата ===\n" +
                        "Дата: %s\n" +
                        "Описание: %s\n" +
                        "Записано: %s\n" +
                        "=====================\n\n",
                dateName, description, currentTime);

        try (FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_APPEND)) {
            fos.write(content.getBytes());
            fos.close();

            tvResult.setText("Данные успешно сохранены в файл!\n\n" + content);
            Toast.makeText(this, "Файл сохранен: " + getFileStreamPath(FILE_NAME), Toast.LENGTH_LONG).show();
            etDateName.setText("");
            etDescription.setText("");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при сохранении файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void readFromFile() {
        try (FileInputStream fis = openFileInput(FILE_NAME)) {
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            String text = new String(bytes);

            if (text.isEmpty()) {
                tvResult.setText("Файл пуст");
            } else {
                tvResult.setText("Содержимое файла:\n\n" + text);
            }

        } catch (IOException e) {
            e.printStackTrace();
            tvResult.setText("Файл не найден или произошла ошибка чтения");
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void listAllFiles() {
        String[] files = fileList();

        if (files.length == 0) {
            tvResult.setText("В директории files нет созданных файлов");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Список файлов в директории приложения:\n\n");

        for (String file : files) {
            sb.append("• ").append(file).append("\n");
        }

        sb.append("\nВсего файлов: ").append(files.length);
        sb.append("\n\nПуть к директории: ").append(getFilesDir().getAbsolutePath());

        tvResult.setText(sb.toString());
    }
}