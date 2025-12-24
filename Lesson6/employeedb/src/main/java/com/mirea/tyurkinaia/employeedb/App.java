package com.mirea.tyurkinaia.employeedb;

import android.app.Application;
import android.util.Log;
import androidx.room.Room;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    private static App instance;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("App", "Application onCreate called");

        instance = this;
        try {
            database = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class,
                            "employee-database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();

            Log.d("App", "Database created successfully");

            // Добавляем тестовые данные в фоновом потоке
            addTestData();

        } catch (Exception e) {
            Log.e("App", "Error creating database: " + e.getMessage(), e);
        }
    }

    public static App getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return database;
    }

    private void addTestData() {
        executorService.execute(() -> {
            try {
                EmployeeDao dao = database.employeeDao();
                List<Employee> employees = dao.getAll();

                if (employees == null || employees.isEmpty()) {
                    Log.d("App", "Adding test data...");

                    Employee[] testEmployees = {
                            new Employee("Tony Stark", "Iron Man", "Genius, billionaire, playboy, philanthropist"),
                            new Employee("Piter Parker", "Spider-man", "Spider-Sense, shoot webs"),
                            new Employee("Tor", "Tor", "god of thunder with a hammer")
                    };

                    for (Employee employee : testEmployees) {
                        dao.insert(employee);
                    }

                    Log.d("App", "Test data added successfully");
                } else {
                    Log.d("App", "Database already contains data: " + employees.size() + " records");
                }
            } catch (Exception e) {
                Log.e("App", "Error adding test data: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onTerminate() {
        executorService.shutdown();
        super.onTerminate();
    }
}