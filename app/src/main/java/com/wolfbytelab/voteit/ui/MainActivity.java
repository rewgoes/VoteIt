package com.wolfbytelab.voteit.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.wolfbytelab.voteit.R;

public class MainActivity extends AppCompatActivity {

    private boolean mTwoPane;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.survey_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                FragmentManager fragmentManager = getSupportFragmentManager();

                SurveyDetailFragment surveyDetailFragment = new SurveyDetailFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.survey_detail_container, surveyDetailFragment)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }
}
