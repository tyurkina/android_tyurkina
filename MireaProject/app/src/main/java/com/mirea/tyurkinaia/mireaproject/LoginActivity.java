package com.mirea.tyurkinaia.mireaproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText fieldEmail;
    private TextInputEditText fieldPassword;
    private TextInputLayout emailContainer;
    private TextInputLayout passwordContainer;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView successTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        fieldEmail = findViewById(R.id.fieldEmail);
        fieldPassword = findViewById(R.id.fieldPassword);
        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        successTextView = findViewById(R.id.successTextView);

        // Set up click listeners
        loginButton.setOnClickListener(v -> {
            String email = fieldEmail.getText().toString().trim();
            String password = fieldPassword.getText().toString().trim();
            signIn(email, password);
        });

        registerButton.setOnClickListener(v -> {
            String email = fieldEmail.getText().toString().trim();
            String password = fieldPassword.getText().toString().trim();
            createAccount(email, password);
        });

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate email
        String email = fieldEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailContainer.setError("Введите email");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailContainer.setError("Некорректный email");
            valid = false;
        } else {
            emailContainer.setError(null);
        }

        // Validate password
        String password = fieldPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            passwordContainer.setError("Введите пароль");
            valid = false;
        } else if (password.length() < 6) {
            passwordContainer.setError("Пароль должен быть не менее 6 символов");
            valid = false;
        } else {
            passwordContainer.setError(null);
        }

        return valid;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
    }

    private void showError(String error) {
        errorTextView.setText(error);
        errorTextView.setVisibility(View.VISIBLE);
        successTextView.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        successTextView.setText(message);
        successTextView.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgress(true);
        showError("");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        showSuccess("Аккаунт успешно создан!");
                        Toast.makeText(LoginActivity.this,
                                "Регистрация успешна!",
                                Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        // Registration failed
                        if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            showError("Слабый пароль. Используйте более сложный пароль.");
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showError("Некорректный email.");
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            showError("Аккаунт с таким email уже существует.");
                        } else {
                            showError("Ошибка регистрации: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgress(true);
        showError("");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        // Sign in successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        showSuccess("Вход выполнен!");
                        Toast.makeText(LoginActivity.this,
                                "Вход выполнен успешно!",
                                Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        // Sign in failed
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            showError("Аккаунт с таким email не найден.");
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showError("Неверный пароль.");
                        } else {
                            showError("Ошибка входа: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
        }
    }
}