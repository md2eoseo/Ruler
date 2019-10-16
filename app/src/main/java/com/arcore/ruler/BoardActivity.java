package com.arcore.ruler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BoardActivity extends Activity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        setTitle("사물 다운로드 받기");

        final GridView gv= (GridView)findViewById(R.id.grid);
        MyGridAdapter gAdpater = new MyGridAdapter(this);
        gv.setAdapter(gAdpater);
    }

    public class MyGridAdapter extends BaseAdapter{
        Context context;
        public MyGridAdapter(Context c){
            context=c;
        }
        public int getCount(){
            return 0;
        }
        public Object getItem(int arg0){
            return null;
        }
        public long getItemId(int arg0){
            return 0;
        }
        public View getView(int arg0, View arg1, ViewGroup arg2){
            ImageView imageview = new ImageView(context);
            imageview.setLayoutParams(new GridView.LayoutParams(200,30));
            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageview.setPadding(5,5,5,5);

            //imageview.setImageResource();

            final int pos=0;
            imageview.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){

                }
            });
            return null;
        }
    }
}