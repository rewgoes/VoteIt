package com.wolfbytelab.voteit.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.wolfbytelab.voteit.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.ADD;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.CLEAN;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.REMOVE;

public class SurveyListFragment extends Fragment implements SurveyAdapter.OnItemClickListener {

    OnSurveyClickListener mCallback;
    private Unbinder mUnbinder;
    private DatabaseReference mSurveyPerUserDataRef;
    private ArrayList<DatabaseReference> mSurveysDataRefs;

    interface OnSurveyClickListener {
        void onSurveySelected(String surveyKey, Survey.Type surveyType);

        void addSurvey();
    }

    private static final String BUNDLE_RECYCLER_LAYOUT = "bundle_recycler_layout";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.add_survey_fab)
    FloatingActionButton mAddSurveyFab;
    @BindView(R.id.empty_layout)
    View mEmptyLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private DatabaseReference mDatabaseReference;
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
        mUnbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
        mDatabaseReference = firebaseDatabase.getReference();

        mSurveys = new ArrayList<>();

        mSurveyAdapter = new SurveyAdapter(mSurveys);
        mSurveyAdapter.setOnItemClickListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mSurveyAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mSurveyAdapter = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnSurveyClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnImageClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
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
        Timber.d("onPause");
        if (mSurveyPerUserDataRef != null && mSurveyPerUserListener != null) {
            mSurveyPerUserDataRef.removeEventListener(mSurveyPerUserListener);
            mSurveyPerUserListener = null;
            mSurveyPerUserDataRef = null;
        }

        if (mSurveysDataRefs != null && mSurveysListener != null && mSurveysListener.size() == mSurveysDataRefs.size()) {
            for (int i = 0; i < mSurveysDataRefs.size(); i++) {
                mSurveysDataRefs.get(i).removeEventListener(mSurveysListener.get(i));
            }
            mSurveysListener = null;
            mSurveysDataRefs = null;
        }
    }

    @Optional
    @OnClick({R.id.add_survey_fab, R.id.add_survey_button})
    public void addSurvey() {
        mCallback.addSurvey();
    }

    private void fetchSurvey() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            mSurveysListener = new ArrayList<>();
            mSurveysDataRefs = new ArrayList<>();

            mDatabaseReference.child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    surveyCount = dataSnapshot.getChildrenCount();
                    initView();

                    if (surveyCount == 0) {
                        PreferenceUtils.editSurveyList(getContext(), CLEAN, null);
                    }
                }
            });

            final HashSet<Integer> surveyIndexes = new HashSet<>();
            if (mSurveys != null) {
                for (int i = 0; i < mSurveys.size(); i++) {
                    surveyIndexes.add(i);
                }
            }

            final Set<String> oldSurveyList = new HashSet<>(PreferenceUtils.getSurveyList(getContext()));

            mSurveyPerUserDataRef = mDatabaseReference.child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail()));
            mSurveyPerUserListener = mSurveyPerUserDataRef.addChildEventListener(new SimpleChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot surveyKeySnapshot, String s) {
                    Bundle extras = new Bundle();
                    Object answerValue = surveyKeySnapshot.getValue();
                    if (answerValue instanceof String) {
                        extras.putString("answerId", (String) surveyKeySnapshot.getValue());
                    }

                    if (!oldSurveyList.contains(surveyKeySnapshot.getKey())) {
                        PreferenceUtils.editSurveyList(getContext(), ADD, surveyKeySnapshot.getKey());
                    } else {
                        oldSurveyList.remove(surveyKeySnapshot.getKey());
                    }

                    SimpleValueEventListener simpleValueEventListener = new SimpleValueEventListener(extras) {
                        @Override
                        public void onDataChange(DataSnapshot surveySnapshot) {
                            surveyCount--;
                            Survey survey = surveySnapshot.getValue(Survey.class);

                            if (survey != null) {
                                survey.key = surveySnapshot.getKey();

                                if (mExtras != null) {
                                    survey.answer = mExtras.getString("answerId");
                                }

                                if (TextUtils.equals(survey.owner, firebaseUser.getUid())) {
                                    survey.type = Survey.Type.OWNER;
                                } else {
                                    survey.type = Survey.Type.MEMBER;
                                }

                                int position = mSurveys.indexOf(survey);

                                if (position != -1) {
                                    surveyIndexes.remove(position);
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
                                    } else if (surveyCount < 0) {
                                        mSurveyAdapter.notifyItemInserted(mSurveys.size() - 1);
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
                                if (oldSurveyList.contains(survey.key)) {
                                    PreferenceUtils.editSurveyList(getContext(), REMOVE, survey.key);
                                    oldSurveyList.remove(survey.key);
                                }
                            }

                            if (surveyCount == 0) {
                                for (int index : surveyIndexes) {
                                    if (mSurveys != null && mSurveys.size() > index) {
                                        mSurveys.remove(index);
                                        mSurveyAdapter.notifyItemRemoved(index);
                                    }
                                }

                                for (String oldSurveyId : oldSurveyList) {
                                    PreferenceUtils.editSurveyList(getContext(), REMOVE, oldSurveyId);
                                }
                            }
                        }
                    };

                    DatabaseReference surveyDataRef = mDatabaseReference.child(SURVEYS_KEY).child(surveyKeySnapshot.getKey());
                    mSurveysListener.add(simpleValueEventListener);
                    mSurveysDataRefs.add(surveyDataRef);
                    surveyDataRef.addValueEventListener(simpleValueEventListener);
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
                    if (mSurveys.size() == 0) {
                        surveyCount = 0;
                        initView();
                    }

                    if (oldSurveyList.contains(survey.key)) {
                        PreferenceUtils.editSurveyList(getContext(), REMOVE, survey.key);
                    }
                }
            });
        }
    }

    private void initView() {
        if (surveyCount == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mAddSurveyFab.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mAddSurveyFab.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(int position) {
        Survey survey = mSurveyAdapter.getItem(position);
        mCallback.onSurveySelected(survey.key, survey.type);
    }
}
