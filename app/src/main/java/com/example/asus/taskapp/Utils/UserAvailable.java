package com.example.asus.taskapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.asus.taskapp.AccountUtils.LoginActivity;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.Model.Users;

import java.util.List;

public class UserAvailable {
    public Context context;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public FetchData fetchData;
    public boolean isNull = false;
    public UserAvailable(Context context){
        this.context = context;
    }
    public boolean check_user_exist(){
        preferences = context.getSharedPreferences("user_data",0);
        String token = preferences.getString("token",null);
        if(token != null){
            int id = preferences.getInt("id",0);
            if(id != 0){
                fetchData = new FetchData(context , new Config().getServerAddress() + "/users/" + String.valueOf(id), FetchData.FetchType.USERS_FIND_BY_ID,token,false);
                fetchData.setUsersListener(new FetchData.OnUsersListener() {
                    @Override
                    public void onUsersData(List<Users> usersList) {
                        if(usersList == null){
                            isNull = true;
                        } else {
                            isNull = false;
                            Log.d("data",usersList.get(0).getEmail());
                        }
                    }
                });
                fetchData.execute();
            } else {
                isNull = true;
            }
        } else {
            isNull = true;
        }
        return isNull;
    }
}
