package com.example.mridul_xpetize.worker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends AppCompatActivity {

    ListView tasks_list;
    ProgressDialog pDialog, pDialogN;
    PreferencesHelper pref;
    private Drawer result = null;
    LayoutInflater inflater;
    JSONArray tasks;
    RelativeLayout main_rel;

    ArrayList<HashMap<String, Object>> dataList;

    String start_og, end_og, createdDate;
    CustomAdapter cardAdapter;
    SwipeRefreshLayout swipe;
    SharedPreferences prefNew;

    int count;
    ListView hidden_not;
    ArrayList<HashMap<String, Object>> notiList;
    MenuItem menuItem;
    int click = 0;

    //Creating a broadcast receiver for gcm registration
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.TitleWorker));

        //Initialize
        notiList = new ArrayList<>();
        prefNew = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_camera);
        main_rel = (RelativeLayout) findViewById(R.id.main_layout);
        hidden_not = (ListView) findViewById(R.id.listView_hidden_notification);
        tasks_list = (ListView) findViewById(R.id.listView_taskList);
        dataList = new ArrayList<HashMap<String, Object>>();

        //Swipe Refresh
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("LOG TAG", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        new GetTaskList().execute();
                    }
                }
        );

        //hide notification list when clicked on layout
        main_rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hidden_not.getVisibility() == View.VISIBLE) {
                    hidden_not.setVisibility(View.GONE);
                }
            }
        });

        //onClick of Floating Action Button
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        //Notification List onClick
        hidden_not.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                count--;
                if (count <= 0) {
                    count = 0;
                }

                menuItem.setIcon(buildCounterDrawable(count, R.drawable.blue_bell_small));
                parent.getChildAt(position - hidden_not.getFirstVisiblePosition()).setBackgroundColor(Color.TRANSPARENT);

            }
        });

        //Get preference values
        pref = new PreferencesHelper(MainActivity.this);
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
                        new PrimaryDrawerItem().withName(getString(R.string.About)).withIcon(getResources().getDrawable(R.drawable.ic_about)).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.Language)).withIcon(getResources().getDrawable(R.drawable.language_switch_ic)).withIdentifier(3).withSelectable(false),
                        new SecondaryDrawerItem().withName(getString(R.string.LogOut)).withIcon(getResources().getDrawable(R.drawable.ic_logout)).withIdentifier(2).withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 1) {

                                //Clicked About

                            } else if (drawerItem.getIdentifier() == 2) {

                                //Clicked LogOut

                            }else if(drawerItem.getIdentifier() == 3){

                                SharedPreferences sp = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);
                                int selection = sp.getInt("LanguageSelect", -1);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                CharSequence[] array = {"English", "Japanese"};
                                builder.setTitle("Select Language")
                                        .setSingleChoiceItems(array, selection, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                if (which == 1) {
                                                    String lang = "ja";
                                                    pref.SavePreferences("Language", lang);
                                                    SharedPreferences.Editor editor = prefNew.edit();
                                                    editor.putInt("LanguageSelect", which);
                                                    editor.commit();
                                                    changeLang(lang);
                                                }else{
                                                    String lang = "en";
                                                    pref.SavePreferences("Language", lang);
                                                    SharedPreferences.Editor editor = prefNew.edit();
                                                    editor.putInt("LanguageSelect", which);
                                                    editor.commit();
                                                    changeLang(lang);
                                                }
                                            }
                                        })

                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                // User clicked OK, so save the result somewhere

                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });

                                builder.create();
                                builder.show();
                            }
                        }
                        return false;
                    }
                })
                .build();

        //ToggleButton on ToolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        //onClick of ListView items
        tasks_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get TextView values and assign to String
                String desc = ((TextView) view.findViewById(R.id.desc)).getText().toString();
                String details_id = ((TextView) view.findViewById(R.id.details_id)).getText().toString();
                String assignedBy = ((TextView) view.findViewById(R.id.assignedBy)).getText().toString();
                String assignedByName = ((TextView) view.findViewById(R.id.assignedByName)).getText().toString();
                String jobOrder = ((TextView) view.findViewById(R.id.jobOrder)).getText().toString();
                String statusId = ((TextView) view.findViewById(R.id.statusId)).getText().toString();
                String comments = ((TextView) view.findViewById(R.id.comments)).getText().toString();
                String priority = ((TextView) view.findViewById(R.id.priority)).getText().toString();
                String subTaskId = ((TextView) view.findViewById(R.id.subtask_id)).getText().toString();
                String st_date = ((TextView) view.findViewById(R.id.start)).getText().toString();
                String end_date = ((TextView) view.findViewById(R.id.end)).getText().toString();
                String taskId = ((TextView) view.findViewById(R.id.task_id)).getText().toString();
                String createdBy_st = ((TextView) view.findViewById(R.id.createdBy)).getText().toString();

                //Pass the Strings to the next Activity
                Intent i = new Intent(MainActivity.this, TaskActivity.class);
                i.putExtra("desc", desc);
