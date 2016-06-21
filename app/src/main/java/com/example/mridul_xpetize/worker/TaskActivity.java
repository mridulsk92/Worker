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
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    TextView comments_text, dsc_text, priority_txt, desc;
    String desc_st, loc_st, start_st, end_st, taskid_st, status_st, comments_st, priority_st, sub_id_st, userId_st, createdBy_st;
    Button submit;
    ProgressDialog pDialog;
    PreferencesHelper pref;

    private static String TAG_DESCRIPTION = "SubTask";
    private static String TAG_ID = "Id";
    int pos, response_json;

    private Drawer result = null;
    ListView subtask_list;
    JSONArray tasks;

    ArrayList<HashMap<String, Object>> dataList;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.logo_ic);

        //Get preference values
        pref = new PreferencesHelper(TaskActivity.this);
        String name = pref.GetPreferences("UserName");

        //Add header to navigation drawer
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(name).withEmail(name + "@gmail.com").withIcon(getResources().getDrawable(R.drawable.profile))
                ).build();

        //Drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withTranslucentStatusBar(false)
                .withDisplayBelowStatusBar(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("About").withIcon(getResources().getDrawable(R.drawable.ic_about)).withIdentifier(1).withSelectable(false),
                        new SecondaryDrawerItem().withName("Log Out").withIcon(getResources().getDrawable(R.drawable.ic_logout)).withIdentifier(2).withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 1) {

                                //Clicked About

                            } else if (drawerItem.getIdentifier() == 2) {

                                //Clicked LogOut

                            }
                        }
                        return false;
                    }
                })
                .build();

        //ToggleButton on ToolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        //Get Intent
        Intent i = getIntent();
        pref = new PreferencesHelper(TaskActivity.this);
        userId_st = pref.GetPreferences("UserID");
        status_st = i.getStringExtra("statusId");
        comments_st = i.getStringExtra("comments");
        priority_st = i.getStringExtra("priority");
        sub_id_st = i.getStringExtra("subTaskId");
        desc_st = i.getStringExtra("desc");
        loc_st = i.getStringExtra("loc");
        start_st = i.getStringExtra("start");
        end_st = i.getStringExtra("end");
        taskid_st = i.getStringExtra("task_id");
        createdBy_st = i.getStringExtra("createdBy");
        pos = i.getIntExtra("pos", -1);

        //Initialise
        comments_text = (TextView) findViewById(R.id.comments);
        priority_txt = (TextView) findViewById(R.id.priority);
        dsc_text = (TextView) findViewById(R.id.SubDesc);
        dataList = new ArrayList<HashMap<String, Object>>();
        subtask_list = (ListView) findViewById(R.id.listView_sub);
        submit = (Button) findViewById(R.id.button_submit);
        desc = (TextView) findViewById(R.id.desc);
        pref = new PreferencesHelper(TaskActivity.this);

        comments_text.setText(comments_st);
        dsc_text.setText(desc_st);

        //onClick of submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                new PostTask().execute();
            }
        });
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

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/?????");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            // Build JSON string
            JSONStringer userJson = null;
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("taskDetails")
                        .object()
                        .key("TaskId").value(taskid_st)
                        .key("AssignedTo").value(userId_st)
                        .key("AssignedBy").value(createdBy_st)
                        .key("StatusId").value(status_st)
                        .key("IsSubTask").value("True")
                        .key("Comments").value(comments_st)
                        .key("CreatedBy").value(createdBy_st)
                        .endObject()
                        .endObject();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("Json", String.valueOf(userJson));
            StringEntity entity = null;
            try {
                entity = new StringEntity(userJson.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            entity.setContentType("application/json");

            request.setEntity(entity);

            // Send request to WCF service
            DefaultHttpClient httpClient = new DefaultHttpClient();
            try {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpClient.execute(request, responseHandler);

                Log.d("res", response);

                if (response != null) {

                    try {

                        //Get Data from Json
                        JSONObject jsonObject = new JSONObject(response);

                        String message = jsonObject.getString("Message");

                        //Save userid and username if success
                        if (message.equals("Success")) {
                            response_json = 200;
                        } else {
                            response_json = 201;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (response_json == 200) {
                Toast.makeText(TaskActivity.this, "Success", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskActivity.this, MainActivity.class);
                i.putExtra("pos", pos);
                startActivity(i);
            } else {

                Toast.makeText(TaskActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
