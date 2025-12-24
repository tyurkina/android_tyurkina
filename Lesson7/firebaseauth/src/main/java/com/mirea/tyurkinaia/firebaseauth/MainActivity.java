package com.mirea.tyurkinaia.firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mirea.tyurkinaia.firebaseauth.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupSectionHeights();
        mAuth = FirebaseAuth.getInstance();
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonStates();
            }
        };

        binding.fieldEmail.addTextChangedListener(textWatcher);
        binding.fieldPassword.addTextChangedListener(textWatcher);
        binding.emailCreateAccountButton.setOnClickListener(v -> {
            String email = binding.fieldEmail.getText().toString();
            String password = binding.fieldPassword.getText().toString();
            createAccount(email, password);
        });

        binding.emailSignInButton.setOnClickListener(v -> {
            String email = binding.fieldEmail.getText().toString();
            String password = binding.fieldPassword.getText().toString();
            signIn(email, password);
        });

        binding.signOutButton.setOnClickListener(v -> signOut());
        binding.verifyEmailButton.setOnClickListener(v -> sendEmailVerification());
    }

    private void setupSectionHeights() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        ViewGroup.LayoutParams topParams = binding.topSection.getLayoutParams();
        topParams.height = (int) (screenHeight * 0.67);
        binding.topSection.setLayoutParams(topParams);

        ViewGroup.LayoutParams bottomParams = binding.bottomSection.getLayoutParams();
        bottomParams.height = (int) (screenHeight * 0.33);
        binding.bottomSection.setLayoutParams(bottomParams);
    }

    private void updateButtonStates() {
        boolean hasEmail = !TextUtils.isEmpty(binding.fieldEmail.getText());
        boolean hasPassword = !TextUtils.isEmpty(binding.fieldPassword.getText());
        boolean isSignedIn = mAuth.getCurrentUser() != null;

        if (!isSignedIn) {
            binding.emailSignInButton.setEnabled(hasEmail && hasPassword);
            binding.emailCreateAccountButton.setEnabled(hasEmail && hasPassword);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.statusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            binding.detailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            binding.detailTextView.setVisibility(View.VISIBLE);
            binding.emailPasswordContainer.setVisibility(View.GONE);
            binding.signedInButtons.setVisibility(View.VISIBLE);
            binding.verifyEmailButton.setEnabled(!user.isEmailVerified());

        } else {
            binding.statusTextView.setText(R.string.signed_out);
            binding.detailTextView.setText(null);
            binding.detailTextView.setVisibility(View.GONE);
            binding.emailPasswordContainer.setVisibility(View.VISIBLE);
            binding.signedInButtons.setVisibility(View.GONE);
            binding.fieldEmail.setText("");
            binding.fieldPassword.setText("");
            binding.emailContainer.setError(null);
            binding.passwordContainer.setError(null);
        }
        updateButtonStates();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = binding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            binding.emailContainer.setError("Required.");
            valid = false;
        } else {
            binding.emailContainer.setError(null);
        }

        String password = binding.fieldPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            binding.passwordContainer.setError("Required.");
            valid = false;
        } else {
            binding.passwordContainer.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        binding.emailCreateAccountButton.setEnabled(false);
        binding.emailSignInButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    updateButtonStates();

                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        Toast.makeText(MainActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Sign in failed
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                        // Handle specific exceptions
                        if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(MainActivity.this,
                                    "Weak password. Please use a stronger password.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(MainActivity.this,
                                    "Invalid email format.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(MainActivity.this,
                                    "Account with this email already exists.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateUI(null);
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        binding.emailSignInButton.setEnabled(false);
        binding.emailCreateAccountButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    updateButtonStates();

                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        Toast.makeText(MainActivity.this,
                                "Signed in successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(MainActivity.this,
                                    "No account found with this email.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(MainActivity.this,
                                    "Invalid password.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateUI(null);
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }

    private void sendEmailVerification() {
        // Disable button
        binding.verifyEmailButton.setEnabled(false);

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(this, task -> {
                        binding.verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}