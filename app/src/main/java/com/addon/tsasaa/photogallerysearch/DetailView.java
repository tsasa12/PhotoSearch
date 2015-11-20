package com.addon.tsasaa.photogallerysearch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class DetailView extends AppCompatActivity {

    private String url;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        mImageView = (ImageView) findViewById(R.id.detailImageID);

        //url = savedInstanceState.getString(PhotoGalleryFragment.DETAIL_VIEW_ID);
        Intent intent = getIntent();
        url = intent.getStringExtra(PhotoGalleryFragment.DETAIL_VIEW_ID);

        //Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
        Glide.with(this)
                .load(url)
                //.placeholder(R.drawable.nice)
                .into(mImageView);
    }
}
