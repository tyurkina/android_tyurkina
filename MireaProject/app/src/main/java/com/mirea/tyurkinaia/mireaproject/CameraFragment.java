package com.mirea.tyurkinaia.mireaproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.mirea.tyurkinaia.mireaproject.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final int REQUEST_IMAGE_CAPTURE = 103;
    private static final int REQUEST_IMAGE_PICK = 104;
    private static final int REQUEST_IMAGE_PICK_ANDROID_13 = 105;

    private GridLayout collageGrid;
    private ImageView[] imageViews;
    private Button takePhotoButton;
    private Button selectPhotoButton;
    private Button saveCollageButton;
    private TextView statusTextView;

    private int currentImageIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        collageGrid = root.findViewById(R.id.collageGrid);
        takePhotoButton = root.findViewById(R.id.takePhotoButton);
        selectPhotoButton = root.findViewById(R.id.selectPhotoButton);
        saveCollageButton = root.findViewById(R.id.saveCollageButton);
        statusTextView = root.findViewById(R.id.statusTextView);

        // Инициализация массива ImageView
        imageViews = new ImageView[]{
                root.findViewById(R.id.imageView1),
                root.findViewById(R.id.imageView2),
                root.findViewById(R.id.imageView3),
                root.findViewById(R.id.imageView4)
        };

        // Настройка обработчиков нажатий
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            }
        });

        saveCollageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCollage();
            }
        });

        // Проверяем начальное состояние кнопки сохранения
        saveCollageButton.setEnabled(currentImageIndex >= imageViews.length);

        return root;
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android ниже 13
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Android ниже 13
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Проверяем, есть ли приложение камеры
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "На устройстве не найдено приложение камеры", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        // Проверяем, есть ли приложение галереи
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        } else {
            // Альтернативный способ для старых устройств
            Intent intentOld = new Intent(Intent.ACTION_GET_CONTENT);
            intentOld.setType("image/*");
            if (intentOld.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(intentOld, REQUEST_IMAGE_PICK);
            } else {
                Toast.makeText(getContext(), "На устройстве не найдено приложение галереи", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Фото сделано камерой
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null && currentImageIndex < imageViews.length) {
                        imageViews[currentImageIndex].setImageBitmap(imageBitmap);
                        currentImageIndex++;
                        updateStatus();
                        Toast.makeText(getContext(), "Фото добавлено в коллаж", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // Фото выбрано из галереи
                Uri selectedImage = data.getData();
                if (selectedImage != null && currentImageIndex < imageViews.length) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(),
                                selectedImage
                        );
                        // Масштабируем изображение для экономии памяти
                        Bitmap scaledBitmap = scaleBitmap(bitmap, 800, 800);
                        imageViews[currentImageIndex].setImageBitmap(scaledBitmap);
                        currentImageIndex++;
                        updateStatus();
                        Toast.makeText(getContext(), "Фото добавлено в коллаж", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void updateStatus() {
        int remaining = imageViews.length - currentImageIndex;
        statusTextView.setText("Осталось добавить: " + remaining + " фото");

        if (currentImageIndex >= imageViews.length) {
            statusTextView.setText("Коллаж готов! Можно сохранить.");
            saveCollageButton.setEnabled(true);
        }
    }

    private void saveCollage() {
        try {
            // Создание битмапа из GridLayout
            collageGrid.setDrawingCacheEnabled(true);
            collageGrid.buildDrawingCache();

            // Устанавливаем размеры для измерения
            collageGrid.measure(
                    View.MeasureSpec.makeMeasureSpec(collageGrid.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(collageGrid.getHeight(), View.MeasureSpec.EXACTLY)
            );
            collageGrid.layout(0, 0, collageGrid.getMeasuredWidth(), collageGrid.getMeasuredHeight());

            Bitmap collageBitmap = Bitmap.createBitmap(collageGrid.getDrawingCache());
            collageGrid.setDrawingCacheEnabled(false);

            // Сохранение коллажа
            saveBitmapToStorage(collageBitmap);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка сохранения коллажа", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveBitmapToStorage(Bitmap bitmap) {
        try {
            // Создаем имя файла с временной меткой
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Collage_" + timeStamp + ".jpg";

            // Сохраняем во внутреннее хранилище приложения
            File storageDir = requireContext().getExternalFilesDir(null);
            File imageFile = new File(storageDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // Обновляем галерею
            MediaStore.Images.Media.insertImage(
                    requireContext().getContentResolver(),
                    imageFile.getAbsolutePath(),
                    fileName,
                    "Коллаж из приложения MireaProject"
            );

            // Отправляем broadcast для обновления галереи
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(imageFile));
            requireContext().sendBroadcast(mediaScanIntent);

            Toast.makeText(getContext(),
                    "Коллаж сохранен: " + imageFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            statusTextView.setText("Коллаж сохранен успешно!");

        } catch (IOException e) {
            Toast.makeText(getContext(), "Ошибка сохранения файла", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Для использования камеры необходимо разрешение", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Для выбора фото необходимо разрешение", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем статус при возвращении на фрагмент
        updateStatus();
    }
}