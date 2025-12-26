package com.mirea.tyurkinaia.lesson4;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private com.mirea.tyurkinaia.lesson4.databinding.ActivityMainBinding binding;
    private boolean isPlaying = false;
    private Handler progressHandler;
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.mirea.tyurkinaia.lesson4.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Восстанавливаем состояние при повороте
        if (savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean("isPlaying", false);
        }

        progressHandler = new Handler();
        setupUI();
        setupListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying", isPlaying);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Если трек играл до поворота, продолжаем симуляцию
        if (isPlaying) {
            startProgressSimulation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Останавливаем симуляцию при уходе с экрана
        stopProgressSimulation();
    }

    private void setupUI() {
        binding.trackTitle.setText("Штиль");
        binding.artistName.setText("Кипелов");
        binding.songProgress.setMax(100);
        binding.songProgress.setProgress(0);
        binding.currentTime.setText("0:00");
        binding.totalTime.setText("3:30");
        updatePlayPauseButton();
    }

    private void setupListeners() {
        binding.btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
        binding.btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousTrack();
            }
        });
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTrack();
            }
        });
        binding.songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateCurrentTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Останавливаем симуляцию при ручном перемещении прогресса
                if (isPlaying) {
                    stopProgressSimulation();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Возобновляем симуляцию после отпускания
                if (isPlaying) {
                    startProgressSimulation();
                }
            }
        });
    }

    private void togglePlayPause() {
        isPlaying = !isPlaying;
        updatePlayPauseButton();
        if (isPlaying) {
            startProgressSimulation();
        } else {
            stopProgressSimulation();
        }
    }

    private void updatePlayPauseButton() {
        if (isPlaying) {
            binding.btnPlayPause.setText("⏸");
        } else {
            binding.btnPlayPause.setText("▶");
        }
    }

    private void previousTrack() {
        binding.songProgress.setProgress(0);
        updateCurrentTime(0);
        if (isPlaying) {
            startProgressSimulation();
        }
    }

    private void nextTrack() {
        binding.songProgress.setProgress(0);
        updateCurrentTime(0);
        if (isPlaying) {
            startProgressSimulation();
        }
    }

    private void updateCurrentTime(int progress) {
        int totalSeconds = 210;
        int currentSeconds = (progress * totalSeconds) / 100;

        int minutes = currentSeconds / 60;
        int seconds = currentSeconds % 60;

        binding.currentTime.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void startProgressSimulation() {
        stopProgressSimulation(); // Останавливаем предыдущую симуляцию

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && binding != null && binding.songProgress.getProgress() < 100) {
                    int progress = binding.songProgress.getProgress() + 1;
                    binding.songProgress.setProgress(progress);
                    updateCurrentTime(progress);

                    if (isPlaying) {
                        progressHandler.postDelayed(this, 1000);
                    }
                } else if (binding != null && binding.songProgress.getProgress() >= 100) {
                    isPlaying = false;
                    updatePlayPauseButton();
                    showToast("Трек завершен");
                }
            }
        };

        progressHandler.post(progressRunnable);
    }

    private void stopProgressSimulation() {
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgressSimulation();
        if (progressHandler != null) {
            progressHandler.removeCallbacksAndMessages(null);
        }
        binding = null;
    }
}