package com.example.asus.taskapp.AccountUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FormDataPost;
import com.example.asus.taskapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    public TextInputEditText teks_email , teks_name , teks_password , teks_sekolah;
    public Spinner teks_kelamin;
    public Button btnRegister;
    public TextView login_teks , teks_parent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ArrayAdapter<String> adapters = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"Laki Laki","Perempuan"});
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teks_email = findViewById(R.id.teks_email);
        teks_name = findViewById(R.id.teks_name);
        teks_password = findViewById(R.id.teks_password);
        teks_sekolah = findViewById(R.id.teks_sekolah);
        teks_kelamin = findViewById(R.id.teks_kelamin);
        btnRegister = findViewById(R.id.register_btn);
        login_teks = findViewById(R.id.teks_login);
        teks_parent = findViewById(R.id.teks_parent);
        login_teks.setMovementMethod(LinkMovementMethod.getInstance());
        login_teks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
                dialog.setTitle("Status");
                dialog.setMessage("Please wait.....");
                dialog.show();
                RegisterRequest();
                dialog.dismiss();
            }
        });
        teks_kelamin.setAdapter(adapters);

    }
    public void RegisterRequest(){
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/blank_image.png");
        final JSONObject object = new JSONObject();
        Log.d("filepath",file.getPath());
        try {
            int id = Integer.parseInt(rand_string(8));
            object.put("id",id);
            object.put("email",teks_email.getText().toString());
            object.put("name",teks_name.getText().toString());
            object.put("password",teks_password.getText().toString());
            object.put("sekolah",teks_sekolah.getText().toString());
            object.put("kelamin",teks_kelamin.getSelectedItem().toString());
            object.put("image_path",String.valueOf(id) + file.getName());
            object.put("original_image_path",file.getName());
        } catch(JSONException e){
            e.printStackTrace();
        }
        RegisterAsync register = new RegisterAsync(new Config().getServerAddress() + "/users","upload_image",file.getPath(),"json_data",object.toString(),"POST");
        register.execute();
    }
    public class RegisterAsync extends AsyncTask<String , Void , Integer> {
        public String uri;
        public String fieldFile;
        public String fileName;
        public String fieldString;
        public String fieldData;
        public String requestType;
        public ProgressDialog dialog;
        public RegisterAsync(String uri , String fieldFile , String fileName , String fieldString , String fieldData , String requestType){
            this.uri = uri;
            this.fieldFile = fieldFile;
            this.fileName = fileName;
            this.fieldString = fieldString;
            this.fieldData = fieldData;
            this.requestType = requestType;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(RegisterActivity.this);
            dialog.setTitle("Status");
            dialog.setMessage("Please wait...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int result = new FormDataPost().UploadMultipart(uri , fileName , fieldFile , fieldString , fieldData , requestType , "x-token","","x-image","");
            return result;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(integer == 200){
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle("Status");
                builder.setMessage("Registered");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if(integer == 403) {
                dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle("Status");
                builder.setMessage("something wrong with your server");
                builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
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
}
