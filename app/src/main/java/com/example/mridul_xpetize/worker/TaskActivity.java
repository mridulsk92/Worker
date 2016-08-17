package com.example.mridul_xpetize.worker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    TextView comments_text, dsc_text, priority_txt, desc, message_view;
    String desc_st, loc_st, start_st, end_st, taskid_st, status_st, comments_st, priority_st, sub_id_st, userId_st, createdBy_st, details_st, comments_post, assignedBy, assignedByName;
    Button submit;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    String createdDate;
    LayoutInflater inflater;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Worker";
    private Uri fileUri; // file url to store image/video
    private ImageView imgPreview;
    ImageButton click;
    String encodedImage;
    Button encodeButton;

    int pos, response_json;

    private Drawer result = null;
    ListView subtask_list, checklist;

    ArrayList<HashMap<String, Object>> dataList;
    List<String> checkedStrings = new ArrayList<String>();
    int k = 0;
    int count = 0;
    private long myDownloadReference;
    private DownloadManager dm;
    SharedPreferences prefNew;
    Button playTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Initialise and add Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.TitleWorker));
        prefNew = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);

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

                            } else if (drawerItem.getIdentifier() == 3) {

                                SharedPreferences sp = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);
                                int selection = sp.getInt("LanguageSelect", -1);
                                AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
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
                                                } else {
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

        //Get Pref
        pref = new PreferencesHelper(TaskActivity.this);
        userId_st = pref.GetPreferences("UserId");

        //Get Intent
        Intent i = getIntent();
        assignedByName = i.getStringExtra("AssignedByName");
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
        createdDate = i.getStringExtra("createdDate");
        pos = i.getIntExtra("pos", -1);

        //Initialise
        playTask = (Button) findViewById(R.id.button_play);
        message_view = (TextView) findViewById(R.id.textView_message);
        encodeButton = (Button) findViewById(R.id.button_encode);
        click = (ImageButton) findViewById(R.id.imageButton_camera);
        imgPreview = (ImageView) findViewById(R.id.imageView_attachment);
        checklist = (ListView) findViewById(R.id.listView_checklist);
        comments_text = (TextView) findViewById(R.id.comments);
        priority_txt = (TextView) findViewById(R.id.priority);
        dsc_text = (TextView) findViewById(R.id.SubDesc);
        dataList = new ArrayList<HashMap<String, Object>>();
//        subtask_list = (ListView) findViewById(R.id.listView_checklist);
        submit = (Button) findViewById(R.id.button_submit);
        desc = (TextView) findViewById(R.id.desc);
        pref = new PreferencesHelper(TaskActivity.this);

        //Set TextView values
        comments_text.setText(comments_st);
        dsc_text.setText(desc_st);

        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/WorkerAudio/SubTask" + taskid_st + ".mp3");
        if (!f.exists()) {
            downloadAudio();
        }

        k = 0;
        count = 0;
        //onClick of submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imgPreview.getDrawable() != null) {

                    final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
                    Bitmap temp = getResizedBitmap(bitmap, 260, 260);
                    encodedImage = encodeToBase64(temp, Bitmap.CompressFormat.JPEG, 50);
                }else{
                    Toast.makeText(TaskActivity.this,"No Attachment",Toast.LENGTH_SHORT).show();
                }