//                i.putExtra("jobOrder", jobOrder);
//                i.putExtra("start", start_og);
//                i.putExtra("end", end_og);
                i.putExtra("TaskDetailsId", details_id);
                i.putExtra("task_id", taskId);
                i.putExtra("AssignedByName", assignedByName);
                i.putExtra("pos", position);
                i.putExtra("statusId", statusId);
                i.putExtra("comments", comments);
                i.putExtra("priority", priority);
                i.putExtra("subTaskId", subTaskId);
                i.putExtra("assignedBy", assignedBy);
                i.putExtra("createdBy", createdBy_st);
                i.putExtra("createdDate", createdDate);
                startActivity(i);
            }
        });

        new GetTaskList().execute();

        new GetNotiList().execute();


        //Initializing our broadcast receiver
//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//
//            //When the broadcast received
//            //We are sending the broadcast from GCMRegistrationIntentService
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                //If the broadcast has received with success
//                //that means device is registered successfully
//                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
//                    //Getting the registration token from the intent
//                    String token = intent.getStringExtra("token");
//                    //Displaying the token as toast
////                    Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
//
//                    //if the intent is not with success then displaying error messages
//                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
//                    Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
//                }
//            }
//        };
//
//        //Checking play service is available or not
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
//
//        //if play service is not available
//        if (ConnectionResult.SUCCESS != resultCode) {
//            //If play service is supported but not installed
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                //Displaying message that play service is not installed
//                Toast.makeText(getApplicationContext(), getString(R.string.GPNotEnabled), Toast.LENGTH_LONG).show();
//                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
//
//                //If play service is not supported
//                //Displaying an error message
//            } else {
//                Toast.makeText(getApplicationContext(), getString(R.string.GPNotSupported), Toast.LENGTH_LONG).show();
//            }
//
//            //If play service is available
//        } else {
//            //Starting intent to register device
//            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
//            startService(intent);
//        }
    }


    public void changeLang(String lang) {

        if (lang.equalsIgnoreCase(""))
            return;
        Locale myLocale = new Locale(lang);
//        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        updateTexts();
    }

    private void updateTexts() {

        Intent i = new Intent(MainActivity.this, MainActivity.class);
        startActivity(i);
    }

    //Registering receiver on activity resume
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            TextView comments, desc, priority, startdate, enddate, jobOrder, statusId, id, subId, createdBy, subName, isSub, detailsId, assignedBy, assignedByName;
            ImageButton PlayOrPause;
            SeekBar audioSeek;
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
                viewHolder.assignedByName = (TextView) convertView.findViewById(R.id.assignedByName);
                viewHolder.assignedBy = (TextView) convertView.findViewById(R.id.assignedBy);
                viewHolder.detailsId = (TextView) convertView.findViewById(R.id.details_id);
                viewHolder.subName = (TextView) convertView.findViewById(R.id.subName);
                viewHolder.createdBy = (TextView) convertView.findViewById(R.id.createdBy);
                viewHolder.subId = (TextView) convertView.findViewById(R.id.subtask_id);
                viewHolder.comments = (TextView) convertView.findViewById(R.id.comments);
                viewHolder.desc = (TextView) convertView.findViewById(R.id.desc);
                viewHolder.isSub = (TextView) convertView.findViewById(R.id.isSub);
                viewHolder.priority = (TextView) convertView.findViewById(R.id.priority);
                viewHolder.startdate = (TextView) convertView.findViewById(R.id.start);
                viewHolder.enddate = (TextView) convertView.findViewById(R.id.end);
                viewHolder.jobOrder = (TextView) convertView.findViewById(R.id.jobOrder);
                viewHolder.statusId = (TextView) convertView.findViewById(R.id.statusId);
                viewHolder.id = (TextView) convertView.findViewById(R.id.task_id);
                viewHolder.cv = (CardView) convertView.findViewById(R.id.card_task);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            viewHolder.assignedByName.setText(dataList.get(position).get("AssignedByName").toString());
            viewHolder.assignedBy.setText(dataList.get(position).get("AssignedById").toString());
            viewHolder.detailsId.setText(dataList.get(position).get("TaskDetailsId").toString());
            viewHolder.subName.setText(dataList.get(position).get("TaskName").toString());
            viewHolder.createdBy.setText(dataList.get(position).get("CreatedBy").toString());
            viewHolder.comments.setText(dataList.get(position).get("Comments").toString());
            viewHolder.desc.setText(dataList.get(position).get("TaskDescription").toString());
