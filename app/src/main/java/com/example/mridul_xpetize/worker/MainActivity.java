package com.example.mridul_xpetize.worker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ListView tasks_list;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    private Drawer result = null;

    JSONArray tasks;

    private static String TAG_DESCRIPTION = "Description";
    private static String TAG_ID = "Id";
    private static String TAG_STARTDATE = "TaskStartDate";
    private static String TAG_ENDDATE = "TaskEndDate";
    private static String TAG_PRIORITY = "TaskPriority";

    ArrayList<HashMap<String, String>> dataList;
    ArrayList<HashMap<String, String>> highPriorityList;
    ArrayList<HashMap<String, String>> mediumPriorityList;
    ArrayList<HashMap<String, String>> lowPriorityList;

    String start_og, end_og;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Worker");
        toolbar.setTitleTextColor(Color.WHITE);

        //Initialize
        tasks_list = (ListView) findViewById(R.id.listView_taskList);
        dataList = new ArrayList<HashMap<String, String>>();
        highPriorityList = new ArrayList<HashMap<String, String>>();
        mediumPriorityList = new ArrayList<HashMap<String, String>>();
        lowPriorityList = new ArrayList<HashMap<String, String>>();

        pref = new PreferencesHelper(MainActivity.this);
        String name = pref.GetPreferences("Name");

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
                        new SecondaryDrawerItem().withName("Log Out").withIcon(getResources().getDrawable(R.drawable.ic_logout)).withIdentifier(2).withSelectable(false),
                        new SectionDrawerItem().withName("Filter"),
