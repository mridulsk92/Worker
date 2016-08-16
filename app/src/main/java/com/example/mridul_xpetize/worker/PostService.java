package com.example.mridul_xpetize.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostService extends Service {

    Calendar cur_cal = Calendar.getInstance();
    String db_details, db_task, db_start, db_end, db_by, db_comments, db_status, db_user, db_sub, db_created, db_modified;
    String db_desc, db_not_task, db_not_by, db_not_to, db_not_created;
    int response_json, count, countNot;

    public PostService() {
    }

    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Intent intent = new Intent(this, PostService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                30 * 1000 * 1, pintent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.d("Service", "Started");
        SQLite sql = new SQLite(PostService.this);
        sql.open();
        count = Integer.parseInt(sql.getCount());
        sql.close();
        Log.d("Service Count", String.valueOf(count));

        //If post count is zero execute Notification post
        if (count == 0) {

            SQLite notC = new SQLite(PostService.this);
            notC.open();
            countNot = Integer.parseInt(notC.getCount());
            notC.close();
            Log.d("Service Count Not", String.valueOf(countNot));

            for (int i = 0; i < countNot; i++) {

                SQLite getNot = new SQLite(PostService.this);
                getNot.open();
                String notData[] = getNot.getNotificationRow();
                db_desc = notData[1];
                db_not_task = notData[2];
                db_not_by = notData[3];
                db_not_to = notData[4];
                db_not_created = notData[5];
                getNot.close();
                new PostNotification().execute();

            }
        }
        for (int i = 0; i < count; i++) {

            SQLite getData = new SQLite(PostService.this);
            getData.open();
            String datas[] = getData.getFirstRow();
            db_details = datas[1];
            db_task = datas[2];
            db_user = datas[3];
            db_start = datas[4];
            db_end = datas[5];
            db_modified = datas[6];
            db_by = datas[7];
            db_status = datas[8];
            db_sub = datas[9];
            db_comments = datas[10];
            db_created = datas[11];
            getData.close();
            new PostTask().execute();

        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private class PostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/UpdateAssignedTask");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            JSONStringer userJson = null;
            // Build JSON string
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("taskDetails")
                        .object()
                        .key("TaskDetailsId").value(db_details)
                        .key("TaskId").value(db_task)
                        .key("AssignedToId").value(db_user)
                        .key("StartDateStr").value(db_start)
                        .key("EndDateStr").value(db_end)
                        .key("ModifiedDateStr").value(db_modified)
                        .key("AssignedById").value(db_by)
                        .key("StatusId").value(db_status)
                        .key("IsSubTask").value(db_sub)
                        .key("Comments").value(db_comments)
                        .key("CreatedBy").value(db_created)
                        .endObject()
                        .endObject();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("UserJson", String.valueOf(userJson));

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
                            Log.d("Service", "Success");
                        } else {
                            response_json = 201;
                            Log.d("Service", "Failed");
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

            if (response_json == 200) {
                SQLite del = new SQLite(PostService.this);
                del.open();
                del.deleteFirstRow();
                del.close();
            }
        }
    }

    private class PostNotification extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

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
                        .key("Description").value(db_desc)
                        .key("TaskId").value(db_not_task)
                        .key("ById").value(db_not_by)
                        .key("ToId").value(db_not_to)
                        .key("CreatedBy").value(db_not_created)
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

            if (response_json == 200) {
                Log.d("Not Service", "Success");
                SQLite del = new SQLite(PostService.this);
                del.deleteNotificationRow();
                del.close();
            } else {
                Log.d("Not Service", "Failed");

            }
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
