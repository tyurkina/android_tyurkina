package com.mirea.tyurkinaia.employeedb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EmployeeDao {
    @Query("SELECT * FROM employees")
    List<Employee> getAll();

    @Query("SELECT * FROM employees WHERE id = :id")
    Employee getById(long id);

    @Query("SELECT * FROM employees WHERE alias LIKE '%' || :search || '%' OR name LIKE '%' || :search || '%'")
    List<Employee> searchEmployees(String search);

    @Insert
    void insert(Employee employee);

    @Update
    void update(Employee employee);

    @Delete
    void delete(Employee employee);
}