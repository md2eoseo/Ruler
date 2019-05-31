package com.arcore.ruler;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private ArrayList<CaptureImg> galleryList;
    private Context context;

    public GalleryAdapter(Context context, ArrayList<CaptureImg> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_gallery_cell, viewGroup, false);
        return new ViewHolder(view);
    }

    private void prepareData(GalleryAdapter.ViewHolder viewHolder){
        ArrayList<CaptureImg> theimage = new ArrayList<>();
        String path = Environment.getRootDirectory().toString();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++)
        {
            CaptureImg createList = new CaptureImg();
            createList.setImage_Location(file[i].getName());
            //Glide.with().load(createList.getImage_Location()).into(theimage.get(i).);
        }
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder viewHolder, int i) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.img.setImageResource((galleryList.get(i).getImage_ID()));
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;

        public ViewHolder(View view) {
            super(view);

            img = (ImageView) view.findViewById(R.id.img);
        }
    }
}