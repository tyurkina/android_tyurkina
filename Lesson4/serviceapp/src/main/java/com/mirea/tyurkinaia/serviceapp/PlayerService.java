package com.mirea.tyurkinaia.serviceapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String TAG = "PlayerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate сервиса");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand вызван");
        playMusic();
        return START_STICKY;
    }
    private void playMusic() {
        try {
            // Создаем MediaPlayer
            mediaPlayer = MediaPlayer.create(this, R.raw.my_music);

            if (mediaPlayer == null) {
                Log.e(TAG, "Не удалось создать MediaPlayer");
                Toast.makeText(this, "Ошибка: аудиофайл не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            Log.d(TAG, "Музыка играет");
            Toast.makeText(this, "Музыка играет: Штиль- Кипелов", Toast.LENGTH_SHORT).show();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "Музыка завершена");
                    stopSelf();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Ошибка воспроизведения: " + e.getMessage());
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy сервиса");

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show();
    }
}