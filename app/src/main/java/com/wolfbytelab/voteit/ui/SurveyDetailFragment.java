package com.wolfbytelab.voteit.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wolfbytelab.voteit.R;

public class SurveyDetailFragment extends Fragment {

    private String mKey;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_survey, container, false);
    }

    public void setKey(String key) {
        mKey = key;
    }
}