//                        new SecondaryDrawerItem().withName("New Task").withIcon(getResources().getDrawable(R.drawable.ic_filter)).withSelectable(false),
                        new SecondaryDrawerItem().withName("All Task").withIcon(getResources().getDrawable(R.drawable.ic_filter)).withIdentifier(6).withSelectable(false),
                        new SecondaryDrawerItem().withName("High Priority").withIcon(getResources().getDrawable(R.drawable.ic_filter)).withIdentifier(3).withSelectable(false),
                        new SecondaryDrawerItem().withName("Medium Priority").withIcon(getResources().getDrawable(R.drawable.ic_filter)).withIdentifier(4).withSelectable(false),
                        new SecondaryDrawerItem().withName("Low Priority").withIcon(getResources().getDrawable(R.drawable.ic_filter)).withIdentifier(5).withSelectable(false)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 3) {

                                //Load high priority tasks
                                ListAdapter adapter = new SimpleAdapter(
                                        MainActivity.this, highPriorityList,
                                        R.layout.task_list, new String[]{TAG_DESCRIPTION, TAG_ID, TAG_STARTDATE, TAG_ENDDATE, TAG_PRIORITY},
                                        new int[]{R.id.desc, R.id.task_id, R.id.start, R.id.end, R.id.priority});

                                tasks_list.setAdapter(adapter);
                            } else if (drawerItem.getIdentifier() == 4) {

                                //Load Medium priority tasks
                                ListAdapter adapter = new SimpleAdapter(
                                        MainActivity.this, mediumPriorityList,
                                        R.layout.task_list, new String[]{TAG_DESCRIPTION, TAG_ID, TAG_STARTDATE, TAG_ENDDATE, TAG_PRIORITY},
                                        new int[]{R.id.desc, R.id.task_id, R.id.start, R.id.end, R.id.priority});

                                tasks_list.setAdapter(adapter);
                            } else if (drawerItem.getIdentifier() == 5) {

                                //Load low priority tasks
                                ListAdapter adapter = new SimpleAdapter(
                                        MainActivity.this, lowPriorityList,
                                        R.layout.task_list, new String[]{TAG_DESCRIPTION, TAG_ID, TAG_STARTDATE, TAG_ENDDATE, TAG_PRIORITY},
                                        new int[]{R.id.desc, R.id.task_id, R.id.start, R.id.end, R.id.priority});

                                tasks_list.setAdapter(adapter);
                            } else if (drawerItem.getIdentifier() == 6) {

                                //Load all tasks
                                ListAdapter adapter = new SimpleAdapter(
                                        MainActivity.this, dataList,
                                        R.layout.task_list, new String[]{TAG_DESCRIPTION, TAG_ID, TAG_STARTDATE, TAG_ENDDATE, TAG_PRIORITY},
                                        new int[]{R.id.desc, R.id.task_id, R.id.start, R.id.end, R.id.priority});

                                tasks_list.setAdapter(adapter);
                            }
                        }
                        return false;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        tasks_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get TextView values and assign to String
                String desc = ((TextView) view.findViewById(R.id.desc)).getText().toString();
                String loc = ((TextView) view.findViewById(R.id.location)).getText().toString();
                String st_date = ((TextView) view.findViewById(R.id.start)).getText().toString();
                String end_date = ((TextView) view.findViewById(R.id.end)).getText().toString();
                String taskId = ((TextView) view.findViewById(R.id.task_id)).getText().toString();

                //Pass the Strings to the next Activity
                Intent i = new Intent(MainActivity.this, TaskActivity.class);
                i.putExtra("desc", desc);
                i.putExtra("loc", loc);
                i.putExtra("start", start_og);
                i.putExtra("end", end_og);
                i.putExtra("task_id", taskId);
                startActivity(i);
            }
        });

        new GetTaskList().execute();

    }

    //AsyncTask to get tasks(to be edited)
    private class GetTaskList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            dataList.clear();
            highPriorityList.clear();
            mediumPriorityList.clear();
            lowPriorityList.clear();

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            String user_id = pref.GetPreferences("User Id");
            String url = "http://vikray.in/MyService.asmx/ExcProcedure?Para=Proc_GetTaskMst&Para=" + user_id;
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
                        start_og = c.getString(TAG_STARTDATE);
                        end_og = c.getString(TAG_ENDDATE);
                        int priority = c.getInt(TAG_PRIORITY);
                        String priority_string = "High";

                        if (priority == 1) {

                            priority_string = "High";

                            HashMap<String, String> tempHigh = new HashMap<String, String>();

                            tempHigh.put(TAG_DESCRIPTION, "Description : " + desc);
                            tempHigh.put(TAG_ID, id);
                            tempHigh.put(TAG_STARTDATE, "Start Date : " + start_og);
                            tempHigh.put(TAG_ENDDATE, "End Date : " + end_og);
                            tempHigh.put(TAG_PRIORITY, "Priority : " + priority_string);
                            highPriorityList.add(tempHigh);

                        } else if (priority == 2) {

                            priority_string = "Medium";

                            HashMap<String, String> tempMedium = new HashMap<String, String>();

                            tempMedium.put(TAG_DESCRIPTION, "Description : " + desc);
                            tempMedium.put(TAG_ID, id);
                            tempMedium.put(TAG_STARTDATE, "Start Date : " + start_og);
                            tempMedium.put(TAG_ENDDATE, "End Date : " + end_og);
                            tempMedium.put(TAG_PRIORITY, "Priority : " + priority_string);
                            mediumPriorityList.add(tempMedium);

                        } else if (priority == 3) {

                            priority_string = "Low";

                            HashMap<String, String> tempLow = new HashMap<String, String>();

                            tempLow.put(TAG_DESCRIPTION, "Description : " + desc);
                            tempLow.put(TAG_ID, id);
                            tempLow.put(TAG_STARTDATE, "Start Date : " + start_og);
                            tempLow.put(TAG_ENDDATE, "End Date : " + end_og);
                            tempLow.put(TAG_PRIORITY, "Priority : " + priority_string);
                            lowPriorityList.add(tempLow);

                        } else {
                            priority_string = "High";
                        }

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_DESCRIPTION, "Description : " + desc);
                        contact.put(TAG_ID, id);
                        contact.put(TAG_STARTDATE, "Start Date : " + start_og);
                        contact.put(TAG_ENDDATE, "End Date : " + end_og);
                        contact.put(TAG_PRIORITY, "Priority : " + priority_string);
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, dataList,
                    R.layout.task_list, new String[]{TAG_DESCRIPTION, TAG_ID, TAG_STARTDATE, TAG_ENDDATE, TAG_PRIORITY},
                    new int[]{R.id.desc, R.id.task_id, R.id.start, R.id.end, R.id.priority});

            tasks_list.setAdapter(adapter);
        }
    }
}
