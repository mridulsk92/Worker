package com.example.mridul_xpetize.worker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    TextView comments_text, dsc_text, priority_txt, desc;
    String desc_st, loc_st, start_st, end_st, taskid_st, status_st, comments_st, priority_st, sub_id_st, userId_st, createdBy_st, details_st, comments_post, assignedBy;
    Button submit;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    LayoutInflater inflater;

    int pos, response_json;

    private Drawer result = null;
    ListView subtask_list, checklist;

    ArrayList<HashMap<String, Object>> dataList;
    List<String> checkedStrings = new ArrayList<String>();
    int k = 0;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Worker");

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

        //Get Pref
        pref = new PreferencesHelper(TaskActivity.this);
        userId_st = pref.GetPreferences("UserId");

        //Get Intent
        Intent i = getIntent();
        assignedBy = i.getStringExtra("assignedBy");
        status_st = i.getStringExtra("statusId");
        details_st = i.getStringExtra("TaskDetailsId");
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
        checklist = (ListView) findViewById(R.id.listView_checklist);
        comments_text = (TextView) findViewById(R.id.comments);
        priority_txt = (TextView) findViewById(R.id.priority);
        dsc_text = (TextView) findViewById(R.id.SubDesc);
        dataList = new ArrayList<HashMap<String, Object>>();
        subtask_list = (ListView) findViewById(R.id.listView_checklist);
        submit = (Button) findViewById(R.id.button_submit);
        desc = (TextView) findViewById(R.id.desc);
        pref = new PreferencesHelper(TaskActivity.this);

        //Set TextView values
        comments_text.setText(comments_st);
        dsc_text.setText(desc_st);

        k = 0;
        count = 0;
        //onClick of submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                count = checklist.getCount();
                for (int j = 0; j < dataList.size(); j++) {

                    if ((CheckBox) checklist.getChildAt(j).findViewById(R.id.checkBox_item) != null) {

                        CheckBox cBox = (CheckBox) checklist.getChildAt(j).findViewById(R.id.checkBox_item);

                        if (cBox.isChecked()) {
                            k++;
                        }
                    }
                }


                SubmitDialog();
            }
        });

        new GetCheckList().execute();
    }

    private void SubmitDialog() {

        LayoutInflater factory = LayoutInflater.from(TaskActivity.this);
        final View addView = factory.inflate(
                R.layout.submit_dialog, null);
        final AlertDialog addDialog = new AlertDialog.Builder(TaskActivity.this).create();
        addDialog.setView(addView);

        //Initialise
        final EditText commentBox = (EditText) addView.findViewById(R.id.subimt_comment_text);
        Button submitTask = (Button) addView.findViewById(R.id.button_submit);

        //onClick of SubmitButton
        submitTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String start_date = getCurrentTimeStamp();
                String end_date = getCurrentTimeStamp();
                comments_post = commentBox.getText().toString();
                if (k != count) {
                    if (isNetworkAvailable()) {
                        Toast.makeText(TaskActivity.this, "Not Completed", Toast.LENGTH_SHORT).show();
                        new PostTask().execute("Pending");
                    } else {
                        Toast.makeText(TaskActivity.this, "No Internet Connection found. Data will be stored locally",Toast.LENGTH_SHORT).show();
                        //Store in SQLite
                        SQLite entry = new SQLite(TaskActivity.this);
                        entry.open();
                        entry.createEntry(details_st, taskid_st, userId_st, start_date, end_date, assignedBy, "4", "1", comments_post, createdBy_st);
                        entry.createEntryNotification("Pending", taskid_st, userId_st, assignedBy, userId_st);
                        String count = entry.getCount();
                        String not_count = entry.getCountNotification();
                        Log.d("Count", count + "NotCount :" + not_count);
                        entry.close();
                        addDialog.dismiss();
                    }
                } else {
                    if (isNetworkAvailable()) {
                        new PostTask().execute("Completed");
                    } else {
                        Toast.makeText(TaskActivity.this, "No Internet Connection found. Data will be stored locally",Toast.LENGTH_SHORT).show();
                        //Store in SQLite
                        SQLite entry = new SQLite(TaskActivity.this);
                        entry.open();
                        entry.createEntry(details_st, taskid_st, userId_st, start_date, end_date, assignedBy, "3", "1", comments_post, createdBy_st);
                        entry.createEntryNotification("Completed", taskid_st, userId_st, assignedBy, userId_st);
                        String count = entry.getCount();
                        String not_count = entry.getCountNotification();
                        Log.d("Count", count + "NotCount :" + not_count);
                        entry.close();
                        addDialog.dismiss();
                    }
                }
                k = 0;
            }
        });

        addDialog.show();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }

    private class PostTask extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(String... arg0) {
            // Creating service handler class instance

            String check = arg0[0];
            String start_date = getCurrentTimeStamp();
            String end_date = getCurrentTimeStamp();

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/UpdateAssignedTask");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            JSONStringer userJson = null;
            if (check.equals("Pending")) {
                // Build JSON string
                try {
                    userJson = new JSONStringer()
                            .object()
                            .key("taskDetails")
                            .object()
                            .key("TaskDetailsId").value(details_st)
                            .key("TaskId").value(taskid_st)
                            .key("AssignedToId").value(userId_st)
                            .key("StartDateStr").value(start_date)
                            .key("EndDateStr").value(end_date)
                            .key("AssignedById").value(assignedBy)
                            .key("StatusId").value(4)
                            .key("IsSubTask").value(1)
                            .key("Comments").value(comments_post)
                            .key("CreatedBy").value(createdBy_st)
                            .endObject()
                            .endObject();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Json", String.valueOf(userJson));
            } else {

                // Build JSON string
                try {
                    userJson = new JSONStringer()
                            .object()
                            .key("taskDetails")
                            .object()
                            .key("TaskDetailsId").value(details_st)
                            .key("TaskId").value(taskid_st)
                            .key("AssignedToId").value(userId_st)
                            .key("StartDateStr").value(start_date)
                            .key("EndDateStr").value(end_date)
                            .key("AssignedById").value(assignedBy)
                            .key("StatusId").value(3)
                            .key("IsSubTask").value(1)
                            .key("Comments").value(comments_post)
                            .key("CreatedBy").value(createdBy_st)
                            .endObject()
                            .endObject();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Json", String.valueOf(userJson));
            }
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

                        String message = jsonObject.getString("UpdateAssignedTaskResult");

                        //Save userid and username if success
                        if (message.equals("success")) {
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
            return check;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (response_json == 200) {
                if (result.equals("Pending")) {
                    Toast.makeText(TaskActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    new PostNotification().execute("Pending");
                } else {
                    Toast.makeText(TaskActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    new PostNotification().execute("Completed");
                }
            } else {
                Toast.makeText(TaskActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.menu_my, menu);

        // Get the notifications MenuItem and LayerDrawable (layer-list)
        MenuItem item_noti = menu.findItem(R.id.action_noti);
        MenuItem item_logOut = menu.findItem(R.id.action_logOut);

        item_logOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                return false;
            }
        });

        item_noti.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                return false;
            }
        });

        return true;
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            CheckBox checkBox;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.checklist, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox_item);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            viewHolder.checkBox.setText(dataList.get(position).get("CheckList").toString());
            return convertView;
        }
    }

    private class GetCheckList extends AsyncTask<Void, Void, Void> {

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
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> taskMap = new HashMap<String, Object>();
            taskMap.put("CheckList", "item1");
            dataList.add(taskMap);

            HashMap<String, Object> taskMap2 = new HashMap<String, Object>();
            taskMap2.put("CheckList", "item2");
            dataList.add(taskMap2);

            HashMap<String, Object> taskMap3 = new HashMap<String, Object>();
            taskMap3.put("CheckList", "item3");
            dataList.add(taskMap3);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing())
                pDialog.dismiss();

            CustomAdapter cardAdapter = new CustomAdapter(TaskActivity.this, R.layout.checklist, dataList);
            checklist.setAdapter(cardAdapter);
        }
    }

    private class PostNotification extends AsyncTask<String, Void, Void> {

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
        protected Void doInBackground(String... params) {

            String status = params[0];

            userId_st = pref.GetPreferences("UserId");

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/NewNotification");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            JSONStringer userJson = null;
            // Build JSON string
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("notification")
                        .object()
                        .key("Description").value(status)
                        .key("TaskId").value(taskid_st)
                        .key("ById").value(userId_st)
                        .key("ToId").value(assignedBy)
                        .key("CreatedBy").value(userId_st)
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

                        String message = jsonObject.getString("NewNotificationResult");

                        //Save userid and username if success
                        if (message.equals("success")) {
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (response_json == 200) {
                Toast.makeText(TaskActivity.this, "Success", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(TaskActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
