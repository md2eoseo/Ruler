package com.arcore.ruler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ManualActivity extends AppCompatActivity {

    private int num = 0;

    private int max = 17;

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
    public void onSkipButtonClick(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    public void onNextButtonClick(View view){
        num %= 17;

        num++;

        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.VISIBLE);

        switch (num)
        {
            case 1:
                imageView2.setImageResource(imgs[0]);
                break;
            case 2:
                imageView2.setImageResource(imgs[1]);
                break;
            case 3:
                imageView2.setImageResource(imgs[2]);
                break;
            case 4:
                imageView2.setImageResource(imgs[3]);
                break;
            case 5:
                imageView2.setImageResource(imgs[4]);
                break;
            case 6:
                imageView2.setImageResource(imgs[5]);
                break;
            case 7:
                imageView2.setImageResource(imgs[6]);
                break;
            case 8:
                imageView2.setImageResource(imgs[7]);
                break;
            case 9:
                imageView2.setImageResource(imgs[8]);
                break;
            case 10:
                imageView2.setImageResource(imgs[9]);
                break;
            case 11:
                imageView2.setImageResource(imgs[10]);
                break;
            case 12:
                imageView2.setImageResource(imgs[11]);
                break;
            case 13:
                imageView2.setImageResource(imgs[12]);
                break;
            case 14:
                imageView2.setImageResource(imgs[13]);
                break;
            case 15:
                imageView2.setImageResource(imgs[14]);
                break;
            case 16:
                imageView2.setImageResource(imgs[15]);
                break;
            case 17:
                imageView2.setImageResource(imgs[16]);
                break;
            default:
                break;
        }
        Toast.makeText(getApplicationContext(), num+"/17", Toast.LENGTH_SHORT).show();
    }
    public void onPrevButtonClick(View view){
        num %= 17;

        num--;

        if(num<=0)
            num+=17;

        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.VISIBLE);

        switch (num)
        {
            case 1:
                imageView2.setImageResource(imgs[0]);
                break;
            case 2:
                imageView2.setImageResource(imgs[1]);
                break;
            case 3:
                imageView2.setImageResource(imgs[2]);
                break;
            case 4:
                imageView2.setImageResource(imgs[3]);
                break;
            case 5:
                imageView2.setImageResource(imgs[4]);
                break;
            case 6:
                imageView2.setImageResource(imgs[5]);
                break;
            case 7:
                imageView2.setImageResource(imgs[6]);
                break;
            case 8:
                imageView2.setImageResource(imgs[7]);
                break;
            case 9:
                imageView2.setImageResource(imgs[8]);
                break;
            case 10:
                imageView2.setImageResource(imgs[9]);
                break;
            case 11:
                imageView2.setImageResource(imgs[10]);
                break;
            case 12:
                imageView2.setImageResource(imgs[11]);
                break;
            case 13:
                imageView2.setImageResource(imgs[12]);
                break;
            case 14:
                imageView2.setImageResource(imgs[13]);
                break;
            case 15:
                imageView2.setImageResource(imgs[14]);
                break;
            case 16:
                imageView2.setImageResource(imgs[15]);
                break;
            case 17:
                imageView2.setImageResource(imgs[16]);
                break;
            default:
                break;
        }
        Toast.makeText(getApplicationContext(), num+"/17", Toast.LENGTH_SHORT).show();
    }
}
