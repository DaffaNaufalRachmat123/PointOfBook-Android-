package com.example.asus.taskapp.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RemoveRequest extends AsyncTask<String , Void , Integer> {
    public ProgressDialog dialog;
    public Context context;
    public Activity activity;
    public String uri;
    public String token;
    public boolean isFragment = false;
    public RemoveRequest(String uri , Context context , String token ,  boolean isFragment){
        this.context = context;
        this.uri = uri;
        this.token = token;
        this.isFragment = isFragment;
    }
    public void setActivity(Activity activity){
        this.activity = activity;
    }
    @Override
    protected void onPreExecute() {
        if(isFragment == true){
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Remove data....");
            dialog.show();
        } else {
            dialog = new ProgressDialog(context);
            dialog.setMessage("Remove data....");
            dialog.show();
        }
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        int responseCode = 0;
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.setRequestProperty("x-token",token);
            connection.setRequestMethod("DELETE");
            responseCode = connection.getResponseCode();
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return responseCode;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        dialog.dismiss();
        super.onPostExecute(integer);
    }
}
