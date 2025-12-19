package com.mirea.tyurkinaia.thread;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.mirea.tyurkinaia.thread.databinding.ActivityMainBinding;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Получаем информацию о главном потоке
        showThreadInfo();

        // Кнопка для расчета среднего количества пар
        binding.calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAveragePairs();
            }
        });

        // Кнопка для теста медленной операции (в отдельном потоке)
        binding.slowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSlowOperationInThread();
            }
        });
    }

    private void showThreadInfo() {
        Thread mainThread = Thread.currentThread();
        String info = "Имя текущего потока: " + mainThread.getName();
        mainThread.setName("МОЙ НОМЕР ГРУППЫ: БИСО-01-21, НОМЕР ПО СПИСКУ: 22, МОЙ ЛЮБИМЫЙ ФИЛЬМ: Властелин колец");
        info += "\n\nНовое имя потока: " + mainThread.getName();
        Log.d(MainActivity.class.getSimpleName(), "Stack: " + Arrays.toString(mainThread.getStackTrace()));

        binding.threadInfoTextView.setText(info);
    }

    private void calculateAveragePairs() {
        // Получаем данные из EditText
        String totalPairsStr = binding.totalPairsEditText.getText().toString();
        String daysStr = binding.daysEditText.getText().toString();

        if (totalPairsStr.isEmpty() || daysStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final int totalPairs = Integer.parseInt(totalPairsStr);
            final int days = Integer.parseInt(daysStr);

            if (days <= 0) {
                Toast.makeText(this, "Количество дней должно быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    // Имитируем вычисления (немного задержки)
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Вычисляем среднее количество пар в день
                    final double averagePairs = (double) totalPairs / days;
                    // Возвращаем результат в главный поток для обновления UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String result = String.format("Среднее количество пар в день: %.2f\n" +
                                            "Всего пар: %d\n" +
                                            "Учебных дней: %d\n" +
                                            "Поток расчета: %s (приоритет: %d)",
                                    averagePairs, totalPairs, days,
                                    Thread.currentThread().getName(),
                                    Thread.currentThread().getPriority());

                            binding.resultTextView.setText(result);
                            // Показываем информацию о фоновом потоке
                            Log.d("ThreadProject", String.format("Расчет выполнен в фоновом потоке. Приоритет: %d",
                                    Thread.currentThread().getPriority()));
                        }
                    });
                }
            }).start();
            Toast.makeText(this, "Расчет начат...", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректные числа", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSlowOperationInThread() {
        new Thread(new Runnable() {
            public void run() {
                int numberThread = counter++;
                Log.d("ThreadProject", String.format(
                        "Запущен поток № %d студентом группы № %s номер по списку № %d",
                        numberThread, "БИСО-01-21", 0));

                long endTime = System.currentTimeMillis() + 20 * 1000;
                while (System.currentTimeMillis() < endTime) {
                    synchronized (this) {
                        try {
                            wait(endTime - System.currentTimeMillis());
                            Log.d(MainActivity.class.getSimpleName(), "Endtime: " + endTime);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                Log.d("ThreadProject", "Выполнен поток № " + numberThread);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Медленная операция завершена", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

        Toast.makeText(this, "Медленная операция запущена в фоновом потоке", Toast.LENGTH_SHORT).show();
    }
}