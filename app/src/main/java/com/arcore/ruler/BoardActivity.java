package com.arcore.ruler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class BoardActivity extends Activity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        setTitle("모델 다운로드");

        String imgURL = "http://13.125.224.69/tpicture/";
        final String[] myRemoteImages = {
                imgURL+"bed.jpg",
                imgURL+"chair.jpg",
                imgURL+"square.jpg",
                imgURL+"table.jpg"
        };
        final Bitmap[] bm = new Bitmap[myRemoteImages.length];

        final Handler handler = new Handler();
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                for(int i=0;i<myRemoteImages.length;i++) {
                    try {
                        URL aURL = new URL(myRemoteImages[i]);
                        HttpURLConnection conn = (HttpURLConnection)aURL.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        final BufferedInputStream bis = new BufferedInputStream(is);
                        bm[i] = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                    } catch (IOException e) {
                        Log.e("DEBUGTAG", "Remtoe Image Exception", e);
                    }

                }
            }
        });
        mThread.start();

        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GridView grid = (GridView) findViewById(R.id.grid);
        MyGridAdapter gridAdpater = new MyGridAdapter(this, bm, myRemoteImages);
        grid.setAdapter(gridAdpater);
    }


    public class MyGridAdapter extends BaseAdapter {
        Context context;
        Bitmap[] tumbFiles;
        String[] link;

        public MyGridAdapter  (Context c, Bitmap[] bm, String[] lk) {
            context = c;
            tumbFiles = bm;
            link = lk;
        }

        @Override
        public int getCount() {
            return tumbFiles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new Gallery.LayoutParams(400, 800));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(2,2,2,2);

            if(position<4) {
                imageView.setImageBitmap(tumbFiles[position]);
            }

            final int pos = position;

            imageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), link[pos].substring(30, link[pos].length()-4) + "를 다운로드 받았습니다.",Toast.LENGTH_SHORT).show();
                    downLoadResource(link[pos].replace("tpicture","obj").substring(0,25), Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler/obj/", link[pos].substring(30, link[pos].length()-4)+".jpg");
                    downLoadResource(link[pos].replace("tpicture","obj").substring(0,25), Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler/obj/", link[pos].substring(30, link[pos].length()-4)+".obj");
                }
            });

            return imageView;
        }
    }

    void downLoadResource(String servUrl, String filedirectory, String fileList) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(servUrl + fileList);
            File file = new File(filedirectory + fileList);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[50];
            int current = 0;

            while ((current = bis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer.toByteArray());
            fos.close();

        } catch (IOException e) {
            Log.e("download", e.getMessage());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}

