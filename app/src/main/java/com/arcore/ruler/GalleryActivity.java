package com.arcore.ruler;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    ArrayList<CaptureImg> captureImg;
    GalleryAdapter galleryAdapter;
    RecyclerView galleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setTitle("gallery");

        galleryView = findViewById(R.id.layout_gallery);
        galleryView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        galleryView.setLayoutManager(layoutManager);

        captureImg = prepareData();
        galleryAdapter = new GalleryAdapter(getApplicationContext(), captureImg);
        galleryView.setAdapter(galleryAdapter);

//        galleryAdapter.onClickListener(this);
    }

    private ArrayList<CaptureImg> prepareData(){

        ArrayList<CaptureImg> theimage = new ArrayList<>();
        String path = Environment.getRootDirectory().toString();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++)
        {
            CaptureImg createList = new CaptureImg();
            createList.setImage_Location(file[i].getName());
            //Glide.with(this).load(createList.getImage_Location()).into(theimage.get(i).);
        }
//        for(int i = 0; i < 3; i++){
//            CaptureImg createList = new CaptureImg();
//            createList.setImage_ID(image_ids[i]);
//            theimage.add(createList);
//        }

//        File directory = new File("/data/data/Ruler"); //path is the string specifying your directory path.
//        File[] files = directory.listFiles();
//        for (int i = 0; i < files.length; i++)
//        {
//            Log.d("Files", "FileName:" + files[i].getName()); //these are the different filenames in the directory
//
////You can now use these file names along with the directory path and convert the image there to a bitmap and set it to recycler view's image view
//
//            File imgFile = new  File("/data/data/Ruler" + files[i].getName());
//            if(imgFile.exists()){
//
//            };
//        }
        return theimage;
    }

//    @Override
//    public void onClick(View v) {
//
//    }
}
