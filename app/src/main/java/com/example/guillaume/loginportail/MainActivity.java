package com.example.guillaume.loginportail;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "http log";
    private ProgressDialog progressDialog;
    private final Button button = (Button) findViewById(R.id.connexion);
    private final EditText username = (EditText) findViewById(R.id.username);
    private final EditText password = (EditText) findViewById(R.id.input_password);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final MainActivity temp = this;

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setEnabled(false);
                String[] arrData = {username.getText().toString(),password.getText().toString(), "240"};
                String[] arrKey = {"n_uid","pswd","n_time"};
                new DoPostRequest(arrData,arrKey, temp).execute( "https://fw-cgcp.emse.fr/auth/plain.html");//,arrData,arrKey
                progressDialog = new ProgressDialog(MainActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Connexion...");
                progressDialog.show();
            }
        });
    }

    public void onLoginSuccess(){
        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("La connexion a reussit")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();

    }

    public void onLoginFail(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("La connexion a echoué")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        progressDialog.dismiss();
        button.setEnabled(true);


    }





}
