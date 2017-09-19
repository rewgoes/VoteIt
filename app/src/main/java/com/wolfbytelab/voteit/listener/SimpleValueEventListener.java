package com.wolfbytelab.voteit.listener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SimpleValueEventListener implements ValueEventListener {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
