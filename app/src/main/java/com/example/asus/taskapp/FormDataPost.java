package com.example.asus.taskapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FormDataPost {
    public int UploadMultipart(String uri , String fileName , String fieldName , String fieldString,  String data , String requestType , String headerToken , String token , String headerReason , String reasonValue){
        HttpURLConnection connection;
        DataOutputStream outputStream;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String two = "--";
        int maxBufferSize = 2 * 2048 * 2048;
        int bytesRead , bytesAvailable , bufferSize;
        byte[] buffer;
        File selectedFile = new File(fileName);
        int responseCode = 0;
        if(!selectedFile.exists()){
            Log.e("Error","Not A File");
            return 0;
        } else {
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                URL url = new URL(uri);
                connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod(requestType);
                connection.setRequestProperty(headerToken , token);
                connection.setRequestProperty(headerReason , reasonValue);
                connection.setRequestProperty("Connection","Keep-Alive");
                connection.setRequestProperty("ENCTYPE","multipart/form-data");
                connection.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty(fieldName , fileName);
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldString + "\"" + lineEnd + lineEnd);
                outputStream.writeBytes(data + lineEnd + two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable , maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fis.read(buffer , 0 , bufferSize);
                while(bytesRead > 0){
                    outputStream.write(buffer , 0 , bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable , maxBufferSize);
                    bytesRead = fis.read(buffer , 0 ,bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(two + boundary + two + lineEnd);
                fis.close();
                outputStream.flush();
                outputStream.close();
                responseCode = connection.getResponseCode();
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine()) != null){
                    sb.append(line);
                }
                Log.d("response" , sb.toString());
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return responseCode;
        }
    }
}
