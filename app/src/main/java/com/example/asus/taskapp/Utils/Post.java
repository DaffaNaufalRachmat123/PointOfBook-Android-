package com.example.asus.taskapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.asus.taskapp.Activities.EditUser;
import com.example.asus.taskapp.MainActivity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Post extends AsyncTask<String , Void , Integer> {
    public String uri;
    public String fileName;
    public String fileField;
    public String fieldString;
    public String data;
    public String requestType;
    public ProgressDialog dialog;
    public Context context;
    public String token;
    public Class destinationClass;
    public Post(String uri, String fileName, String fileField, String fieldString, String data, String requestType, Context context, String token) {
        this.uri = uri;
        this.fileName = fileName;
        this.fileField = fileField;
        this.fieldString = fieldString;
        this.data = data;
        this.requestType = requestType;
        this.context = context;
        this.token = token;
    }
    public void setDestinationClass(Class destinationClass){
        this.destinationClass = destinationClass;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Updating...");
        dialog.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        return UploadMultipart(uri, fileName, fileField, fieldString, data, requestType);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        dialog.dismiss();
        context.startActivity(new Intent(context,destinationClass));
        super.onPostExecute(integer);
    }

    public int UploadMultipart(String uri, String fileName, String fieldName, String fieldString, String data, String requestType) {
        HttpURLConnection connection;
        DataOutputStream outputStream;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String two = "--";
        int maxBufferSize = 2 * 2048 * 2048;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        File selectedFile = new File(fileName);
        int responseCode = 0;
        if (!selectedFile.exists()) {
            Log.e("Error", "Not A File");
            return 0;
        } else {
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod(requestType);
                connection.setRequestProperty("x-token", token);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty(fieldName, fileName);
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldString + "\"" + lineEnd + lineEnd);
                outputStream.writeBytes(data + lineEnd + two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fis.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(two + boundary + two + lineEnd);
                fis.close();
                outputStream.flush();
                outputStream.close();
                responseCode = connection.getResponseCode();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseCode;
        }
    }
}