package com.mirea.tyurkinaia.sharer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        TextView textView = findViewById(R.id.text_view);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    textView.setText("Полученный текст: " + sharedText);
                    Toast.makeText(this, "Текст получен: " + sharedText, Toast.LENGTH_LONG).show();
                }
            } else if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    textView.setText("Получено изображение: " + imageUri.toString());
                    Toast.makeText(this, "Изображение получено", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            textView.setText("Данные не получены");
        }
    }
}