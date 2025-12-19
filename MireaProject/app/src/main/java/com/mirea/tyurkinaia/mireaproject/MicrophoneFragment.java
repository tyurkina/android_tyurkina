package com.mirea.tyurkinaia.mireaproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mirea.tyurkinaia.mireaproject.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MicrophoneFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private static final String[] AUDIO_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Button recordButton;
    private Button stopButton;
    private Button playLastRecordingButton;
    private TextView recordingStatusTextView;
    private TextView timerTextView;
    private ProgressBar visualizer;
    private RecyclerView recordingsRecyclerView;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentRecordingPath;
    private CountDownTimer recordingTimer;
    private long recordingTime = 0;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    private Handler visualizerHandler = new Handler();
    private Runnable visualizerRunnable;

    private RecordingsAdapter recordingsAdapter;
    private List<RecordingItem> recordingItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_microphone, container, false);

        // Инициализация UI элементов
        recordButton = root.findViewById(R.id.recordButton);
        stopButton = root.findViewById(R.id.stopButton);
        playLastRecordingButton = root.findViewById(R.id.playLastRecordingButton);
        recordingStatusTextView = root.findViewById(R.id.recordingStatusTextView);
        timerTextView = root.findViewById(R.id.timerTextView);
        visualizer = root.findViewById(R.id.visualizer);
        recordingsRecyclerView = root.findViewById(R.id.recordingsRecyclerView);

        // Настройка RecyclerView
        recordingsAdapter = new RecordingsAdapter(recordingItems, new RecordingsAdapter.OnItemClickListener() {
            @Override
            public void onPlayClick(RecordingItem item) {
                playRecording(item.getFilePath());
            }

            @Override
            public void onDeleteClick(RecordingItem item) {
                deleteRecording(item);
            }
        });

        recordingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recordingsRecyclerView.setAdapter(recordingsAdapter);

        // Загрузка существующих записей
        loadExistingRecordings();

        // Настройка обработчиков нажатий
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        playLastRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recordingItems.isEmpty()) {
                    playRecording(recordingItems.get(0).getFilePath());
                } else {
                    Toast.makeText(getContext(), "Нет сохраненных записей", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                AUDIO_PERMISSIONS,
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void startRecording() {
        if (!checkAudioPermission()) {
            requestAudioPermission();
            return;
        }

        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    STORAGE_PERMISSIONS,
                    REQUEST_STORAGE_PERMISSION);
            return;
        }

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);

            // Создание файла для записи
            File recordingsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "MireaAudioNotes");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            currentRecordingPath = recordingsDir.getAbsolutePath() + "/recording_" + timeStamp + ".m4a";

            mediaRecorder.setOutputFile(currentRecordingPath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            updateUIForRecording(true);
            startTimer();
            startVisualizer();

            Toast.makeText(getContext(), "Запись началась", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Ошибка начала записи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            resetRecording();
        } catch (IllegalStateException e) {
            Toast.makeText(getContext(), "Ошибка состояния записи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            resetRecording();
        }
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;
                updateUIForRecording(false);
                stopTimer();
                stopVisualizer();

                // Сохранение информации о записи
                saveRecordingInfo();

                Toast.makeText(getContext(), "Запись сохранена", Toast.LENGTH_SHORT).show();

            } catch (RuntimeException e) {
                Toast.makeText(getContext(), "Ошибка остановки записи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                resetRecording();
            }
        }
    }

    private void resetRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
        }
        isRecording = false;
        updateUIForRecording(false);
        stopTimer();
        stopVisualizer();
    }

    private void playRecording(String filePath) {
        if (isPlaying) {
            stopPlaying();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            isPlaying = true;
            updateUIForPlaying(true);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

        } catch (IOException e) {
            Toast.makeText(getContext(), "Ошибка воспроизведения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
        isPlaying = false;
        updateUIForPlaying(false);
    }

    private void startTimer() {
        recordingTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                recordingTime++;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                // Не вызывается
            }
        }.start();
    }

    private void stopTimer() {
        if (recordingTimer != null) {
            recordingTimer.cancel();
        }
        recordingTime = 0;
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        long minutes = recordingTime / 60;
        long seconds = recordingTime % 60;
        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(time);
    }

    private void startVisualizer() {
        visualizer.setVisibility(View.VISIBLE);
        visualizerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    // Имитация визуализации звука
                    int amplitude = (int) (Math.random() * 100);
                    visualizer.setProgress(amplitude);
                    visualizerHandler.postDelayed(this, 100);
                }
            }
        };
        visualizerHandler.post(visualizerRunnable);
    }

    private void stopVisualizer() {
        if (visualizerRunnable != null) {
            visualizerHandler.removeCallbacks(visualizerRunnable);
        }
        visualizer.setVisibility(View.GONE);
        visualizer.setProgress(0);
    }

    private void updateUIForRecording(boolean recording) {
        if (recording) {
            recordButton.setText("Остановить запись");
            recordButton.setBackgroundColor(Color.RED);
            stopButton.setEnabled(true);
            recordingStatusTextView.setText("Идет запись...");
        } else {
            recordButton.setText("Начать запись");
            recordButton.setBackgroundColor(Color.parseColor("#2196F3")); // Синий цвет
            stopButton.setEnabled(false);
            recordingStatusTextView.setText("Готов к записи");
        }
    }

    private void updateUIForPlaying(boolean playing) {
        if (playing) {
            playLastRecordingButton.setText("Остановить воспроизведение");
            playLastRecordingButton.setBackgroundColor(Color.RED);
        } else {
            playLastRecordingButton.setText("Воспроизвести последнюю запись");
            playLastRecordingButton.setBackgroundColor(Color.parseColor("#4CAF50")); // Зеленый цвет
        }
    }

    private void saveRecordingInfo() {
        File recordingFile = new File(currentRecordingPath);
        if (recordingFile.exists()) {
            RecordingItem item = new RecordingItem(
                    recordingFile.getName(),
                    currentRecordingPath,
                    recordingTime,
                    new Date().getTime()
            );
            recordingItems.add(0, item); // Добавляем в начало списка
            recordingsAdapter.notifyItemInserted(0);
            recordingsRecyclerView.scrollToPosition(0);
        }
    }

    private void loadExistingRecordings() {
        try {
            File recordingsDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), "MireaAudioNotes");

            if (recordingsDir.exists() && recordingsDir.isDirectory()) {
                File[] files = recordingsDir.listFiles((dir, name) ->
                        name.endsWith(".m4a") || name.endsWith(".mp4") || name.endsWith(".mp3"));

                if (files != null) {
                    for (File file : files) {
                        RecordingItem item = new RecordingItem(
                                file.getName(),
                                file.getAbsolutePath(),
                                0, // Длину можно получить из MediaPlayer
                                file.lastModified()
                        );
                        recordingItems.add(item);
                    }
                    recordingsAdapter.notifyDataSetChanged();
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Нет разрешения на доступ к файлам", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecording(RecordingItem item) {
        File file = new File(item.getFilePath());
        if (file.delete()) {
            int position = recordingItems.indexOf(item);
            recordingItems.remove(position);
            recordingsAdapter.notifyItemRemoved(position);
            Toast.makeText(getContext(), "Запись удалена", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Ошибка удаления записи", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Теперь проверяем storage permission
                if (checkStoragePermission()) {
                    startRecording();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(),
                            STORAGE_PERMISSIONS,
                            REQUEST_STORAGE_PERMISSION);
                }
            } else {
                Toast.makeText(getContext(), "Для записи аудио необходимо разрешение", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkAudioPermission()) {
                    startRecording();
                }
            } else {
                Toast.makeText(getContext(), "Для сохранения записи необходимо разрешение на доступ к хранилищу", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
        }
        if (isPlaying) {
            stopPlaying();
        }
        if (visualizerHandler != null && visualizerRunnable != null) {
            visualizerHandler.removeCallbacks(visualizerRunnable);
        }
    }

    // Вложенный класс для элемента записи
    public static class RecordingItem {
        private String fileName;
        private String filePath;
        private long duration;
        private long timestamp;

        public RecordingItem(String fileName, String filePath, long duration, long timestamp) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.duration = duration;
            this.timestamp = timestamp;
        }

        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public long getDuration() { return duration; }
        public long getTimestamp() { return timestamp; }
    }
}