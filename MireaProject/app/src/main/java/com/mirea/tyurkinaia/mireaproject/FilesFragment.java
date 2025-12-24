package com.mirea.tyurkinaia.mireaproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FilesFragment extends Fragment {

    private static final int REQUEST_PICK_FILE = 1001;

    private TextView selectedFileTextView;
    private Button selectFileButton;
    private Spinner operationSpinner;
    private ProgressBar fileOperationProgressBar;
    private TextView fileOperationStatusTextView;
    private Button executeButton;

    private Uri selectedFileUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_files, container, false);
        selectedFileTextView = root.findViewById(R.id.selectedFileTextView);
        selectFileButton = root.findViewById(R.id.selectFileButton);
        operationSpinner = root.findViewById(R.id.operationSpinner);
        fileOperationProgressBar = root.findViewById(R.id.fileOperationProgressBar);
        fileOperationStatusTextView = root.findViewById(R.id.fileOperationStatusTextView);
        executeButton = root.findViewById(R.id.executeButton);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.file_operations_array,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationSpinner.setAdapter(spinnerAdapter);
        selectFileButton.setOnClickListener(v -> selectFile());
        executeButton.setOnClickListener(v -> executeOperation());

        return root;
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Выберите файл"),
                    REQUEST_PICK_FILE
            );
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Установите файловый менеджер", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeOperation() {
        if (selectedFileUri == null) {
            Toast.makeText(getContext(), "Сначала выберите файл", Toast.LENGTH_SHORT).show();
            return;
        }

        int operationType = operationSpinner.getSelectedItemPosition();
        String operationName = operationSpinner.getSelectedItem().toString();
        fileOperationProgressBar.setVisibility(View.VISIBLE);
        fileOperationStatusTextView.setVisibility(View.VISIBLE);
        fileOperationStatusTextView.setText("Выполнение операции...");
        executeButton.setEnabled(false);
        new Thread(() -> {
            try {
                String operationResult = "";
                String savedPath = "";

                switch (operationType) {
                    case 0: // Calculate Hash (SHA-256)
                        String hashResult = calculateFileHash();
                        if (hashResult != null) {
                            operationResult = hashResult;
                            savedPath = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS) + "/file_hash.txt";
                        }
                        break;

                    case 1: // Convert to Base64
                        String base64Result = convertToBase64();
                        if (base64Result != null) {
                            operationResult = base64Result;
                            savedPath = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS) + "/base64_output.txt";
                        }
                        break;

                    case 2: // Extract Text
                        String extractedText = extractTextFromFile();
                        if (extractedText != null) {
                            operationResult = extractedText;
                            savedPath = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOCUMENTS) + "/extracted_text_" +
                                    getFileNameFromUri(selectedFileUri) + ".txt";
                        }
                        break;
                }

                final String finalResult = operationResult;
                final String finalPath = savedPath;
                final boolean success = !operationResult.isEmpty();

                requireActivity().runOnUiThread(() -> {
                    fileOperationProgressBar.setVisibility(View.GONE);
                    executeButton.setEnabled(true);

                    if (success) {
                        fileOperationStatusTextView.setText("Операция выполнена успешно!");
                        fileOperationStatusTextView.setTextColor(
                                getResources().getColor(android.R.color.holo_green_dark)
                        );
                        showResultDialog(operationName, finalResult, finalPath);
                    } else {
                        fileOperationStatusTextView.setText("Ошибка выполнения операции");
                        fileOperationStatusTextView.setTextColor(
                                getResources().getColor(android.R.color.holo_red_dark)
                        );
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    fileOperationProgressBar.setVisibility(View.GONE);
                    fileOperationStatusTextView.setText("Ошибка: " + e.getMessage());
                    fileOperationStatusTextView.setTextColor(
                            getResources().getColor(android.R.color.holo_red_dark)
                    );
                    executeButton.setEnabled(true);
                });
            }
        }).start();
    }

    private String calculateFileHash() {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) return null;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String hashResult = hexString.toString();
            File hashFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "file_hash.txt"
            );

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(hashFile, true))) {
                writer.write("Файл: " + getFileNameFromUri(selectedFileUri));
                writer.newLine();
                writer.write("SHA-256 Hash: " + hashResult);
                writer.newLine();
                writer.write("Дата: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        .format(new Date()));
                writer.newLine();
                writer.write("---");
                writer.newLine();
            }

            inputStream.close();
            return hashResult;

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertToBase64() {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) return null;

            byte[] buffer = new byte[inputStream.available()];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();

            if (bytesRead <= 0) {
                return null;
            }

            String base64 = android.util.Base64.encodeToString(buffer, 0, bytesRead, android.util.Base64.DEFAULT);
            File base64File = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "base64_output.txt"
            );

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(base64File))) {
                writer.write("=== Base64 кодировка файла: " + getFileNameFromUri(selectedFileUri) + " ===\n\n");
                writer.write("Исходный файл: " + getFileNameFromUri(selectedFileUri));
                writer.newLine();
                writer.write("Размер: " + bytesRead + " байт");
                writer.newLine();
                writer.write("Дата конвертации: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        .format(new Date()));
                writer.newLine();
                writer.write("\nBase64 результат:\n");
                writer.write(base64);
            }

            return base64;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractTextFromFile() {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) return null;

            StringBuilder textContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;

            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                textContent.append(line).append("\n");
                lineCount++;
                if (lineCount > 1000) {
                    textContent.append("\n...[файл слишком большой, показаны первые 1000 строк]");
                    break;
                }
            }

            reader.close();
            inputStream.close();

            String extractedText = textContent.toString();
            File textFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "extracted_text_" + getFileNameFromUri(selectedFileUri) + ".txt"
            );

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
                writer.write("=== Извлеченный текст из файла: " + getFileNameFromUri(selectedFileUri) + " ===\n\n");
                writer.write("Исходный файл: " + getFileNameFromUri(selectedFileUri));
                writer.newLine();
                writer.write("Количество строк: " + lineCount);
                writer.newLine();
                writer.write("Дата извлечения: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                        .format(new Date()));
                writer.newLine();
                writer.write("\nСодержимое:\n");
                writer.write(extractedText);
            }

            return extractedText;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            DocumentFile documentFile = DocumentFile.fromSingleUri(requireContext(), uri);
            if (documentFile != null) {
                fileName = documentFile.getName();
            }
        }

        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName != null ? fileName : "unknown_file";
    }
    private void showResultDialog(String operationType, String result, String savedPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_result, null);

        TextView resultTitleTextView = dialogView.findViewById(R.id.resultTitleTextView);
        TextView resultTypeTextView = dialogView.findViewById(R.id.resultTypeTextView);
        TextView resultFileNameTextView = dialogView.findViewById(R.id.resultFileNameTextView);
        TextView resultContentTextView = dialogView.findViewById(R.id.resultContentTextView);
        TextView resultPathTextView = dialogView.findViewById(R.id.resultPathTextView);
        Button closeButton = dialogView.findViewById(R.id.closeButton);
        switch (operationType) {
            case "Вычислить хэш (SHA-256)":
                resultTitleTextView.setText("Результат вычисления хэша");
                break;
            case "Конвертировать в Base64":
                resultTitleTextView.setText("Результат конвертации в Base64");
                break;
            case "Извлечь текст":
                resultTitleTextView.setText("Извлеченный текст");
                break;
        }

        resultTypeTextView.setText("Тип операции: " + operationType);
        resultFileNameTextView.setText("Файл: " + getFileNameFromUri(selectedFileUri));
        resultContentTextView.setText(result);
        resultPathTextView.setText("Сохранено в: " + savedPath);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    public void showCreateOperationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_file_operation, null);

        android.widget.EditText operationNameEditText = dialogView.findViewById(R.id.operationNameEditText);
        android.widget.EditText filePathEditText = dialogView.findViewById(R.id.filePathEditText);
        android.widget.EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button createButton = dialogView.findViewById(R.id.createButton);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        createButton.setOnClickListener(v -> {
            String operationName = operationNameEditText.getText().toString().trim();
            String filePath = filePathEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (operationName.isEmpty()) {
                Toast.makeText(getContext(), "Введите название операции", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(getContext(), "Запись создана: " + operationName, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_PICK_FILE) {
                selectedFileUri = data.getData();
                String fileName = getFileNameFromUri(selectedFileUri);
                selectedFileTextView.setText("Выбран файл: " + fileName);
                executeButton.setEnabled(true);
            }
        }
    }
}