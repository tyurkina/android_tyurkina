package com.mirea.tyurkinaia.sharer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> pickActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri selectedFileUri = data.getData();
                                Log.d("MainActivity", "Selected URI: " + selectedFileUri.toString());
                                Toast.makeText(MainActivity.this,
                                        "Выбран файл: " + selectedFileUri.getLastPathSegment(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("MainActivity", "Операция отменена");
                        }
                    }
                });

        Button sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });

        Button receiveButton = findViewById(R.id.receive_button);
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveData();
            }
        });
    }

    private void sendData() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Mirea");
        sendIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(sendIntent, "Выберите приложение для отправки");

        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "Нет приложений для отправки", Toast.LENGTH_SHORT).show();
        }
    }
    private void receiveData() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("*/*"); // Любой тип файлов
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (pickIntent.resolveActivity(getPackageManager()) != null) {
            pickActivityResultLauncher.launch(pickIntent);
        } else {
            Toast.makeText(this, "Нет приложений для выбора файлов", Toast.LENGTH_SHORT).show();
        }
    }
}