package com.example.asus.taskapp.Utils;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSON_POST extends AsyncTask<String , String , Integer> {
    public String uri;
    public String json_objects;
    public OnResponseCode responseListener;
    public String method;
    public String token;
    public JSON_POST(String uri , String json_objects){
        this.uri = uri;
        this.json_objects = json_objects;
    }
    public void setToken(String token){
        this.token = token;
    }
    public void setRequestMethod(String method){
        this.method = method;
    }
    public void setResponseListener(OnResponseCode responseListener){
        this.responseListener = responseListener;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        HttpURLConnection connection;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("x-token",token);
            connection.setRequestProperty("x-image","no image");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(json_objects);
            InputStreamReader inStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(inStreamReader);
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = reader.readLine()) != null){
                sb.append(line);
            }
            responseListener.serverResponseCode(sb.toString() , connection.getResponseCode());
            writer.flush();
            writer.close();
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    public interface OnResponseCode {
        void serverResponseCode(String responseMessage , int code);
    }
}
