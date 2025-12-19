package com.mirea.tyurkinaia.favoritebook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ShareActivity extends AppCompatActivity {

    private TextView textViewDeveloperBook;
    private TextView textViewDeveloperQuote;
    private EditText editTextUserBook;
    private EditText editTextUserQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        textViewDeveloperBook = findViewById(R.id.textViewDeveloperBook);
        textViewDeveloperQuote = findViewById(R.id.textViewDeveloperQuote);
        editTextUserBook = findViewById(R.id.editTextUserBook);
        editTextUserQuote = findViewById(R.id.editTextUserQuote);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String book_name = extras.getString(MainActivity.BOOK_NAME_KEY);
            String quotes_name = extras.getString(MainActivity.QUOTES_KEY);

            textViewDeveloperBook.setText("Любимая книга разработчика: " + book_name);
            textViewDeveloperQuote.setText("Цитата из книги: " + quotes_name);
        }
    }

    public void sendUserData(View view) {
        String userBook = editTextUserBook.getText().toString().trim();
        String userQuote = editTextUserQuote.getText().toString().trim();

        if (userBook.isEmpty()) {
            editTextUserBook.setError("Введите название книги");
            return;
        }

        if (userQuote.isEmpty()) {
            editTextUserQuote.setError("Введите цитату из книги");
            return;
        }

        String text = "Название Вашей любимой книги: " + userBook +
                ". Цитата: " + userQuote;

        Intent data = new Intent();
        data.putExtra(MainActivity.USER_MESSAGE, text);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}