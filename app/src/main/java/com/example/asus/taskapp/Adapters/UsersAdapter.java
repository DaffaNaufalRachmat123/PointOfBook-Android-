package com.example.asus.taskapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.Model.Users;
import com.example.asus.taskapp.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewAdapter> {
    public List<Users> usersList;
    public Context context;
    public UsersClickListener listener;
    public UsersAdapter(List<Users> usersList , Context context , UsersClickListener listener){
        this.usersList = usersList;
        this.context = context;
        this.listener = listener;
    }
    public class MyViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView cardUsers;
        public ImageView imageUsers;
        public TextView users_email , users_name , users_sekolah;
        public MyViewAdapter(View itemView){
            super(itemView);
            cardUsers = itemView.findViewById(R.id.card_users);
            imageUsers = itemView.findViewById(R.id.imageUsers);
            users_email = itemView.findViewById(R.id.users_email);
            users_name = itemView.findViewById(R.id.users_name);
            users_sekolah = itemView.findViewById(R.id.users_sekolah);
            cardUsers.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onUserClicked(usersList.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public MyViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users , parent , false);
        return new MyViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewAdapter holder, int position) {
        Users users = usersList.get(position);
        holder.users_email.setText(users.getEmail());
        holder.users_name.setText("Nama : " + users.getName());
        holder.users_sekolah.setText("Sekolah : " + users.getSekolah());
        Glide.with(context)
                .load(new Config().getServerAddress() + "/" + users.getImagePath())
                .priority(Priority.HIGH)
                .fitCenter()
                .placeholder(R.mipmap.ic_persons)
                .error(R.mipmap.ic_persons)
                .into(holder.imageUsers);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
    public interface UsersClickListener {
        void onUserClicked(Users users);
    }
}
