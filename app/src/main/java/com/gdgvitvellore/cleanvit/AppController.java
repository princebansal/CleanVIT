package com.gdgvitvellore.cleanvit;

import android.app.Application;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import exam.vsrk.cleanvit.R;

public class AppController extends Application {

    public static AuthData mAuthData;
    public static Firebase mFirebaseRef;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
       /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
    }
}