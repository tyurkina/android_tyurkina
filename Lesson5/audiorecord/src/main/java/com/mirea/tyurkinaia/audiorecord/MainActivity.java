package com.mirea.tyurkinaia.audiorecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 200;
    private final String TAG = MainActivity.class.getSimpleName();

    private boolean isWork = false;
    private String recordFilePath = null;
    private Button recordButton = null;
    private Button playButton = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    boolean isStartRecording = true;
    boolean isStartPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);
        playButton.setEnabled(false);
        recordFilePath = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "/audiorecordtest.3gp").getAbsolutePath();
        checkPermissions();
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartRecording) {
                    // Проверяем разрешения перед записью
                    if (hasAllPermissions()) {
                        recordButton.setText("Остановить запись");
                        playButton.setEnabled(false);
                        startRecording();
                        isWork = true;
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Запрашиваем разрешения...",
                                Toast.LENGTH_SHORT).show();
                        requestNeededPermissions();
                    }
                } else {
                    recordButton.setText("Начать запись. Тюркина И.А, БИСО-01-21");
                    playButton.setEnabled(true);
                    stopRecording();
                }
                isStartRecording = !isStartRecording;
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartPlaying) {
                    File file = new File(recordFilePath);
                    if (!file.exists()) {
                        Toast.makeText(MainActivity.this,
                                "Сначала запишите аудио",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    playButton.setText("Остановить воспроизведение");
                    recordButton.setEnabled(false);
                    startPlaying();
                } else {
                    playButton.setText("Воспроизвести");
                    recordButton.setEnabled(true);
                    stopPlaying();
                }
                isStartPlaying = !isStartPlaying;
            }
        });
    }
    private boolean hasAllPermissions() {
        int audioRecordPermissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int storagePermissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean hasAudioPermission = audioRecordPermissionStatus == PackageManager.PERMISSION_GRANTED;
        boolean hasWritePermission = storagePermissionStatus == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            int readStoragePermissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            boolean hasReadPermission = readStoragePermissionStatus == PackageManager.PERMISSION_GRANTED;
            return hasAudioPermission && hasWritePermission && hasReadPermission;
        }

        return hasAudioPermission && hasWritePermission;
    }
    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_PERMISSION);
        }
    }
    private void checkPermissions() {
        if (hasAllPermissions()) {
            isWork = true;
            Toast.makeText(this,
                    "Все разрешения предоставлены",
                    Toast.LENGTH_SHORT).show();
        } else {
            isWork = false;
            Toast.makeText(this,
                    "Нажмите кнопку записи для запроса разрешений",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                isWork = true;
                Toast.makeText(this,
                        "Разрешения предоставлены. Можно начинать запись.",
                        Toast.LENGTH_SHORT).show();
                if (isStartRecording) {
                    recordButton.performClick();
                }
            } else {
                isWork = false;
                Toast.makeText(this,
                        "Разрешения не предоставлены. Невозможно записывать аудио.",
                        Toast.LENGTH_LONG).show();
                recordButton.setText("Начать запись. Тюркина И.А., БИСО-01-21");
                isStartRecording = true;
            }
        }
    }


    private void startRecording() {
        if (!isWork) {
            Toast.makeText(this,
                    "Нет разрешений для записи",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(recordFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            recorder.prepare();
            recorder.start();

            Toast.makeText(this, "Запись начата", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed: " + e.getMessage());
            Toast.makeText(this, "Ошибка подготовки записи", Toast.LENGTH_SHORT).show();
            releaseRecorder();
        } catch (Exception e) {
            Log.e(TAG, "startRecording() failed: " + e.getMessage());
            Toast.makeText(this, "Ошибка начала записи", Toast.LENGTH_SHORT).show();
            releaseRecorder();
        }
    }

    private void stopRecording() {
        releaseRecorder();
        Toast.makeText(this, "Запись остановлена", Toast.LENGTH_SHORT).show();

        // Проверяем, создался ли файл
        File file = new File(recordFilePath);
        if (file.exists()) {
            playButton.setEnabled(true);
            Toast.makeText(this,
                    "Файл сохранен: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                Log.e(TAG, "stop() failed: " + e.getMessage());
            }
            recorder.release();
            recorder = null;
        }
    }

    private void startPlaying() {
        try {
            player = new MediaPlayer();
            player.setDataSource(recordFilePath);
            player.prepare();
            player.start();

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Автоматически останавливаем воспроизведение
                    playButton.setText("Воспроизвести");
                    recordButton.setEnabled(true);
                    isStartPlaying = true;
                    stopPlaying();
                }
            });

            Toast.makeText(this, "Воспроизведение начато", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed: " + e.getMessage());
            Toast.makeText(this, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
            releasePlayer();
        }
    }

    private void stopPlaying() {
        releasePlayer();
        Toast.makeText(this, "Воспроизведение остановлено", Toast.LENGTH_SHORT).show();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseRecorder();
        releasePlayer();
    }
}