//            viewHolder.priority.setText(dataList.get(position).get("Priority").toString());
//            viewHolder.startdate.setText(dataList.get(position).get("TaskStartDate").toString());
//            viewHolder.enddate.setText(dataList.get(position).get("TaskEndDate").toString());
//            viewHolder.jobOrder.setText(dataList.get(position).get("JobOrder").toString());
            viewHolder.isSub.setText(dataList.get(position).get("IsSub").toString());
            viewHolder.statusId.setText(dataList.get(position).get("StatusId").toString());
            viewHolder.id.setText(dataList.get(position).get("TaskId").toString());
//            viewHolder.subId.setText(dataList.get(position).get("SubTaskId").toString());

            return convertView;
        }
    }

    private class GetTaskList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dataList.clear();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String user_id = pref.GetPreferences("UserId");

            HttpPost request = new HttpPost("http://vikray.in/EagleXpetizeService.svc/TaskAssigned");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            // Build JSON string
            JSONStringer userJson = null;
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("taskDetails")
                        .object()
                        .key("TaskDetailsId").value(0)
                        .key("TaskId").value(0)
                        .key("AssignedToId").value(user_id)
                        .key("AssignedById").value(0)
                        .key("IsSubTask").value(1)
                        .key("StatusId").value(1)
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

                        JSONObject json1 = new JSONObject(response);
                        tasks = json1.getJSONArray("TaskAssignedResult");

                        // Looping through Array
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject c = tasks.getJSONObject(i);

                            String id = c.getString("TaskId");
                            String detailsId = c.getString("TaskDetailsId");
                            String name = c.getString("TaskName");
                            String desc = c.getString("TaskDescription");
                            String comments = c.getString("Comments");
                            String isSub = c.getString("IsSubTask");
                            createdDate = c.getString("CreatedDate");
                            String createdBy = c.getString("CreatedBy");
                            String assignedBy = c.getString("AssignedById");
                            String assignedByName = c.getString("AssignedByName");
                            int statusId = c.getInt("StatusId");
//                            int subId = c.getInt("SubTaskId");

                            //adding each child node to HashMap key => value
                            HashMap<String, Object> taskMap = new HashMap<String, Object>();
                            taskMap.put("TaskDescription", desc);
                            taskMap.put("TaskDetailsId", detailsId);
                            taskMap.put("AssignedById", assignedBy);
                            taskMap.put("CreatedBy", createdBy);
                            taskMap.put("TaskId", id);
                            taskMap.put("TaskName", name);
                            taskMap.put("IsSub", isSub);
//                            taskMap.put("SubTaskId", subId);
                            taskMap.put("StatusId", statusId);
                            taskMap.put("Comments", comments);
                            taskMap.put("AssignedByName", assignedByName);
