package com.uber.uberhack.view.word;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.uber.uberhack.R;
import com.uber.uberhack.service.SpeechRecognitionService;
import com.uber.uberhack.UberHACKApplication;
import com.uber.uberhack.view.register.FinishActivity;

public class WordFinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_finish);

        // Setar palavra chave
        if (!UberHACKApplication.getSafeWord().equals("")) {
            ((TextView)findViewById(R.id.safe_word)).setText(UberHACKApplication.getSafeWord());
            startService(new Intent(this, SpeechRecognitionService.class));
        }

    }

    // Terminar configuração
    public void onTerminar (View v) {
        startActivity(new Intent(this, FinishActivity.class));
        finish();
    }

    // Registrar nova palavra chave
    public void onDeNovo (View v) {
        UberHACKApplication.setSafeWord("");
        startActivity(new Intent(this, RecordActivity.class));
        finish();
    }
}
