package com.example.mridul_xpetize.worker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskActivity extends AppCompatActivity {

    ImageView img;
    ImageButton camera;
    String path;
    TextView desc, loc;
    private static final int CAMERA_REQUEST = 1888;
    String desc_st, loc_st, start_st, end_st, taskid_st;
    Button submit;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    int pos;

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
        desc_st = i.getStringExtra("desc");
        loc_st = i.getStringExtra("loc");
        start_st = i.getStringExtra("start");
        end_st = i.getStringExtra("end");
        taskid_st = i.getStringExtra("task_id");
        pos = i.getIntExtra("pos",-1);

        //Initialise
        submit = (Button) findViewById(R.id.button_submit);
        desc = (TextView) findViewById(R.id.desc);
        loc = (TextView) findViewById(R.id.location);
        img = (ImageView) findViewById(R.id.imageView);
        camera = (ImageButton) findViewById(R.id.imageButton_camera);
        pref = new PreferencesHelper(TaskActivity.this);

        //Set values to textviews
        desc.setText(desc_st);
        loc.setText(loc_st);

        //onClick of submit buton
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new PostTask().execute();
            }
        });

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
                    i.putExtra("type", "path");
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

    private class PostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TaskActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

//            String url = "http://vikray.in/MyService.asmx/GetEmployessJSONNewN";
            String user_id = pref.GetPreferences("Designation");
            String username = pref.GetPreferences("Name");
            int status = 0;
            String stDate = start_st.replaceAll("\\s+", "");
            String endDate = end_st.replaceAll("\\s+", "");
            Log.d("Replaced", stDate);
            String url = "http://vikray.in/MyService.asmx/ExcProcedure?Para=Proc_PostTaskMst&Para=" + taskid_st + "&Para=" + user_id + "&Para=" + status + "&Para=" + username;
            // Making a request to url and getting response

            Log.d("Test", url);

            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            Log.d("Response: ", "> " + jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Intent i = new Intent(TaskActivity.this, MainActivity.class);
            i.putExtra("pos",pos);
            startActivity(i);
        }
    }

}
