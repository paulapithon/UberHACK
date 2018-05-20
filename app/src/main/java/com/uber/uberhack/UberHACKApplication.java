package com.uber.uberhack;

import android.app.Application;

/**
 * Created by paula on 20/05/18.
 */

public class UberHACKApplication extends Application {

    public static String safeWord;

    @Override
    public void onCreate() {
        super.onCreate();
        safeWord = "cachorro";
    }
}
