package com.arcore.ruler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ManualActivity extends AppCompatActivity {

    private int num = 0;

    ImageView imageView1, imageView2, imageView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        setTitle("메뉴얼");
        imageView1 = (ImageView)findViewById(R.id.changeImage1);
        imageView2 = (ImageView)findViewById(R.id.changeImage2);
        imageView3 = (ImageView)findViewById(R.id.changeImage3);
    }
    public void onSkipButtonClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    public void onNextButtonClick(View view){
        num %= 3;
        if(num == 0)
        {
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.GONE);
            imageView3.setVisibility(View.GONE);
        }
        if(num == 1)
        {
            imageView1.setVisibility(View.GONE);
            imageView2.setVisibility(View.VISIBLE);
            imageView3.setVisibility(View.GONE);
        }
        if(num == 2)
        {
            imageView1.setVisibility(View.GONE);
            imageView2.setVisibility(View.GONE);
            imageView3.setVisibility(View.VISIBLE);
        }
        num++;
    }
}
