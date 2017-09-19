package com.wolfbytelab.voteit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.adapter.SurveyAdapter;
import com.wolfbytelab.voteit.listener.SimpleChildEventListener;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;

public class SurveyListFragment extends Fragment {

    private static final String STATE_SURVEYS = "state_surveys";
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSurveyDatabaseReference;
    private SurveyAdapter mSurveyAdapter;
    private ArrayList<Survey> mSurveys;
    private ChildEventListener mSurveyChildEventListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey_list, container, false);
        ButterKnife.bind(this, view);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mFirebaseDatabase = FirebaseUtils.getDatabase();
        mSurveyDatabaseReference = mFirebaseDatabase.getReference();

        if (savedInstanceState != null) {
            mSurveys = savedInstanceState.getParcelableArrayList(STATE_SURVEYS);
        } else {
            mSurveys = new ArrayList<>();
        }

        mSurveyAdapter = new SurveyAdapter(mSurveys, getContext());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mSurveyAdapter);

        mSurveyChildEventListener = mSurveyDatabaseReference.child(SURVEYS_PER_USER_KEY).child(firebaseUser.getUid()).addChildEventListener(new SimpleChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // TODO: addValueEventListener for the items in the adapter
                // Instead of creating a new survey in the list, update the one already in the list
                mSurveyDatabaseReference.child(SURVEYS_KEY).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Survey survey = dataSnapshot.getValue(Survey.class);
                        survey.key = dataSnapshot.getKey();
                        if (TextUtils.equals(survey.owner, firebaseUser.getUid())) {
                            survey.type = Survey.Type.OWNER;
                        } else {
                            survey.type = Survey.Type.MEMBER;
                        }

                        int position = mSurveys.indexOf(survey);
                        if (position == -1) { // does not contain item
                            mSurveyAdapter.addSurvey(survey);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSurveyChildEventListener != null) {
            mSurveyDatabaseReference.removeEventListener(mSurveyChildEventListener);
        }
    }

    @OnClick(R.id.add_survey)
    public void addSurvey() {
        Intent intent = new Intent(getContext(), AddSurveyActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_SURVEYS, mSurveys);
    }
}
