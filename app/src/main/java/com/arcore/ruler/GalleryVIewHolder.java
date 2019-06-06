package com.arcore.ruler;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

public class GalleryVIewHolder extends RecyclerView.ViewHolder {

    public ImageView img;

    public GalleryVIewHolder(@NonNull View itemView) {
        super(itemView);
        img = (ImageView) itemView.findViewById(R.id.img);
    }
}
