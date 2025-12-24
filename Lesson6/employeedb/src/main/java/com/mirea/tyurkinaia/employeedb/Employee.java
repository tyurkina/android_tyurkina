package com.mirea.tyurkinaia.employeedb;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employees")
public class Employee {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String alias;
    public String superpower;

    public Employee() {}

    public Employee(String name, String alias, String superpower) {
        this.name = name;
        this.alias = alias;
        this.superpower = superpower;
    }
}