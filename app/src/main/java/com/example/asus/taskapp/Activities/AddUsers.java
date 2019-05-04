package com.example.asus.taskapp.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asus.taskapp.AccountUtils.LoginActivity;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FilePath;
import com.example.asus.taskapp.FormDataPost;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.UserAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

public class AddUsers extends AppCompatActivity {
    public TextInputEditText email_users , name_users , password_users , sekolah_users;
    public Spinner kelamin_users;
    public Button btnSelect , btnAddUsers;
    public ImageView imageUsers;
    public String sourcePath = null;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);
        if(new UserAvailable(AddUsers.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddUsers.this);
            builder.setMessage("Akun Anda Telah Dihapus / Ada Kesalahan Aplikasi \n Anda Akan Logout Otomatis");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    preferences = getSharedPreferences("user_data",0);
                    editor = preferences.edit();
                    editor.clear();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        init();
        ArrayAdapter<String> adapters = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item , new String[]{"Laki Laki","Perempuan"});
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelamin_users.setAdapter(adapters);
        Toolbar toolbar = findViewById(R.id.toolbar_add_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(AddUsers.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"),10);
            }
        });
        btnAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sourcePath == null){
                    Toast.makeText(AddUsers.this,"Wajib Pake Gambar",Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject object = new JSONObject();
                try {
                    int id = Integer.parseInt(rand_string(8));
                    object.put("id",id);
                    object.put("email",email_users.getText().toString());
                    object.put("name",name_users.getText().toString());
                    object.put("password",password_users.getText().toString());
                    object.put("sekolah",sekolah_users.getText().toString());
                    object.put("kelamin",kelamin_users.getSelectedItem().toString());
                    object.put("image_path" , String.valueOf(id) + new File(sourcePath).getName());
                    object.put("original_image_path",new File(sourcePath).getName());
                } catch (JSONException e){
                    e.printStackTrace();
                }
                new AddAsync(new Config().getServerAddress() + "/users",sourcePath , "upload_image","json_data",object.toString(),"POST",
                        "x-token","","x-reason","").execute();
            }
        });
    }
    public class AddAsync extends AsyncTask<String , Void , Integer> {
        public String uri;
        public String fileName;
        public String fieldName;
        public String fieldString;
        public String data;
        public String requestType;
        public String headerToken;
        public String token;
        public String headerReason;
        public String reasonValue;
        public ProgressDialog dialog;
        public AddAsync(String uri , String fileName , String fieldName , String fieldString ,
                        String data , String requestType , String headerToken , String token , String headerReason,String reasonValue){
            this.uri = uri;
            this.fileName = fileName;
            this.fieldName = fieldName;
            this.fieldString = fieldString;
            this.data = data;
            this.requestType = requestType;
            this.headerToken = headerToken;
            this.token = token;
            this.headerReason = headerReason;
            this.reasonValue = reasonValue;
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AddUsers.this);
            dialog.setMessage("Adding data....");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            FormDataPost post = new FormDataPost();
            return post.UploadMultipart(uri , fileName , fieldName , fieldString , data , requestType ,
                    headerToken , token , headerReason , reasonValue);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            dialog.dismiss();
            Intent intent = new Intent(AddUsers.this,MainActivity.class);
            startActivity(intent);
            finish();
            super.onPostExecute(integer);
        }
    }
    public String rand_string(int length){
        char[] chars = {'0','1','2','3','4','5','6','7','8','9'};
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        while(0 < length--){
            sb.append(chars[random.nextInt(chars.length)]);
        }
        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 10){
            if(resultCode == RESULT_OK && data != null){
                sourcePath = FilePath.getPath(AddUsers.this,data.getData());
                Glide.with(this)
                        .load(sourcePath)
                        .into(imageUsers);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init(){
        email_users = findViewById(R.id.email_users);
        name_users = findViewById(R.id.name_users);
        password_users = findViewById(R.id.password_users);
        sekolah_users = findViewById(R.id.sekolah_users);
        kelamin_users = findViewById(R.id.kelamin_users);
        btnSelect = findViewById(R.id.btnSelect);
        btnAddUsers = findViewById(R.id.btnAddUser);
        imageUsers = findViewById(R.id.image_users);
    }
}
