package com.uber.uberhack.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.uber.uberhack.R;
import com.uber.uberhack.UberHACKApplication;

public class WordFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_finish);

        ((TextView)findViewById(R.id.safe_word)).setText(UberHACKApplication.safeWord);
    }

    public void onTerminar (View v) {
        startActivity(new Intent(this, FinishActivity.class));
        finish();
    }

    public void onDeNovo (View v) {
        UberHACKApplication.safeWord = null;
        startActivity(new Intent(this, RecordActivity.class));
        finish();
    }
}
