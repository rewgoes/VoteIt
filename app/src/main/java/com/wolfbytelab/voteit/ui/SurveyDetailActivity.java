package com.wolfbytelab.voteit.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.Constants;

public class SurveyDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        if(savedInstanceState == null) {
            String surveyKey = getIntent().getStringExtra(Constants.EXTRA_SURVEY_KEY);
            Survey.Type surveyType = (Survey.Type) getIntent().getSerializableExtra(Constants.EXTRA_SURVEY_TYPE);

            if (TextUtils.isEmpty(surveyKey)) {
                setTitle(R.string.new_survey);
            }

            FragmentManager fm = getSupportFragmentManager();

            SurveyDetailFragment surveyDetailFragment = new SurveyDetailFragment();
            surveyDetailFragment.setSurveyKeyType(surveyKey, surveyType);

            fm.beginTransaction()
                    .add(R.id.survey_detail_container, surveyDetailFragment)
                    .commit();
        }
    }
}
