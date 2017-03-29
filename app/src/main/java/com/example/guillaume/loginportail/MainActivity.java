package com.example.guillaume.loginportail;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "http log";
    private ProgressDialog progressDialog;
    private Button button;
    private EditText username ;
    private EditText password ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.connexion);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.input_password);

        DisableSsl.disableSSLCertificateChecking();
        final MainActivity temp = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("En utilisant cette application, vous acceptez les conditions d'utilisation du réseau de la résidence")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button.setEnabled(false);
                String[] arrData = {username.getText().toString(),password.getText().toString(), "240"};
                String[] arrKey = {"n_uid","pswd","n_time"};
                new SendCredentials(arrData,arrKey, temp, "https://fw-cgcp.emse.fr/auth/plain.html").execute( "");//,arrData,arrKey
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

        //si le user veut qu'on garde ses id:


        AlertDialog alert = builder.create();
        alert.show();

        button.setEnabled(true);

    }

    public void onLoginFail(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("La connexion a echoué")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        progressDialog.dismiss();
        button.setEnabled(true);


    }


    public void acceptCond(String html){
        String session;
        int debutSession;


        debutSession = html.indexOf("value=\"id=");
        session = html.substring(debutSession + 7, html.indexOf("\">", debutSession));


        Log.e("session id",session);
        String[] arrData = {session, "accepted", "J'accepte"};
        String[] arrKey = {"session", "read", "action"};


        new AcceptCond(this,arrData, arrKey, "https://fw-cgcp.emse.fr/auth/disclaimer.html").execute("");

    }




}
