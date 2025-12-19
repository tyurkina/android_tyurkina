package com.mirea.tyurkinaia.looper;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MyLooper myLooper;
    private EditText editTextAge;
    private EditText editTextProfession;
    private Button buttonProcess;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAge = findViewById(R.id.editTextAge);
        editTextProfession = findViewById(R.id.editTextProfession);
        buttonProcess = findViewById(R.id.buttonProcess);
        textViewResult = findViewById(R.id.textViewResult);

        Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.getData().getString("result");
                textViewResult.setText(result);
                Log.d("MainActivity", "Получен результат: " + result);
                Toast.makeText(MainActivity.this, "Обработка завершена!", Toast.LENGTH_SHORT).show();
            }
        };
        myLooper = new MyLooper(mainThreadHandler);
        myLooper.start();
        buttonProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ageStr = editTextAge.getText().toString();
                String profession = editTextProfession.getText().toString();

                if (ageStr.isEmpty() || profession.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                int age = Integer.parseInt(ageStr);
                if (age <= 0) {
                    Toast.makeText(MainActivity.this, "Возраст должен быть положительным числом", Toast.LENGTH_SHORT).show();
                    return;
                }

                textViewResult.setText("Обработка... Ждем " + age + " секунд");
                Toast.makeText(MainActivity.this, "Начата обработка с задержкой " + age + " секунд", Toast.LENGTH_SHORT).show();

                // Отправляем данные в поток MyLooper
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("KEY", "user_data");
                bundle.putInt("AGE", age);
                bundle.putString("PROFESSION", profession);
                msg.setData(bundle);

                // Проверяем, создан ли уже Handler в потоке MyLooper
                if (myLooper.mHandler != null) {
                    myLooper.mHandler.sendMessage(msg);
                    Log.d("MainActivity", "Данные отправлены в MyLooper: возраст=" + age + ", профессия=" + profession);
                } else {
                    Toast.makeText(MainActivity.this, "Очередь еще не готова. Подождите...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myLooper != null && myLooper.mHandler != null) {
            myLooper.mHandler.getLooper().quit();
        }
    }
}