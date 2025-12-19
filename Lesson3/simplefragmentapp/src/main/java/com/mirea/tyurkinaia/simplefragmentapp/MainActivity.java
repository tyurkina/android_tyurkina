package com.mirea.tyurkinaia.simplefragmentapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Fragment firstFragment;
    private Fragment secondFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstFragment = FirstFragment.newInstance();
        secondFragment = SecondFragment.newInstance();
        boolean isLandscape = findViewById(R.id.fragmentFirst) != null
                && findViewById(R.id.fragmentSecond) != null;

        if (isLandscape) {
            setupLandscapeLayout();
        } else {
            setupPortraitLayout();
        }
    }

    private void setupPortraitLayout() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Button btnFirstFragment = findViewById(R.id.btnFirstFragment);
        Button btnSecondFragment = findViewById(R.id.btnSecondFragment);

        btnFirstFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragmentContainer, firstFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        btnSecondFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragmentContainer, secondFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        if (fragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragmentContainer, firstFragment);
            transaction.commit();
        }
    }

    private void setupLandscapeLayout() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentFirst, firstFragment);
        transaction.replace(R.id.fragmentSecond, secondFragment);
        transaction.commit();

    }
}