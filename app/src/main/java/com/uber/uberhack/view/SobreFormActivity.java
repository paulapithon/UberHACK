package com.uber.uberhack.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.uber.uberhack.R;

public class SobreFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_form);
    }
    public void onClickSobre (View v) {
        startActivity(new Intent(this, RegisterWordActivity.class));
        finish();
    }
}
