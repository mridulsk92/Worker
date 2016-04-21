package com.example.mridul_xpetize.worker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import java.io.File;

public class FullImageActivity extends AppCompatActivity {

    ImageView full_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        full_img = (ImageView) findViewById(R.id.imageView_full);
        Intent i = getIntent();

        String path = i.getStringExtra("path");
        File imgFile = new File(path);
        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            full_img.setImageBitmap(myBitmap);

        }
    }
}