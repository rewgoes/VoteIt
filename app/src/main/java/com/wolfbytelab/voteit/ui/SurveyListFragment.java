package com.wolfbytelab.voteit.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.adapter.SurveyAdapter;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.model.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurveyListFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey_list, container, false);
        ButterKnife.bind(this, view);

        ArrayList<Survey> surveys = new ArrayList<>();

        //START DUMMY DATA
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = new User();

        if (firebaseUser != null) {
            user.name = firebaseUser.getDisplayName();
            user.email = firebaseUser.getEmail();
        } else {
            user.name = "Name";
            user.email = "name@provider.com";
        }

        Survey survey = new Survey();
        survey.owner = user;
        survey.startDate = System.currentTimeMillis();
        survey.title = "My Survey";
        survey.description = "This is a sample survey";

        surveys.add(survey);
        surveys.add(survey);
        surveys.add(survey);
        surveys.add(survey);
        surveys.add(survey);
        surveys.add(survey);
        surveys.add(survey);
        //END DUMMY DATA

        SurveyAdapter surveyAdapter = new SurveyAdapter(surveys, getContext());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(surveyAdapter);

        return view;
    }
}
