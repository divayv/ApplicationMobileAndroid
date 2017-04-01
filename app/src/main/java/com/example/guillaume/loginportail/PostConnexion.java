package com.example.guillaume.loginportail;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Guillaume on 29/03/2017.
 */

public class PostConnexion {
//    private String[] params;
//    private String[] paramKey;
    private HttpsURLConnection conn;
    private byte[] postDataBytes;
    private BufferedReader reader = null;
    private StringBuilder response = new StringBuilder();


    public PostConnexion(String[] params, String[] paramKey, String urlParam){

        try {
            URL url = new URL(urlParam);
            Map<String, String> requestParams = new LinkedHashMap<>();

            for (int i = 0; i < params.length; i++) {
                requestParams.put(paramKey[i], params[i]);
            }

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : requestParams.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            postDataBytes = postData.toString().getBytes("UTF-8");


            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);


        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public String execRequest(){
        try{
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
                return "ERROR";
            }

            conn.disconnect(); // disconnect connection
        } catch (IOException e) {
            Log.e( "Error ", e.toString());
            return "ERROR";
        }


        Log.d("urlResult", response.toString());
        return response.toString();
    }

    public HttpsURLConnection getConnexion(){
        return this.conn;
    }

}
