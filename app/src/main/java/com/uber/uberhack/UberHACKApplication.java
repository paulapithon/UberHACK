package com.uber.uberhack;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by paula on 20/05/18.
 */

public class UberHACKApplication extends Application {

    private static SharedPreferences sPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sPreferences = getSharedPreferences("UBERHackApplication", Context.MODE_PRIVATE);
    }

    public static String getSafeWord() {
        return sPreferences.getString("UBERHACK_SAFE_WORD", "");
    }

    public static void setSafeWord(String safeWord) {
        sPreferences
                .edit()
                .putString("UBERHACK_SAFE_WORD", safeWord)
                .apply();
    }

    public static String getSafePhone() {
        return sPreferences.getString("UBERHACK_SAFE_PHONE", "");
    }

    public static void setSafePhone (String safePhone) {
        sPreferences
                .edit()
                .putString("UBERHACK_SAFE_PHONE", safePhone)
                .apply();
    }

}
