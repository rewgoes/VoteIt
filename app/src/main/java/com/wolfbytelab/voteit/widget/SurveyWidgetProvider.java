package com.wolfbytelab.voteit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.MainActivity;
import com.wolfbytelab.voteit.ui.SurveyDetailActivity;

public class SurveyWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.survey_widget);

        Intent createIntent = new Intent(context, SurveyDetailActivity.class);
        PendingIntent createPendingIntent = PendingIntent.getActivity(context, 0, createIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.create_survey, createPendingIntent);

        Intent listIntent = new Intent(context, MainActivity.class);
        PendingIntent listPendingIntent = PendingIntent.getActivity(context, 1, listIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.list_survey, listPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
