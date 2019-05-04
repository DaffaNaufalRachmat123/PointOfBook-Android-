package com.example.asus.taskapp.Model;

public class SoldBooks {
    public int id;
    public int user_id;
    public int book_id;
    public String book_name;
    public int count_book;
    public int total_price;
    public String status;
    public String users_sold;
    public String buyer;
    public String sold_at;
    public String image_path;
    public String original_image_path;
    public void setId(int id){
        this.id = id;
    }
    public void setUserId(int user_id){
        this.user_id = user_id;
    }
    public void setBookId(int book_id){
        this.book_id = book_id;
    }
    public void setBookName(String book_name){
        this.book_name = book_name;
    }
    public void setCountBook(int count_book){
        this.count_book = count_book;
    }
    public void setTotalPrice(int total_price){
        this.total_price = total_price;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setUsersSold(String users_sold){
        this.users_sold = users_sold;
    }
    public void setBuyer(String buyer){
        this.buyer = buyer;
    }
    public void setSoldAt(String sold_at){
        this.sold_at = sold_at;
    }
    public void setImagePath(String image_path){
        this.image_path = image_path;
    }
    public void setOriginalImagePath(String original_image_path){
        this.original_image_path = original_image_path;
    }
    public int getId(){
        return id;
    }
    public int getUserId(){
        return user_id;
    }
    public int getBookId(){
        return book_id;
    }
    public String getBookName(){
        return book_name;
    }
    public int getCountBook(){
        return count_book;
    }
    public int getTotalPrice(){
        return total_price;
    }
    public String getStatus(){
        return status;
    }
    public String getUsersSold(){
        return users_sold;
    }
    public String getBuyer(){
        return buyer;
    }
    public String getSoldAt(){
        return sold_at;
    }
    public String getImagePath(){
        return image_path;
    }
    public String getOriginalImagePath(){
        return original_image_path;
    }
}
