package com.example.asus.taskapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FilePath {
    public static boolean isExternalStorageDocument(Uri uri){
        return "com.externalStorage.documents".equals(uri.getAuthority());
    }
    public static boolean isDownloadDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public static boolean isMediaDocument(Uri uri){
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static boolean isGooglePhotos(Uri uri){
        return "com.google.android.apps.photos".equals(uri.getAuthority());
    }
    public static String get_path_api_under_18(Context context , Uri contentUri){
        String[] projetion = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context,contentUri,projetion,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();
        String result = "";
        if(cursor != null){
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }
    public static String get_path_api_under_11(Context context , Uri contentUri){
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(contentUri , projection , null , null , null,null);
        String result = "";
        if(cursor != null){
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }
    public static String getDataColumn(Context context , Uri uri , String selection , String[] selectionArgs){
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri,projection,selection,selectionArgs,null);
            if(cursor != null && cursor.moveToFirst()){
                final int index = cursor.getColumnIndex(column);
                return cursor.getString(index);
            }
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return null;
    }
    public static String getPath(Context context , Uri contentUri){
        boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if(isKitkat && DocumentsContract.isDocumentUri(context , contentUri)){
            if(isExternalStorageDocument(contentUri)){
                final String docId = DocumentsContract.getDocumentId(contentUri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if(isDownloadDocument(contentUri)){
                final String docId = DocumentsContract.getDocumentId(contentUri);
                Uri resultUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                return getDataColumn(context , resultUri , null , null);
            } else if(isMediaDocument(contentUri)){
                final String docId = DocumentsContract.getDocumentId(contentUri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri resultsUri = null;
                if(type.equals("images")){
                    resultsUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if(type.equals("audio")){
                    resultsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else if(type.equals("video")){
                    resultsUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id = ?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context , resultsUri , selection , selectionArgs);
            }
        } else if("content".equalsIgnoreCase(contentUri.getScheme())){
            if(isGooglePhotos(contentUri)){
                return contentUri.getLastPathSegment();
            }
            return getDataColumn(context , contentUri , null , null);
        } else if("file".equalsIgnoreCase(contentUri.getScheme())){
            return contentUri.getPath();
        }
        return null;
    }
}
