package com.arcore.ruler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.jar.Attributes;

public class GalleryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setTitle("Gallery");

        File[] imageFiles;
        imageFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler").listFiles();

        Gallery gallery = (Gallery) findViewById(R.id.Gallery1);
        MyGalleryAdapter galAdpater = new MyGalleryAdapter(this, imageFiles);
        gallery.setAdapter(galAdpater);

    }

    public class MyGalleryAdapter extends BaseAdapter {
        Context context;
        File[] imgFiles;

        int[] posterID = {R.drawable.ruler,R.drawable.ruler,
                R.drawable.ruler,R.drawable.ruler,
                R.drawable.ruler,R.drawable.ruler,
                R.drawable.ruler,R.drawable.ruler,
                R.drawable.ruler,R.drawable.ruler};


        public MyGalleryAdapter  (Context c, File[] Files) {
            context = c;
            imgFiles=Files;
        }

        @Override
        public int getCount() {
            return imgFiles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new Gallery.LayoutParams(100, 150));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(5,5,5,5);

            if(imgFiles[position].exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFiles[position].getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }

            final int pos = position;
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ImageView picPreview = (ImageView) findViewById(R.id.PicPreview);
                    picPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    if(imgFiles[pos].exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFiles[pos].getAbsolutePath());
                        picPreview.setImageBitmap(myBitmap);
                    }

                    picPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(),"d",Toast.LENGTH_SHORT).show();
                        }
                    });

                    return false;
                }
            });
            return imageView;
        }
    }
}
