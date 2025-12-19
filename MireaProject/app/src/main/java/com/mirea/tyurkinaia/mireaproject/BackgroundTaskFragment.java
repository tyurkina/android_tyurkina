package com.mirea.tyurkinaia.mireaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.mirea.tyurkinaia.mireaproject.R;
import com.mirea.tyurkinaia.mireaproject.BackgroundWorker;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BackgroundTaskFragment extends Fragment {

    private TextView taskInfoTextView;
    private TextView progressTextView;
    private TextView logTextView;
    private ProgressBar progressBar;
    private Button startTaskButton;
    private Button scheduleTaskButton;
    private Button cancelTaskButton;

    private WorkManager workManager;
    private UUID currentWorkId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_background_task, container, false);

        // Инициализация элементов UI
        taskInfoTextView = root.findViewById(R.id.taskInfoTextView);
        progressTextView = root.findViewById(R.id.progressTextView);
        logTextView = root.findViewById(R.id.logTextView);
        progressBar = root.findViewById(R.id.progressBar);
        startTaskButton = root.findViewById(R.id.startTaskButton);
        scheduleTaskButton = root.findViewById(R.id.scheduleTaskButton);
        cancelTaskButton = root.findViewById(R.id.cancelTaskButton);

        workManager = WorkManager.getInstance(requireContext());
        setupButtonListeners();

        return root;
    }

    private void setupButtonListeners() {
        startTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOneTimeTask();
            }
        });

        scheduleTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedulePeriodicTask();
            }
        });

        cancelTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAllTasks();
            }
        });
    }

    private void startOneTimeTask() {
        addLog("Запуск разовой фоновой задачи...");
        Data inputData = new Data.Builder()
                .putString("task_name", "Фоновая обработка данных")
                .putInt("attempt", 1)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BackgroundWorker.class)
                .setInputData(inputData)
                .addTag("background_task")
                .build();

        currentWorkId = workRequest.getId();
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(currentWorkId).observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null) {
                    updateTaskInfo(workInfo);
                }
            }
        });

        taskInfoTextView.setText("Задача поставлена в очередь");
        progressBar.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
    }

    private void schedulePeriodicTask() {
        addLog("Планирование повторяющейся задачи...");
        Data inputData = new Data.Builder()
                .putString("task_name", "Периодическая синхронизация")
                .putInt("attempt", 1)
                .build();
        // Создание повторяющейся задачи (минимальный интервал - 15 минут)
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(BackgroundWorker.class, 15, TimeUnit.MINUTES)
                        .setInputData(inputData)
                        .addTag("periodic_task")
                        .build();

        workManager.enqueue(periodicWorkRequest);

        // Наблюдение за задачей
        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null) {
                            addLog("Периодическая задача: " + workInfo.getState().toString());
                        }
                    }
                });

        taskInfoTextView.setText("Повторяющаяся задача запланирована (каждые 15 минут)");
        addLog("Повторяющаяся задача запланирована успешно");
    }

    private void cancelAllTasks() {
        addLog("Отмена всех задач...");
        workManager.cancelAllWork();
        taskInfoTextView.setText("Все задачи отменены");
        progressBar.setProgress(0);
        progressTextView.setText("Прогресс: 0%");
        addLog("Все задачи отменены");
    }

    private void updateTaskInfo(WorkInfo workInfo) {
        WorkInfo.State state = workInfo.getState();

        switch (state) {
            case ENQUEUED:
                taskInfoTextView.setText("Задача в очереди на выполнение");
                addLog("Задача поставлена в очередь");
                break;

            case RUNNING:
                taskInfoTextView.setText("Задача выполняется");

                // Получение прогресса
                Data progressData = workInfo.getProgress();
                int progress = progressData.getInt("progress", 0);
                String status = progressData.getString("status");

                if (progress > 0) {
                    progressBar.setProgress(progress);
                    progressTextView.setText("Прогресс: " + progress + "%");
                    progressTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    if (status != null) {
                        addLog(status + " - " + progress + "%");
                    }
                }
                break;

            case SUCCEEDED:
                taskInfoTextView.setText("Задача успешно завершена");
                progressBar.setVisibility(View.GONE);
                progressTextView.setVisibility(View.GONE);

                // Получение результатов
                Data outputData = workInfo.getOutputData();
                String result = outputData.getString("result");
                long timestamp = outputData.getLong("timestamp", 0);

                if (result != null) {
                    addLog("Результат: " + result);
                }
                if (timestamp > 0) {
                    addLog("Время выполнения: " + new java.util.Date(timestamp));
                }
                break;

            case FAILED:
                taskInfoTextView.setText("Задача завершилась с ошибкой");
                progressBar.setVisibility(View.GONE);
                progressTextView.setVisibility(View.GONE);
                addLog("Задача завершилась с ошибкой");
                break;

            case BLOCKED:
                taskInfoTextView.setText("Задача заблокирована");
                addLog("Задача заблокирована другими задачами");
                break;

            case CANCELLED:
                taskInfoTextView.setText("Задача отменена");
                progressBar.setVisibility(View.GONE);
                progressTextView.setVisibility(View.GONE);
                addLog("Задача отменена");
                break;
        }
    }

    private void addLog(String message) {
        String currentLog = logTextView.getText().toString();
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        logTextView.setText(currentLog + timestamp + " - " + message + "\n");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Отписываемся от наблюдателей
        if (currentWorkId != null) {
            workManager.getWorkInfoByIdLiveData(currentWorkId).removeObservers(getViewLifecycleOwner());
        }
    }
}