package com.mirea.tyurkinaia.multiactivity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textView = findViewById(R.id.textViewResult);
        String receivedText = getIntent().getStringExtra("input_text");
        if (receivedText != null && !receivedText.isEmpty()) {
            textView.setText(receivedText);
        } else {
            textView.setText("Текст не был передан");
        }
    }
}