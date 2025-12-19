package com.mirea.tyurkinaia.looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MyLooper extends Thread {
    public Handler mHandler;
    private Handler mainHandler;

    public MyLooper(Handler mainThreadHandler) {
        mainHandler = mainThreadHandler;
    }

    @Override
    public void run() {
        Log.d("MyLooper", "run");
        Looper.prepare();

        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String data = msg.getData().getString("KEY");
                int age = msg.getData().getInt("AGE", 0);
                String profession = msg.getData().getString("PROFESSION");

                Log.d("MyLooper", "Получено сообщение: возраст=" + age + ", профессия=" + profession);
                try {
                    Thread.sleep(age * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String result = String.format(
                        "После ожидания %d секунд:\n" +
                                "Возраст: %d лет\n" +
                                "Профессия: %s\n" +
                                "Количество символов в профессии: %d",
                        age, age, profession, profession.length()
                );
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", result);
                message.setData(bundle);
                mainHandler.sendMessage(message);
            }
        };

        Looper.loop();
    }
}