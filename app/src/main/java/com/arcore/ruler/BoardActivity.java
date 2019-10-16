package com.arcore.ruler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

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

public class BoardActivity extends Activity {
    String[] fileList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        setTitle("사물 다운로드 받기");

        File dir_tumb = new File(Environment.getExternalStorageDirectory() + File.separator + "Ruler/tumb");
        dir_tumb.mkdirs();

        //파일리스트 불러오기
        final String filedirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ruler/obj/";
        final String servUrl = "http://13.125.224.69/obj/";
        final String tpicUrl = "http://13.125.224.69/obj/tpicture";
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    int fnum = jsonResponse.getInt("fNum");
                    for (int i = 0; i < fnum; i++) {
                        String index = Integer.toString(i);
                        fileList[i] = jsonResponse.getString(index);

                    }
                } catch (JSONException e) {
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
        ListRequest listRequest = new ListRequest(responseListener, errlistener);
        RequestQueue queue = Volley.newRequestQueue(BoardActivity.this);
        queue.add(listRequest);
    }

    public class MyGridAdapter extends BaseAdapter {
        Context context;
        File[] tumbFiles;

        public MyGridAdapter  (Context c, File[] Files) {
            context = c;
            tumbFiles = Files;
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
            imageView.setLayoutParams(new Gallery.LayoutParams(200, 300));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(5,5,5,5);

            if(tumbFiles[position].exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(tumbFiles[position].getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }

            final int pos = position;
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

        File[] tumbFiles;
        tumbFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler/tumb/").listFiles();

        GridView grid = (GridView) findViewById(R.id.grid);
        MyGridAdapter gridAdpater = new MyGridAdapter(this, tumbFiles);
        grid.setAdapter(gridAdpater);

    }
}

