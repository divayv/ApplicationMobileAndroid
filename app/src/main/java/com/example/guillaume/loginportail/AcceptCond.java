package com.example.guillaume.loginportail;

import android.os.AsyncTask;

/**
 * Created by Guillaume on 29/03/2017.
 */

public class AcceptCond extends AsyncTask<String, Void, String> {

    private MainActivity mainActivity;
    private String[] params;
    private String[] keyParams;
    private String urlParam;

    public AcceptCond(MainActivity mainActivity, String[] dataParam, String[] keyParam, String urlParam){
        this.mainActivity = mainActivity;
        this.params = dataParam;
        this.keyParams = keyParam;
        this.urlParam = urlParam;
    }

    @Override
    protected String doInBackground(String... params) {
        PostConnexion postConnexion = new PostConnexion(this.params, keyParams, urlParam);
        return postConnexion.execRequest();
    }

    protected void onPostExecute(String feed) {

        this.mainActivity.onLoginSuccess();
    }
}
