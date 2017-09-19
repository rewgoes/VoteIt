package com.wolfbytelab.voteit.listener;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SimpleValueEventListener implements ValueEventListener {
    protected Bundle mExtras;

    public SimpleValueEventListener() {
    }

    public SimpleValueEventListener(Bundle extras) {
        mExtras = extras;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
