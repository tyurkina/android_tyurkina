package com.mirea.tyurkinaia.employeedb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etAlias, etSuperpower, etSearch;
    private Button btnAdd, btnSearch;
    private TextView tvEmployees, tvResult;
    private AppDatabase database;
    private EmployeeDao employeeDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate called");
        initViews();
        setupListeners();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initializeDatabase();
        }, 500);
    }

    private void initViews() {
        try {
            etName = findViewById(R.id.etName);
            etAlias = findViewById(R.id.etAlias);
            etSuperpower = findViewById(R.id.etSuperpower);
            etSearch = findViewById(R.id.etSearch);

            btnAdd = findViewById(R.id.btnAdd);
            btnSearch = findViewById(R.id.btnSearch);

            tvEmployees = findViewById(R.id.tvEmployees);
            tvResult = findViewById(R.id.tvResult);

            Log.d("MainActivity", "Views initialized");
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void initializeDatabase() {
        try {
            App app = App.getInstance();
            if (app == null) {
                Toast.makeText(this, "Приложение еще не инициализировано", Toast.LENGTH_LONG).show();
                return;
            }

            database = app.getDatabase();
            if (database != null) {
                employeeDao = database.employeeDao();
                Log.d("MainActivity", "Database initialized successfully");
                loadAllEmployees();
                btnAdd.setEnabled(true);
                btnSearch.setEnabled(true);
            } else {
                Log.e("MainActivity", "Database is null");
                Toast.makeText(this, "База данных не инициализирована", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing database: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации базы данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                initializeDatabase();
            }, 1000);
        }
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> addEmployee());
        btnSearch.setOnClickListener(v -> searchEmployees());
        btnAdd.setEnabled(false);
        btnSearch.setEnabled(false);
    }

    private void addEmployee() {
        String name = etName.getText().toString().trim();
        String alias = etAlias.getText().toString().trim();
        String superpower = etSuperpower.getText().toString().trim();

        if (name.isEmpty() || alias.isEmpty() || superpower.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                Employee employee = new Employee(name, alias, superpower);
                employeeDao.insert(employee);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Супер-герой добавлен!", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etAlias.setText("");
                    etSuperpower.setText("");
                    loadAllEmployees();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Error adding employee: " + e.getMessage(), e);
                    Toast.makeText(this, "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void searchEmployees() {
        String searchText = etSearch.getText().toString().trim();

        executorService.execute(() -> {
            try {
                List<Employee> employees;

                if (searchText.isEmpty()) {
                    employees = employeeDao.getAll();
                    final String message = "Все супер-герои: " + employees.size() + " записей";

                    runOnUiThread(() -> {
                        displayEmployees(employees);
                        tvResult.setText(message);
                    });

                } else {
                    employees = employeeDao.searchEmployees(searchText);
                    final String message = "Результаты поиска: " + employees.size() + " найден(о)";

                    runOnUiThread(() -> {
                        displayEmployees(employees);
                        tvResult.setText(message);
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Error searching employees: " + e.getMessage(), e);
                    Toast.makeText(this, "Ошибка поиска", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadAllEmployees() {
        executorService.execute(() -> {
            try {
                List<Employee> employees = employeeDao.getAll();
                final String message = "Все супер-герои: " + employees.size() + " записей";

                runOnUiThread(() -> {
                    displayEmployees(employees);
                    tvResult.setText(message);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("MainActivity", "Error loading employees: " + e.getMessage(), e);
                    tvEmployees.setText("Ошибка загрузки данных: " + e.getMessage());
                });
            }
        });
    }

    private void displayEmployees(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();

        if (employees != null && !employees.isEmpty()) {
            for (Employee employee : employees) {
                sb.append("ID: ").append(employee.id).append("\n")
                        .append("Имя: ").append(employee.name).append("\n")
                        .append("Псевдоним: ").append(employee.alias).append("\n")
                        .append("Суперспособность: ").append(employee.superpower).append("\n")
                        .append("----------------------------\n\n");
            }
        } else {
            sb.append("Нет записей в базе данных.\n");
            sb.append("Добавьте нового супер-героя!");
        }

        tvEmployees.setText(sb.toString());
    }

    @Override
    protected void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
}