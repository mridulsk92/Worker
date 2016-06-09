package com.example.mridul_xpetize.worker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static String TAG_DESCRIPTION = "SubTask";
    private static String TAG_ID = "Id";
    int pos;
    ListView subtask_list;
    JSONArray tasks;
    ArrayList<HashMap<String, Object>> dataList;
    LayoutInflater inflater;
    CustomAdapter cardAdapter;

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
        pos = i.getIntExtra("pos", -1);

        //Initialise
        dataList = new ArrayList<HashMap<String, Object>>();
        subtask_list = (ListView) findViewById(R.id.listView_sub);
        submit = (Button) findViewById(R.id.button_submit);
        desc = (TextView) findViewById(R.id.desc);
        loc = (TextView) findViewById(R.id.location);
        img = (ImageView) findViewById(R.id.imageView);
        camera = (ImageButton) findViewById(R.id.imageButton_camera);
        pref = new PreferencesHelper(TaskActivity.this);

        //onClick of submit button
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

        //Image onClick
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

        //Load SubTask
        new LoadSubTask().execute();
    }

    private class LoadSubTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TaskActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            ServiceHandler sh = new ServiceHandler();

            String user_id = pref.GetPreferences("User Id");
            String url = getString(R.string.url) + "MyService.asmx/ExcProcedure?Para=Proc_GetSubTsk&Para=" + taskid_st;
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {

                try {

                    tasks = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject c = tasks.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String desc = c.getString(TAG_DESCRIPTION);

                        //tmp hashmap for single contact
                        HashMap<String, Object> contact = new HashMap<String, Object>();

                        //adding each child node to HashMap key => value
                        contact.put(TAG_DESCRIPTION, "Description : " + desc);
                        contact.put(TAG_ID, id);
                        dataList.add(contact);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (pDialog.isShowing())
                pDialog.dismiss();

//            swipe.setRefreshing(false);
            cardAdapter = new CustomAdapter(TaskActivity.this, R.layout.task_list, dataList);
            subtask_list.setAdapter(cardAdapter);
        }
    }

    //Define Custom Adapter for Message Cards
    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            TextView id,desc;
            CardView cv;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.task_list, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.id = (TextView) convertView.findViewById(R.id.id);
                viewHolder.desc = (TextView) convertView.findViewById(R.id.desc);
                viewHolder.cv = (CardView) convertView.findViewById(R.id.card_task);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            viewHolder.desc.setText(dataList.get(position).get("Description").toString());
            viewHolder.id.setText(dataList.get(position).get("Id").toString());
            return convertView;
        }
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

            //Making a request to url and getting response
            String url = getString(R.string.url) + "MyService.asmx/ExcProcedure?Para=Proc_PostTaskMst&Para=" + taskid_st + "&Para=" + user_id + "&Para=" + status + "&Para=" + username;

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
            i.putExtra("pos", pos);
            startActivity(i);
        }
    }
}
