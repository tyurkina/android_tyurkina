package com.mirea.tyurkinaia.securesharedpreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private TextView poetNameTextView;
    private ImageView poetImageView;
    private SharedPreferences secureSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        poetNameTextView = findViewById(R.id.poetNameTextView);
        poetImageView = findViewById(R.id.poetImageView);
        poetNameTextView.setText("Марина Цветаева");
        poetImageView.setImageResource(R.drawable.tsvetayeva);
        initSecureSharedPreferences();
        savePoetData();
        readPoetData();
    }
    private void initSecureSharedPreferences() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            secureSharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void savePoetData() {
        try {
            secureSharedPreferences.edit().putString("poet_name", "Марина Цветаева").apply();
            // Преобразование изображения в Base64 и сохранение
            Bitmap bitmap = ((BitmapDrawable) poetImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] byteArray = byteStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            secureSharedPreferences.edit().putString("poet_image_base64", encodedImage).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readPoetData() {
        String savedName = secureSharedPreferences.getString("poet_name", "Данные не найдены");
        String savedImage = secureSharedPreferences.getString("poet_image_base64", null);
    }
}