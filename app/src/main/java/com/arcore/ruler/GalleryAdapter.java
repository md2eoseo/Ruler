package com.arcore.ruler;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryVIewHolder> {
    List<GalleryItem> items = new ArrayList<>();
    public void add(GalleryItem data){
        items.add(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryVIewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_gallery, viewGroup, false);

        return new GalleryVIewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryVIewHolder galleryVIewHolder, int i) {
       GalleryItem item = items.get(i);
       Uri uri = Uri.parse(item.getUri());
       galleryVIewHolder.img.setImageURI(uri);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
