package com.example.mridul_xpetize.worker;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Mridul-Xpetize on 09/16/16.
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String recent_token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TOKEN",recent_token);
        PreferencesHelper pref = new PreferencesHelper(getApplicationContext());
        pref.SavePreferences("FCM TOKEN", recent_token);
    }
}
