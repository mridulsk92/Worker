package com.example.mridul_xpetize.worker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.prefs.PreferenceChangeEvent;

public class LoginActivity extends AppCompatActivity {

    Button login;
    EditText username, password;
    String username_st, password_st;
    int response;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONArray tasks;

    private static String TAG_NAME = "Name";
    private static String TAG_ID = "Id";
    private static String TAG_DESIGNATION = "Designation";
    private static String TAG_USERNAME = "UserName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialise
        login = (Button) findViewById(R.id.button_login);
        username = (EditText) findViewById(R.id.editText_username);
        password = (EditText) findViewById(R.id.editText_password);
        pref = new PreferencesHelper(LoginActivity.this);

        //onClick of Login Button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username_st = username.getText().toString();
                password_st = password.getText().toString();

                new PostLogin().execute();

            }
        });
    }

    private class PostLogin extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            String designation = pref.GetPreferences("Designation");
            String url = "http://vikray.in/MyService.asmx/ExcProcedure?Para=Proc_ChkLogin&Para=" + username_st + "&Para=" + password_st + "&Para=" + designation;

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
                        String name = c.getString(TAG_NAME);
                        String username = c.getString(TAG_USERNAME);

                        if (username.equals(username_st)) {
                            response = 200;
                            pref.SavePreferences("User Id", id);
                            pref.SavePreferences("Name", name);
                            pref.SavePreferences("User Name", username);
                        }
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

            if (response == 200) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(LoginActivity.this, "Please Check Username and Password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.finish();
    }
}
