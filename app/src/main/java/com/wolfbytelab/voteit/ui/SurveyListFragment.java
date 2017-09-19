package com.wolfbytelab.voteit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.adapter.SurveyAdapter;
import com.wolfbytelab.voteit.listener.SimpleChildEventListener;
import com.wolfbytelab.voteit.listener.SimpleValueEventListener;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;

public class SurveyListFragment extends Fragment {

    private static final String BUNDLE_RECYCLER_LAYOUT = "bundle_recycler_layout";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private DatabaseReference mSurveyDatabaseReference;
    private SurveyAdapter mSurveyAdapter;
    private ArrayList<Survey> mSurveys;
    private Parcelable mSavedRecyclerLayoutState;
    private long surveyCount = 0;
    private ChildEventListener mSurveyPerUserListener;
    private ArrayList<ValueEventListener> mSurveysListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey_list, container, false);
        ButterKnife.bind(this, view);

        FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
        mSurveyDatabaseReference = firebaseDatabase.getReference();

        mSurveys = new ArrayList<>();

        mSurveyAdapter = new SurveyAdapter(mSurveys);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mSurveyAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mSavedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSurvey();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSurveyPerUserListener != null) {
            mSurveyDatabaseReference.removeEventListener(mSurveyPerUserListener);
        }

        if (mSurveysListener != null) {
            for (ValueEventListener surveyListener : mSurveysListener) {
                mSurveyDatabaseReference.removeEventListener(surveyListener);
            }
        }
    }

    @OnClick(R.id.add_survey)
    public void addSurvey() {
        Intent intent = new Intent(getContext(), AddSurveyActivity.class);
        startActivity(intent);
    }

    private void fetchSurvey() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            mSurveysListener = new ArrayList<>();

            mSurveyDatabaseReference.child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).addValueEventListener(new SimpleValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    surveyCount = dataSnapshot.getChildrenCount();
                }
            });
            mSurveyPerUserListener = mSurveyDatabaseReference.child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).addChildEventListener(new SimpleChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot surveyKeySnapshot, String s) {
                    Bundle extras = new Bundle();
                    extras.putBoolean("isMember", (boolean) surveyKeySnapshot.getValue());

                    SimpleValueEventListener simpleValueEventListener = new SimpleValueEventListener(extras) {
                        @Override
                        public void onDataChange(DataSnapshot surveySnapshot) {
                            surveyCount--;
                            Survey survey = surveySnapshot.getValue(Survey.class);

                            if (survey != null) {
                                survey.key = surveySnapshot.getKey();

                                boolean isMember = false;
                                if (mExtras != null) {
                                    isMember = mExtras.getBoolean("isMember");
                                }

                                if (isMember) {
                                    if (TextUtils.equals(survey.owner, firebaseUser.getUid())) {
                                        survey.type = Survey.Type.OWNER;
                                    } else {
                                        survey.type = Survey.Type.MEMBER;
                                    }
                                } else {
                                    survey.type = Survey.Type.INVITE;
                                }

                                int position = mSurveys.indexOf(survey);

                                if (position != -1) {
                                    mSurveys.remove(position);
                                    mSurveys.add(position, survey);
                                    mSurveyAdapter.notifyItemChanged(position);
                                } else {
                                    mSurveys.add(survey);
                                    if (surveyCount == 0) {
                                        mSurveyAdapter.notifyDataSetChanged();
                                        if (mSavedRecyclerLayoutState != null) {
                                            mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
                                            mSavedRecyclerLayoutState = null;
                                        }
                                    }
                                }
                            } else {
                                survey = new Survey();
                                survey.key = surveySnapshot.getKey();
                                int position = mSurveys.indexOf(survey);
                                if (position != -1) {
                                    mSurveys.remove(position);
                                    mSurveyAdapter.notifyItemRemoved(position);
                                }
                            }
                        }
                    };

                    mSurveysListener.add(simpleValueEventListener);
                    mSurveyDatabaseReference.child(SURVEYS_KEY).child(surveyKeySnapshot.getKey()).addValueEventListener(simpleValueEventListener);
                }

                @Override
                public void onChildRemoved(DataSnapshot surveySnapshot) {
                    Survey survey = new Survey();
                    survey.key = surveySnapshot.getKey();
                    int position = mSurveys.indexOf(survey);
                    if (position != -1) {
                        mSurveys.remove(position);
                        mSurveyAdapter.notifyItemRemoved(position);
                    }
                }
            });
        }
    }
}
