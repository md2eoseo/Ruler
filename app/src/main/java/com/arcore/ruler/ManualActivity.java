package com.arcore.ruler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ManualActivity extends AppCompatActivity {

    private int num = 0;

    private int max = 21;

    int[] imgs = new int[max];

    ImageView imageView, imageView2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        setTitle("메뉴얼");

        imageView = (ImageView)findViewById(R.id.changeImage);
        imageView2 = (ImageView)findViewById(R.id.changeImage2);

        for(int i = 0;i<max;i++)
        {
            imgs[i] = getApplicationContext().getResources().getIdentifier("img"+i, "drawable", getPackageName());
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater mainInflater = getMenuInflater();
        mainInflater.inflate(R.menu.manual_option, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.VISIBLE);
        switch (item.getItemId()){
            case R.id.menu1:
                imageView2.setImageResource(imgs[1]);
                num = 2;
                return true;
            case R.id.menu2:
                imageView2.setImageResource(imgs[6]);
                num = 7;
                return true;
            case R.id.menu3:
                imageView2.setImageResource(imgs[11]);
                num = 12;
                return true;
            case R.id.menu4:
                imageView2.setImageResource(imgs[17]);
                num = 18;
                return true;
        }
        return false;
    }

    public void onSkipButtonClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    public void onNextButtonClick(View view){
        num %= 21;

        num++;

        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.VISIBLE);
        imageView2.setImageResource(imgs[num-1]);
        Toast.makeText(getApplicationContext(), num+"/21", Toast.LENGTH_SHORT).show();
    }
    public void onPrevButtonClick(View view){
        num %= 21;

        num--;

        if(num<=0)
            num+=21;

        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.VISIBLE);
        imageView2.setImageResource(imgs[num-1]);

        Toast.makeText(getApplicationContext(), num+"/21", Toast.LENGTH_SHORT).show();
    }
}
