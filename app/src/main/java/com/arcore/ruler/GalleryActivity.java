package com.arcore.ruler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

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

        public MyGalleryAdapter  (Context c, File[] Files) {
            context = c;
            imgFiles = Files;
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

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater galleryInflater = getMenuInflater();
        galleryInflater.inflate(R.menu.gallery_option, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.share:
//                Toast.makeText(getApplicationContext(), "share", Toast.LENGTH_LONG).show();
//                File dirName = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler");  //디렉토리를 지정합니다.
//                String fileName = "Ruler_Capture_20190603134441.jpeg"; //공유할 이미지 파일 명
//                File file = new File(dirName, fileName); //image 파일의 경로를 설정합니다.
////                Log.d("aa", "파일경로는? " + file);
////                Uri mSaveImageUri = Uri.fromFile(file); //file의 경로를 uri로 변경합니다.
//                Uri uri = null;
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                    uri = FileProvider.getUriForFile(GalleryActivity.this, "com.test.fileprovider", file);
//                }else{
//                    uri = Uri.fromFile(file);
//                }
////                Log.d("aa", "유알아이경로는?" + uri);
//
//                Intent intent = new Intent(Intent.ACTION_SEND); //전송 메소드를 호출합니다. Intent.ACTION_SEND
//                intent.setType("image/jpeg"); //jpg 이미지를 공유 하기 위해 Type을 정의합니다.
//                intent.putExtra(Intent.EXTRA_STREAM, uri); //사진의 Uri를 가지고 옵니다.
//                startActivity(intent); //Activity를 이용하여 호출 합니다.
                return true;
        }
        return false;
    }
}
