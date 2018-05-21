package com.uber.uberhack;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.uber.uberhack.view.register.AboutActivity;
import com.uber.uberhack.view.word.WordFinishActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (!UberHACKApplication.getSafeWord().equals("")) {
                    intent = new Intent(SplashActivity.this, WordFinishActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, AboutActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
