package com.mirea.tyurkinaia.data_thread;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.mirea.tyurkinaia.data_thread.databinding.ActivityMainBinding;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Runnable runn1 = new Runnable() {
            public void run() {
                counter++;
                binding.tvInfo.append("runn1 выполнен (счётчик: " + counter + ")\n");
            }
        };

        final Runnable runn2 = new Runnable() {
            public void run() {
                counter++;
                binding.tvInfo.append("runn2 выполнен (счётчик: " + counter + ")\n");
            }
        };

        final Runnable runn3 = new Runnable() {
            public void run() {
                counter++;
                binding.tvInfo.append("runn3 выполнен (счётчик: " + counter + ")\n");
            }
        };

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    binding.tvInfo.post(new Runnable() {
                        public void run() {
                            binding.tvInfo.append("Поток начал работу\n");
                        }
                    });
                    TimeUnit.SECONDS.sleep(2);
                    runOnUiThread(runn1);
                    TimeUnit.SECONDS.sleep(1);
                    binding.tvInfo.postDelayed(runn3, 2000);
                    binding.tvInfo.post(runn2);
                    binding.tvInfo.post(new Runnable() {
                        public void run() {
                            binding.tvInfo.append("Все Runnable отправлены в очередь\n");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        // Добавим кнопку для повторного теста
        binding.btnTest.setOnClickListener(v -> {
            counter = 0;
            binding.tvInfo.setText("");
            t.start();
        });

        // Добавим кнопку для очистки
        binding.btnClear.setOnClickListener(v -> {
            counter = 0;
            binding.tvInfo.setText("");
        });
    }
}