package com.mirea.tyurkinaia.mireaproject;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundWorker extends Worker {

    private static final String TAG = "BackgroundWorker";

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Фоновая задача началась");
            String taskName = getInputData().getString("task_name");
            if (taskName != null) {
                Log.d(TAG, "Выполняется задача: " + taskName);
            }

            // Имитация длительной фоновой задачи
            for (int i = 1; i <= 10; i++) {
                Thread.sleep(1000); // Имитация работы
                Log.d(TAG, "Прогресс: " + i * 10 + "%");
                Data progressData = new Data.Builder()
                        .putInt("progress", i * 10)
                        .putString("status", "Выполняется")
                        .build();

                setProgressAsync(progressData);
            }
            Data outputData = new Data.Builder()
                    .putString("result", "Задача успешно выполнена")
                    .putLong("timestamp", System.currentTimeMillis())
                    .build();

            Log.d(TAG, "Фоновая задача завершена успешно");
            return Result.success(outputData);

        } catch (InterruptedException e) {
            Log.e(TAG, "Задача была прервана", e);
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка выполнения задачи", e);
            return Result.failure();
        }
    }
}