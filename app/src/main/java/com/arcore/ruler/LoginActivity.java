package com.arcore.ruler;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class LoginActivity extends AppCompatActivity {



    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final String filedirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Ruler/obj/";
        final String  servUrl = "http://13.125.224.69/obj/";
        final String [] fileList= {"bed.jpg", "bed.obj", "chair.jpg", "chair.obj", "square.jpg", "square.obj", "table.jpg", "table.obj"};

        final EditText idText = (EditText)findViewById(R.id.idText);
        final EditText passwordText = (EditText)findViewById(R.id.passwordText);

        Button loginbtn = (Button)findViewById(R.id.loginbtn);
        TextView registerbtn = (TextView)findViewById(R.id.registerbtn);
        registerbtn.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //다운로드 테스트



                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                final String userID = idText.getText().toString();
                final String userPassword = passwordText.getText().toString();


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            boolean success = jsonResponse.getBoolean("success");


                            Toast.makeText(getApplicationContext(), "success" + success, Toast.LENGTH_SHORT).show();

                            if(success){
                                String userID = jsonResponse.getString("userid");
                                String userPassword = jsonResponse.getString("passwd");


                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);

                                intent.putExtra("userID",userID);




                                for(int i = 0 ; i < fileList.length ; i++)
                                    downLoadResource(servUrl, filedirectory, fileList[i]);
                                LoginActivity.this.startActivity(intent);
                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

                                builder.setMessage("Login failed").setNegativeButton("retry",null).create().show();;
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }

                    }
                };

                Response.ErrorListener errlistener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("에러 => " + error.getMessage());
                    }
                };

                LoginRequest loginRequest = new LoginRequest(userID, userPassword, responseListener, errlistener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);


            }

        });
    }

    void downLoadResource (String servUrl, String filedirectory, String fileList){
        try
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
                URL url = new URL(servUrl + fileList);
                File file = new File( filedirectory+fileList);
                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[50];
                int current = 0;

                while((current = bis.read(data,0,data.length)) != -1){
                    buffer.write(data,0,current);
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer.toByteArray());
                fos.close();

        }
        catch (IOException e)
        {
            Log.e("download", e.getMessage());
        }

    }
}
