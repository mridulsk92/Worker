package com.example.mridul_xpetize.worker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskActivity extends AppCompatActivity {

    ImageView img;
    ImageButton camera;
    String path;
    TextView desc, loc;
    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Worker");
        toolbar.setTitleTextColor(Color.WHITE);

        //get Intent
        Intent i = getIntent();
        String desc_st = i.getStringExtra("desc");
        String loc_st = i.getStringExtra("loc");

        //Initialise
        desc = (TextView)findViewById(R.id.desc);
        loc = (TextView)findViewById(R.id.location);
        img = (ImageView)findViewById(R.id.imageView);
        camera = (ImageButton)findViewById(R.id.imageButton_camera);

        //Set values to textviews
        desc.setText(desc_st);
        loc.setText(loc_st);

        //onClick of Camera
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (path != null) {

                    Intent i = new Intent(TaskActivity.this, FullImageActivity.class);
                    i.putExtra("path", path);
                    i.putExtra("type","path");
                    startActivity(i);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(photo);
            path = getOriginalImagePath();
        }
    }

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = TaskActivity.this.managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }
}
