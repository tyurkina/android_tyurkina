package com.mirea.tyurkinaia.lesson4;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private com.mirea.tyurkinaia.lesson4.databinding.ActivityMainBinding binding;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.mirea.tyurkinaia.lesson4.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        binding.trackTitle.setText("Штиль");
        binding.artistName.setText("Кипелов");
        binding.songProgress.setMax(100); // Максимальное значение 100%
        binding.songProgress.setProgress(0); // Начальное значение 0%
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
                    // Обновляем отображаемое время
                    updateCurrentTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void togglePlayPause() {
        isPlaying = !isPlaying;
        updatePlayPauseButton();
        if (isPlaying) {
            startProgressSimulation();
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
    }

    private void nextTrack() {
        binding.songProgress.setProgress(0);
        updateCurrentTime(0);
    }

    private void updateCurrentTime(int progress) {
        int totalSeconds = 210; // 3 минуты 30 секунд
        int currentSeconds = (progress * totalSeconds) / 100;

        int minutes = currentSeconds / 60;
        int seconds = currentSeconds % 60;

        binding.currentTime.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void startProgressSimulation() {
        if (isPlaying) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPlaying && binding.songProgress.getProgress() < 100) {
                        int progress = binding.songProgress.getProgress() + 1;
                        binding.songProgress.setProgress(progress);
                        updateCurrentTime(progress);
                        if (isPlaying) {
                            startProgressSimulation();
                        }
                    } else if (binding.songProgress.getProgress() >= 100) {
                        isPlaying = false;
                        updatePlayPauseButton();
                        showToast("Трек завершен");
                    }
                }
            }, 1000);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}