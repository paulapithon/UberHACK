package com.uber.uberhack.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.uber.uberhack.R;

public class RegisterWordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_word);
    }
    public void onWord (View v) {
        startActivity(new Intent(this, RecordActivity.class));
        finish();
    }
}
