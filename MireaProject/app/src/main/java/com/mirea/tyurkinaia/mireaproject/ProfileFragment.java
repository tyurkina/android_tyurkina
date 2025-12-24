package com.mirea.tyurkinaia.mireaproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_AGE = "age";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";

    private EditText nameEditText;
    private EditText ageEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button saveButton;
    private Button clearButton;
    private TextView statusTextView;

    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        nameEditText = root.findViewById(R.id.nameEditText);
        ageEditText = root.findViewById(R.id.ageEditText);
        emailEditText = root.findViewById(R.id.emailEditText);
        phoneEditText = root.findViewById(R.id.phoneEditText);
        saveButton = root.findViewById(R.id.saveButton);
        clearButton = root.findViewById(R.id.clearButton);
        statusTextView = root.findViewById(R.id.statusTextView);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        loadProfileData();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearProfileData();
            }
        });

        return root;
    }

    private void loadProfileData() {
        // Load data from SharedPreferences
        nameEditText.setText(sharedPreferences.getString(KEY_NAME, ""));
        ageEditText.setText(sharedPreferences.getString(KEY_AGE, ""));
        emailEditText.setText(sharedPreferences.getString(KEY_EMAIL, ""));
        phoneEditText.setText(sharedPreferences.getString(KEY_PHONE, ""));
    }

    private void saveProfileData() {
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        if (name.isEmpty()) {
            showStatus("Пожалуйста, введите имя", true);
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_AGE, age);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);

        if (editor.commit()) {
            showStatus("Данные профиля сохранены", false);
            Toast.makeText(getContext(), "Профиль сохранен", Toast.LENGTH_SHORT).show();
        } else {
            showStatus("Ошибка сохранения данных", true);
        }
    }

    private void clearProfileData() {
        nameEditText.setText("");
        ageEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        showStatus("Данные профиля очищены", false);
        Toast.makeText(getContext(), "Профиль очищен", Toast.LENGTH_SHORT).show();
    }

    private void showStatus(String message, boolean isError) {
        if (statusTextView != null) {
            statusTextView.setText(message);
            statusTextView.setVisibility(View.VISIBLE);

            if (isError) {
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
            statusTextView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (statusTextView != null) {
                        statusTextView.setVisibility(View.GONE);
                    }
                }
            }, 3000);
        }
    }
}