//                            contact.put("Priority", "Priority : " + priority);

                            dataList.add(taskMap);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
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

            if (swipe.isRefreshing()) {
                swipe.setRefreshing(false);
            }

            cardAdapter = new CustomAdapter(MainActivity.this, R.layout.task_list, dataList);
            tasks_list.setAdapter(cardAdapter);

        }
    }

    private class CustomAdapterNot extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapterNot(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            TextView not, isRead, byName, taskName;
            LinearLayout noti_linear;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.notification_layout, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.taskName = (TextView) convertView.findViewById(R.id.noti_task);
                viewHolder.byName = (TextView) convertView.findViewById(R.id.noti_by);
                viewHolder.noti_linear = (LinearLayout) convertView.findViewById(R.id.not_layout);
                viewHolder.not = (TextView) convertView.findViewById(R.id.textview_noti);
                viewHolder.isRead = (TextView) convertView.findViewById(R.id.textview_isRead);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();

            //set the data to be displayed
            viewHolder.byName.setText(notiList.get(position).get("UserName").toString());
            viewHolder.taskName.setText(notiList.get(position).get("TaskName").toString());
            viewHolder.not.setText(notiList.get(position).get("Description").toString());
            viewHolder.noti_linear.setBackgroundColor(Color.LTGRAY);

//            for (int i = 0; i < savedList.size(); i++) {
//                Log.d("Test Custom", String.valueOf(savedList.get(i)));
//                if (position == savedList.get(i)) {
//                    viewHolder.noti_linear.setBackgroundColor(Color.TRANSPARENT);
//                } else {
//                    viewHolder.noti_linear.setBackgroundColor(Color.LTGRAY);
//                }
//            }

            return convertView;
        }
    }

    private class GetNotiList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dataList.clear();
            // Showing progress dialog
            pDialogN = new ProgressDialog(MainActivity.this);
            pDialogN.setMessage(getString(R.string.pDialog_wait));
            pDialogN.setCancelable(false);
            pDialogN.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String user_id = pref.GetPreferences("UserId");

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            String url = getString(R.string.url) + "EagleXpetizeService.svc/Notifications/" + user_id + "/1";
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            Log.d("Url", url);
            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {

                try {

                    JSONArray tasks = new JSONArray(jsonStr);

                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject c = tasks.getJSONObject(i);

                        String id = c.getString("TaskId");
                        String taskName = c.getString("TaskName");
                        String username = c.getString("UserName");
                        String description = c.getString("Description");
                        String byId = c.getString("ById");
                        String toId = c.getString("ToId");
                        String isNew = c.getString("IsNew");

                        // adding each child node to HashMap key => value
                        HashMap<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put("TaskId", id);
                        taskMap.put("UserName", username);
                        taskMap.put("TaskName", taskName);
                        taskMap.put("Description", description);
                        taskMap.put("ById", byId);
                        taskMap.put("ToId", toId);
                        taskMap.put("IsNew", isNew);
                        notiList.add(taskMap);

                    }

                    count = notiList.size();
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

            if (pDialogN.isShowing())
                pDialogN.dismiss();

            // initialize pop up window
            CustomAdapterNot notAdapter = new CustomAdapterNot(MainActivity.this, R.layout.popup_layout, notiList);
            hidden_not.setAdapter(notAdapter);
            menuItem.setIcon(buildCounterDrawable(count, R.drawable.blue_bell_small));

        }
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_my, menu);

        menuItem = menu.findItem(R.id.testAction);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (hidden_not.getVisibility() == View.VISIBLE) {
                    hidden_not.setVisibility(View.GONE);
                } else {
                    hidden_not.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

//        MenuItem item = menu.findItem(R.id.badge);
//        MenuItemCompat.setActionView(item, R.layout.feed_update_count);
//        View view = MenuItemCompat.getActionView(item);
//        notifCount = (Button)view.findViewById(R.id.notif_count);
//        notifCount.setText(String.valueOf(mNotifCount));
//
//        // Get the notifications MenuItem and LayerDrawable (layer-list)
////        MenuItem item_noti = menu.findItem(R.id.action_noti);
//        MenuItem item_logOut = menu.findItem(R.id.action_logOut);
//
//        item_logOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//
//
//                return false;
//            }
//        });
//
////        item_noti.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
////            @Override
////            public boolean onMenuItemClick(MenuItem item) {
////
////                Intent i = new Intent(DashboardActivity.this, NotificationActivity.class);
////                startActivity(i);
////                return false;
////            }
////        });

        return true;
    }

    private Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.counter_menuitem_layout, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.rel_panel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }

}
