package com.wolfbytelab.voteit.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PreferenceUtils {

    private static final String KEY_SURVEY_LIST = "KEY_SURVEY_LIST";

    public enum EditSurveyAction {
        ADD, REMOVE, CLEAN
    }

    synchronized public static void editSurveyList(@NonNull Context context, @NonNull EditSurveyAction action, @Nullable String surveyId) {
        Set<String> surveys = getSurveyList(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        switch (action) {
            case ADD:
                surveys.add(surveyId);
                editor.putStringSet(KEY_SURVEY_LIST, surveys);
                break;
            case REMOVE:
                surveys.remove(surveyId);
                editor.putStringSet(KEY_SURVEY_LIST, surveys);
                break;
            case CLEAN:
                editor.remove(KEY_SURVEY_LIST);
                break;
        }
        editor.apply();
    }

    public static Set<String> getSurveyList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(KEY_SURVEY_LIST, new HashSet<String>());
    }

}
