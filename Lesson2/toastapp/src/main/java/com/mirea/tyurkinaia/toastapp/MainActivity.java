package com.mirea.tyurkinaia.toastapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInput;
    private Button buttonCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextInput = findViewById(R.id.editTextInput);
        buttonCount = findViewById(R.id.buttonCount);
        buttonCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countAndShowCharacters();
            }
        });
    }

    private void countAndShowCharacters() {
        String inputText = editTextInput.getText().toString();
        int characterCount = inputText.length();
        String studentNumber = "22";
        String groupNumber = "БИСО-01-21";
        String message = String.format(
                "СТУДЕНТ № %s ГРУППА %s Количество символов - %d",
                studentNumber, groupNumber, characterCount
        );

        Toast toast = Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_LONG
        );
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}