//                encodedImage = resizeBase64Image(temp);
//                encodedImage = getString(R.string.audioEncode);

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

        playTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/WorkerAudio/SubTask" + taskid_st + ".mp3");

                if (file.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(intent);
                } else {
                    Toast.makeText(TaskActivity.this, getString(R.string.NoAudio), Toast.LENGTH_SHORT).show();
                }
            }
        });

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    // Do something with downloaded file.
                    Toast.makeText(TaskActivity.this, getString(R.string.DownloadComplete), Toast.LENGTH_SHORT).show();
                    playTask.setVisibility(View.VISIBLE);
                    message_view.setVisibility(View.GONE);
                }
            }
        };
        registerReceiver(receiver, filter);

        new GetCheckList().execute();

        //onClick of attachment view
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                captureImage();
            }
        });
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

        Intent i = new Intent(TaskActivity.this, TaskActivity.class);
        startActivity(i);
    }

    private void downloadAudio() {

        String path;
        message_view.setVisibility(View.VISIBLE);
        playTask.setVisibility(View.GONE);

        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/WorkerAudio");
        if (!f.exists()) {
            f.mkdir();
        }
        path = f.getPath();

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse("http://vikray.in/NImage/SubTask" + taskid_st + ".mp3"));

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Worker")
                .setDescription("Downloading Task Audio")
                .setDestinationInExternalPublicDir("" + "/WorkerAudio/", "SubTask" + taskid_st + ".mp3");
        myDownloadReference = dm.enqueue(req);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        getString(R.string.CaptureCancelled), Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        getString(R.string.CaptureFailed), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        String tempName = "IMG_" + timeStamp + ".jpg";
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + tempName);
        } else {
            return null;
        }

        return mediaFile;
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
                        Toast.makeText(TaskActivity.this, getString(R.string.NotCompleted), Toast.LENGTH_SHORT).show();
                        new PostAttachment().execute();
                        new PostTask().execute("Pending");
                    } else {
                        Toast.makeText(TaskActivity.this, getString(R.string.NoConnection), Toast.LENGTH_SHORT).show();
                        //Store in SQLite
                        String current_time = getCurrentTimeStamp();
                        SQLite entry = new SQLite(TaskActivity.this);
                        entry.open();
                        entry.createEntry(details_st, taskid_st, userId_st, start_date, end_date, current_time, assignedBy, "4", "1", comments_post, createdBy_st);
                        entry.createEntryNotification("Pending", taskid_st, userId_st, assignedBy, userId_st);
                        String count = entry.getCount();
                        String not_count = entry.getCountNotification();
                        Log.d("Count", count + "NotCount :" + not_count);
                        entry.close();
                        addDialog.dismiss();
                    }
                } else {
                    if (isNetworkAvailable()) {
                        new PostAttachment().execute();
                        new PostTask().execute("Completed");
                    } else {
                        Toast.makeText(TaskActivity.this, getString(R.string.NoConnection), Toast.LENGTH_SHORT).show();
                        //Store in SQLite
                        String current_time = getCurrentTimeStamp();
                        SQLite entry = new SQLite(TaskActivity.this);
                        entry.open();
                        entry.createEntry(details_st, taskid_st, userId_st, start_date, end_date, current_time, assignedBy, "3", "1", comments_post, createdBy_st);
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

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private class PostAttachment extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(TaskActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            userId_st = pref.GetPreferences("UserId");

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/NewAttachment");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            JSONStringer userJson = null;
            // Build JSON string
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("attachment")
                        .object()
                        .key("TaskId").value(taskid_st)
                        .key("IsSubTask").value(true)
                        .key("File").value(encodedImage)
                        .key("FileType").value("jpg")
                        .key("ModifiedBy").value(1)
                        .key("CreatedBy").value(1)
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

                        String message = jsonObject.getString("NewAttachmentResult");

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
                Toast.makeText(TaskActivity.this, getString(R.string.Success), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(TaskActivity.this, getString(R.string.Failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PostTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TaskActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            // Creating service handler class instance

            String check = arg0[0];
            String start_date = getCurrentTimeStamp();
            String end_date = getCurrentTimeStamp();
            String current_time = getCurrentTimeStamp();

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
                            .key("ModifiedDateStr").value(current_time)
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
                            .key("ModifiedDateStr").value(current_time)
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
                    Toast.makeText(TaskActivity.this, getString(R.string.Success), Toast.LENGTH_SHORT).show();
                    new PostNotification().execute("Pending");
                    new PostHistory().execute("Pending");
                } else {
                    Toast.makeText(TaskActivity.this, getString(R.string.Success), Toast.LENGTH_SHORT).show();
                    new PostNotification().execute("Completed");
                    new PostHistory().execute("Completed");
                }
            } else {
                Toast.makeText(TaskActivity.this, getString(R.string.Failed), Toast.LENGTH_SHORT).show();
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
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            HashMap<String, Object> taskMap = new HashMap<String, Object>();
            taskMap.put("CheckList", "Weld Height <= 2cm");
            dataList.add(taskMap);

            HashMap<String, Object> taskMap2 = new HashMap<String, Object>();
            taskMap2.put("CheckList", "Weld Lenght <= 10cm");
            dataList.add(taskMap2);

            HashMap<String, Object> taskMap3 = new HashMap<String, Object>();
            taskMap3.put("CheckList", "Weld Material");
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
            pDialog.setMessage(getString(R.string.pDialog_wait));
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
                Toast.makeText(TaskActivity.this, getString(R.string.Success), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(TaskActivity.this, getString(R.string.Failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PostHistory extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TaskActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String historyDate = getCurrentTimeStamp();
            String status = params[0];
            String username = pref.GetPreferences("UserName");

            HttpPost request = new HttpPost(getString(R.string.url) + "EagleXpetizeService.svc/NewHistory");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");

            JSONStringer userJson = null;
            // Build JSON string
            try {
                userJson = new JSONStringer()
                        .object()
                        .key("history")
                        .object()
                        .key("TaskId").value(taskid_st)
                        .key("IsSubTask").value(1)
                        .key("Notes").value("SubTask Submitted for approval by: " + username)
                        .key("Comments").value(status)
//                        .key("HistoryDate").value(historyDate)
//                        .key("CreatedDate").value(createdDate)
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

                        String message = jsonObject.getString("NewHistoryResult");

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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (response_json == 200) {
                Toast.makeText(TaskActivity.this, getString(R.string.Success), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(TaskActivity.this, getString(R.string.Failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        super.finish();
//    }
}
