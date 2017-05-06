package com.example.guillaume.loginportail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;


public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "http log";
    private ProgressDialog progressDialog;
    private Button button;
    private EditText username ;
    private EditText password ;
    private String aliasText;
    private KeyStore keyStore;
    private CheckBox storeData;
    List<String> keyAliases;
    public static final String PREFS_NAME = "Credentials";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        keyAliases = new ArrayList<>();

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

        }
        catch(Exception e) {}

        refreshKeys();


        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network net : cm.getAllNetworks()) {
                if (cm.getNetworkInfo(net).getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.e("reseau debug", "Seting process network to " + net);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cm.bindProcessToNetwork(net);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cm.setProcessDefaultNetwork(net);
                    }
                }
            }
        }else{
            cm.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
        }




        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.connexion);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.input_password);
        storeData = (CheckBox) findViewById(R.id.saveData);


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String usernameStr = settings.getString("username", "");
        if(usernameStr != ""){ //l'utilisateur a enregistré ses ids;
            username.setText(usernameStr);
            password.setText(decryptString(keyAliases.get(0), settings.getString("password","")));
            storeData.setChecked(true);
        }

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
                final String[] arrData = {username.getText().toString(),password.getText().toString(), "240"};
                final String[] arrKey = {"n_uid","pswd","n_time"};



                new SendCredentials(arrData, arrKey, temp, "https://fw-cgcp.emse.fr/auth/plain.html").execute("");//,arrData,arrKey

                progressDialog = new ProgressDialog(MainActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Connexion...");
                progressDialog.show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Network net : cm.getAllNetworks()) {
                if (cm.getNetworkInfo(net).getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.e("reseau debug", "Seting process network to " + net);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cm.bindProcessToNetwork(net);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cm.setProcessDefaultNetwork(net);
                    }
                }
            }
        }else{
            cm.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
        }

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



        AlertDialog alert = builder.create();
        alert.show();

        button.setEnabled(true);



        //stockage des id:
        if(storeData.isChecked()){
            if(keyAliases.size() == 0){ //l'utilisateur n'a pas encore de paire de clé

                //création de nouvelles clées avec alias = username
                createNewKeys(username.getText().toString());


                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", username.getText().toString());
                editor.putString("password", encryptString(keyAliases.get(0), password.getText().toString()));

                editor.commit();


            }

        }else{ //on les efface
            username.setText("");
            password.setText("");

        }

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


    public void createNewKeys(String alias) {
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Minitel, O=Minitel Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e("Erreur create key:", Log.getStackTraceString(e));
        }
        refreshKeys();
    }

    private void refreshKeys() {
        keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        }catch (Exception e) {}


    }

    public String encryptString(String alias, String initialText) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();


            if(initialText.isEmpty()) {
                Toast.makeText(this, "Enter text in the 'Initial Text' widget", Toast.LENGTH_LONG).show();
                return "";
            }

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(initialText.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);

        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e("error ecrypt", Log.getStackTraceString(e));
            return "ERROR";
        }
    }

    public String decryptString(String alias, String cipherText) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PrivateKey privateKey = (PrivateKey) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;

        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e("error decrypt", Log.getStackTraceString(e));
            return "Error";
        }
    }



}
