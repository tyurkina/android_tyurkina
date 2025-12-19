package com.mirea.tyurkinaia.dialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.DialogFragment;

public class MyProgressDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Загрузка данных...");
        progressDialog.setTitle("Пожалуйста, подождите");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);

        // Запускаем симуляцию загрузки
        startProgressSimulation(progressDialog);

        return progressDialog;
    }

    private void startProgressSimulation(ProgressDialog progressDialog) {
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(500);
                    int finalI = i;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressDialog.setProgress(finalI);
                        if (finalI == 100) {
                            progressDialog.dismiss();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}