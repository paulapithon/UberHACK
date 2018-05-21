package com.uber.uberhack.view.register;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.uber.uberhack.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }
    public void onClickSobre (View v) {
        EditText text = findViewById(R.id.input_nome);
        if (!text.getText().toString().equals("")) {
            Intent intent = new Intent(this, HelloActivity.class);
            intent.putExtra("NOME", text.getText().toString());
            startActivity(intent);
            finish();
        }
    }
}
