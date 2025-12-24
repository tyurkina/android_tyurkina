package com.mirea.tyurkinaia.lesson6;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextGroup;
    private EditText editTextNumber;
    private EditText editTextMovie;
    private Button buttonSave;

    private SharedPreferences sharedPref;
    private static final String PREF_NAME = "student_preferences";
    private static final String KEY_GROUP = "group_number";
    private static final String KEY_NUMBER = "list_number";
    private static final String KEY_MOVIE = "favorite_movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextGroup = findViewById(R.id.editTextGroup);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextMovie = findViewById(R.id.editTextMovie);
        buttonSave = findViewById(R.id.buttonSave);
        sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadSavedData();
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void saveData() {
        String group = editTextGroup.getText().toString().trim();
        String numberStr = editTextNumber.getText().toString().trim();
        String movie = editTextMovie.getText().toString().trim();

        // Валидация данных
        if (group.isEmpty() || numberStr.isEmpty() || movie.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int number = Integer.parseInt(numberStr);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_GROUP, group);
            editor.putInt(KEY_NUMBER, number);
            editor.putString(KEY_MOVIE, movie);
            editor.apply(); // Асинхронное сохранение

            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Номер должен быть числом", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedData() {
        String savedGroup = sharedPref.getString(KEY_GROUP, "");
        int savedNumber = sharedPref.getInt(KEY_NUMBER, 0);
        String savedMovie = sharedPref.getString(KEY_MOVIE, "");

        editTextGroup.setText(savedGroup);
        editTextNumber.setText(savedNumber > 0 ? String.valueOf(savedNumber) : "");
        editTextMovie.setText(savedMovie);
    }
}