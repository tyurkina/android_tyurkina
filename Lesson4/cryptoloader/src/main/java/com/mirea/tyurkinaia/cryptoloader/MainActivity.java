package com.mirea.tyurkinaia.cryptoloader;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mirea.tyurkinaia.cryptoloader.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private ActivityMainBinding binding;
    private final int LOADER_ID = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = binding.editText.getText().toString().trim();

                if (inputText.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Введите текст для шифрования",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                SecretKey secretKey = AESCryptHelper.generateKey();
                byte[] encryptedText = AESCryptHelper.encryptMsg(inputText, secretKey);
                Bundle bundle = new Bundle();
                bundle.putByteArray(MyLoader.ARG_WORD, encryptedText);
                bundle.putByteArray(MyLoader.ARG_KEY, secretKey.getEncoded());
                LoaderManager.getInstance(MainActivity.this)
                        .restartLoader(LOADER_ID, bundle, MainActivity.this);
                Snackbar.make(binding.getRoot(),
                        "Загрузка началась... Ожидайте 5 секунд",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOADER_ID) {
            Toast.makeText(this, "Создание загрузчика: " + id,
                    Toast.LENGTH_SHORT).show();
            return new MyLoader(this, args);
        }
        throw new IllegalArgumentException("Неверный ID загрузчика");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String decryptedText) {
        if (loader.getId() == LOADER_ID) {
            String message = "Дешифрованный текст: " + decryptedText;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Очистка ресурсов, если необходимо
    }
}