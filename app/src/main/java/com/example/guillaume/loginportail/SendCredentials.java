package com.example.guillaume.loginportail;

import android.os.AsyncTask;



/**
 * Created by Guillaume on 14/03/2017.
 */

public class SendCredentials extends AsyncTask<String, Void, String> {

    private Exception exception;

    private MainActivity mainActivity;
    private String[] params;
    private String[] keyParams;
    private String urlParam;

    public SendCredentials(String[] params, String [] keyParams, MainActivity mainActivity, String urlParam){
        this.params = params;
        this.keyParams = keyParams;
        this.mainActivity = mainActivity;
        this.urlParam = urlParam;
    }

    protected String doInBackground( String ...params ) {
        PostConnexion postConnexion = new PostConnexion(this.params, keyParams, urlParam);
        return postConnexion.execRequest();
    }



    protected void onPostExecute(String feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
        //feed c'est le retour de la fonction do in background
        if(feed.toString().contains("ERROR")){
            mainActivity.onLoginFail();
        }else if(feed.toString().contains("<h4 class=\"western\">Règlement d'utilisation des moyens informatiques</h4>")){
            mainActivity.acceptCond(feed);
        }else{
            mainActivity.onLoginFail();
        }
    }



}
