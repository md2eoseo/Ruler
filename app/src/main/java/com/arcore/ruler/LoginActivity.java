package com.arcore.ruler;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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


public class LoginActivity extends AppCompatActivity {



    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final EditText idText = (EditText)findViewById(R.id.idText);
        final EditText passwordText = (EditText)findViewById(R.id.passwordText);

        Button loginbtn = (Button)findViewById(R.id.loginbtn);
        TextView registerbtn = (TextView)findViewById(R.id.registerbtn);
        registerbtn.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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




                            if(success){
                                String userID = jsonResponse.getString("userid");
                                String userPassword = jsonResponse.getString("passwd");

                                Toast.makeText(getApplicationContext(), userID + "님 환영합니다!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);

                                intent.putExtra("userID",userID);

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


}
