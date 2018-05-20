package com.uber.uberhack.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.uber.uberhack.R;
import com.uber.uberhack.UberHACKApplication;

public class PeoplesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peoples);
    }

    public void onAddContact (View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_contact, null);
        builder.setView(view);
        builder.setTitle("Inserir Pessoa");

        final EditText nome = view.findViewById(R.id.nome_contato);
        final EditText telefone = view.findViewById(R.id.telefone_contato);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UberHACKApplication.pessoaNome = nome.getText().toString();
                UberHACKApplication.pessoaTelefone = telefone.getText().toString();

                startActivity(new Intent(PeoplesActivity.this, RegisterWordActivity.class));
                finish();
            }
        });

        builder.show();


    }
}
