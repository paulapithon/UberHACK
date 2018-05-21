package com.uber.uberhack.view.register;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.uber.uberhack.R;

public class SafePlaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_place);
    }

    public void onDoneLocais (View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Endereço");
        builder.setMessage("Digite o endereço do seu lugar seguro:");

        EditText text = new EditText(this);
        builder.setView(text);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(SafePlaceActivity.this, ContactActivity.class));
                finish();
            }
        });

        builder.show();
    }
}
