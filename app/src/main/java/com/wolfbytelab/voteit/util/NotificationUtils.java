package com.wolfbytelab.voteit.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.wolfbytelab.voteit.NotificationService;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.ui.SurveyDetailActivity;

import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    private static final String SURVEYS_NOTIFICATION_CHANNEL_ID = "survey_notification_channeid";

    private static final int NOTIFICATION_INTERVAL_MINUTES = 15;
    private static final int NOTIFICATION_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(NOTIFICATION_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = NOTIFICATION_INTERVAL_SECONDS;

    private static final String NOTIFICATION_JOB_TAG = "NOTIFICATION_JOB_TAG";

    private static boolean sInitialized;

    synchronized public static void scheduleNotificationJob(@NonNull final Context context) {
        if (sInitialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = dispatcher.newJobBuilder()
                .setService(NotificationService.class)
                .setTag(NOTIFICATION_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        NOTIFICATION_INTERVAL_SECONDS,
                        NOTIFICATION_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(constraintReminderJob);
        sInitialized = true;
    }

    synchronized public static void cancelNotificationJob(@NonNull final Context context) {
        if (!sInitialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        dispatcher.cancel(NOTIFICATION_JOB_TAG);
        sInitialized = false;
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void notifyUserAboutSurvey(Context context, Survey survey) {
        if (survey != null) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        SURVEYS_NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.surveys),
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, SURVEYS_NOTIFICATION_CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_thumbs_up_down_black_24px)
                    .setLargeIcon(largeIcon(context))
                    .setContentTitle(context.getString(R.string.new_survey))
                    .setContentText(survey.title)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(contentIntent(context, survey))
                    .setAutoCancel(true);

            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationManager.notify(survey.key.hashCode(), notificationBuilder.build());
        }
    }

    private static PendingIntent contentIntent(Context context, Survey survey) {
        Intent startActivityIntent = new Intent(context, SurveyDetailActivity.class);
        startActivityIntent.putExtra(Constants.EXTRA_SURVEY_KEY, survey.key);
        startActivityIntent.putExtra(Constants.EXTRA_SURVEY_TYPE, survey.type);
        return PendingIntent.getActivity(
                context,
                survey.key.hashCode(),
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.ic_thumbs_up_down_black_24px);
    }
}
