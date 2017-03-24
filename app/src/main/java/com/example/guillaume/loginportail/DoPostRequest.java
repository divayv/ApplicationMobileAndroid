package com.example.guillaume.loginportail;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by Guillaume on 14/03/2017.
 */

public class DoPostRequest extends AsyncTask<String, Void, String> {

    private Exception exception;
    private String[] params;
    private String[] paramKey;
    private MainActivity mainActivity;
    public DoPostRequest(String[] params, String [] keyParams, MainActivity mainActivity){
        this.params = params;
        this.paramKey = keyParams;
        this.mainActivity = mainActivity;
    }

    protected String doInBackground( String... urlParam) {

        disableSSLCertificateChecking();
        BufferedReader reader = null;

        StringBuilder response = new StringBuilder();



        try {
            URL url = new URL(urlParam[0]);
            Map<String,String> requestParams = new LinkedHashMap<>();

            for(int i = 0; i<params.length; i++){
                requestParams.put(paramKey[i],params[i]);
            }

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,String> param : requestParams.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);



            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close(); // close BufferReader
            } else  {
                Log.e("erreur http", String.valueOf(responseCode));
            }

            conn.disconnect(); // disconnect connection
        } catch (IOException e) {
            Log.e( "Error ", e.toString());
            return null;
        } catch(Exception e){
            e.printStackTrace();
        }


        Log.d("urlResult", response.toString());
        return response.toString();
    }

    protected void onPostExecute(String feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
        //feed c'est le retour de la fonction do in background
        Log.d("feed", feed);
        if(feed.toString().contains("ERROR")){

        }
    }


    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
