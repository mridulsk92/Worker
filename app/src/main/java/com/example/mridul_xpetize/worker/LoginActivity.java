package com.example.mridul_xpetize.worker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.PreferenceChangeEvent;

public class LoginActivity extends AppCompatActivity {

    Button login;
    ImageButton languageBtn;
    EditText username, password;
    String username_st, password_st;
    int response;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    SharedPreferences prefNew;

    private static String TAG_NAME = "UserName";
    private static String TAG_ID = "UserId";
    private static String TAG_MESSAGE = "Message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialise
        prefNew = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);
        languageBtn = (ImageButton) findViewById(R.id.imageButton_language);
        login = (Button) findViewById(R.id.button_login);
        username = (EditText) findViewById(R.id.editText_username);
        password = (EditText) findViewById(R.id.editText_password);
        pref = new PreferencesHelper(LoginActivity.this);

        //onClick of Login Button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get EditText Values
                username_st = username.getText().toString();
                password_st = password.getText().toString();

                //PostLogin Details to Server
                new PostLogin().execute();

            }
        });

        //onClick of ImageButton
        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sp = getSharedPreferences("LangPref", Activity.MODE_PRIVATE);
                int selection = sp.getInt("LanguageSelect", -1);

                Log.d("Test", String.valueOf(selection));

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
        });
    }

    private class PostLogin extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            //Url with parameters
            String url = getString(R.string.url) + "EagleXpetizeService.svc/CheckUserLogin/" + username_st + "/" + password_st + "/Worker";

            // Making a request to url and get response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            Log.d("url", url);
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {

                try {

                    //Get Data from Json
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    String id = jsonObject.getString(TAG_ID);
                    String name = jsonObject.getString(TAG_NAME);
                    String message = jsonObject.getString(TAG_MESSAGE);

                    //Save userid and username if success
                    if (message.equals("Success")) {
                        response = 200;
                        pref.SavePreferences("UserId", id);
                        pref.SavePreferences("UserName", name);
                        pref.SavePreferences("IsLoggedIn", "Yes");
                    } else {
                        response = 201;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                response = 201;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            //Login if success
            if (response == 200) {
                Toast.makeText(LoginActivity.this, getString(R.string.LoginSuccess), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.LoginFailed), Toast.LENGTH_SHORT).show();
            }
        }
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

        Intent i = new Intent(LoginActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.finish();
    }
}
