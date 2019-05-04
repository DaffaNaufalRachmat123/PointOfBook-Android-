package com.example.asus.taskapp.Model;

public class Users {
    public int id;
    public String email;
    public String name;
    public String password;
    public String sekolah;
    public String kelamin;
    public String image_path;
    public String original_image_path;
    public Users(int id , String email , String name , String password , String sekolah , String kelamin , String image_path , String original_image_path){
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.sekolah = sekolah;
        this.kelamin = kelamin;
        this.image_path = image_path;
        this.original_image_path = original_image_path;
    }
    public int getId(){
        return id;
    }
    public String getEmail(){
        return email;
    }
    public String getName(){
        return name;
    }
    public String getPassword(){
        return password;
    }
    public String getSekolah(){
        return sekolah;
    }
    public String getKelamin(){
        return kelamin;
    }
    public String getImagePath(){
        return image_path;
    }
    public String getOriginalImagePath(){
        return original_image_path;
    }
}
