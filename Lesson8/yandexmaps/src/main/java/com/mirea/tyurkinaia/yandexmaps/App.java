package com.mirea.tyurkinaia.yandexmaps;

import android.app.Application;
import com.yandex.mapkit.MapKitFactory;

public class App extends Application {
    private final String MAPKIT_API_KEY = "3f789182-85fb-4813-92aa-9ac503cd39cd";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            MapKitFactory.setApiKey(MAPKIT_API_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}