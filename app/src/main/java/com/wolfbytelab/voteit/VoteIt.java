package com.wolfbytelab.voteit;

import android.app.Application;

import timber.log.Timber;

public class VoteIt extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
