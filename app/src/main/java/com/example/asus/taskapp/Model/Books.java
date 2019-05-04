package com.example.asus.taskapp.Model;

public class Books {
    public int id;
    public int user_id;
    public String book_name;
    public int count_book;
    public int price_book;
    public String status;
    public String image_path;
    public String original_image_path;
    public String created_at;
    public void setId(int id){
        this.id = id;
    }
    public void setUserId(int user_id){
        this.user_id = user_id;
    }
    public void setBookName(String book_name){
        this.book_name = book_name;
    }
    public void setCountBook(int count_book){
        this.count_book = count_book;
    }
    public void setPriceBook(int price_book){
        this.price_book = price_book;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setImagePath(String image_path){
        this.image_path = image_path;
    }
    public void setOriginalImagePath(String original_image_path){
        this.original_image_path = original_image_path;
    }
    public void setCreatedAt(String created_at){
        this.created_at = created_at;
    }
    public int getId(){return id;}
    public int getUserId(){
        return user_id;
    }
    public String getBookName(){
        return book_name;
    }
    public int getCountBook(){
        return count_book;
    }
    public int getPriceBook(){
        return price_book;
    }
    public String getStatus(){
        return status;
    }
    public String getImagePath(){
        return image_path;
    }
    public String getOriginalImagePath(){
        return original_image_path;
    }
    public String getCreatedAt(){
        return created_at;
    }
}
