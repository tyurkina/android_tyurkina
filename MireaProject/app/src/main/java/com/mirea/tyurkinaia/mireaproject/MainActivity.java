package com.mirea.tyurkinaia.mireaproject;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.mirea.tyurkinaia.mireaproject.databinding.ActivityMainBinding;
import com.yandex.mapkit.MapKitFactory;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("3f789182-85fb-4813-92aa-9ac503cd39cd");
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(
                        MainActivity.this,
                        R.id.nav_host_fragment_content_main
                );
                if (navController.getCurrentDestination() != null) {
                    int currentDestination = navController.getCurrentDestination().getId();

                    if (currentDestination == R.id.nav_files) {
                        androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment_content_main);

                        if (navHostFragment != null) {
                            androidx.fragment.app.Fragment currentFragment = null;
                            currentFragment = navHostFragment.getChildFragmentManager()
                                    .getPrimaryNavigationFragment();
                            if (currentFragment == null || !(currentFragment instanceof FilesFragment)) {
                                for (androidx.fragment.app.Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
                                    if (fragment instanceof FilesFragment) {
                                        currentFragment = fragment;
                                        break;
                                    }
                                }
                            }

                            if (currentFragment instanceof FilesFragment) {
                                ((FilesFragment) currentFragment).showCreateOperationDialog();
                                return;
                            }
                        }
                    }
                }
                Snackbar.make(view, "Создание новой записи доступно только в разделе 'Работа с файлами'",
                        Snackbar.LENGTH_LONG).show();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_data,
                R.id.nav_webview,
                R.id.nav_background_task,
                R.id.nav_compass,
                R.id.nav_camera,
                R.id.nav_microphone,
                R.id.nav_profile,
                R.id.nav_files,
                R.id.nav_news,
                R.id.nav_maps
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}