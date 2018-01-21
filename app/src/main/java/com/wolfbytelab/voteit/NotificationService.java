package com.wolfbytelab.voteit;

import android.text.TextUtils;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolfbytelab.voteit.listener.SimpleValueEventListener;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.FirebaseUtils;
import com.wolfbytelab.voteit.util.NotificationUtils;
import com.wolfbytelab.voteit.util.PreferenceUtils;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.ADD;

public class NotificationService extends JobService {

    private SimpleValueEventListener valueEventListener;
    FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
    DatabaseReference databaseReference;

    @Override
    public boolean onStartJob(final JobParameters job) {
        Timber.d("notify: onStartJob");
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            databaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail()));
            valueEventListener = new SimpleValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Object> surveysValues = (HashMap<String, Object>) dataSnapshot.getValue();

                    if (surveysValues != null) {
                        final Set<String> surveys = surveysValues.keySet();
                        Set<String> oldSurveys = PreferenceUtils.getSurveyList(NotificationService.this);
                        surveys.removeAll(oldSurveys);

                        if (surveys.size() == 0) {
                            jobFinished(job, false);
                        }

                        for (String surveyKey : surveys) {
                            Timber.d("notify: New survey: " + surveyKey);
                            PreferenceUtils.editSurveyList(NotificationService.this, ADD, surveyKey);
                            firebaseDatabase.getReference().child(SURVEYS_KEY).child(surveyKey).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    HashMap<String, Object> surveyValues = (HashMap<String, Object>) dataSnapshot.getValue();
                                    Survey survey = new Survey();
                                    survey.key = dataSnapshot.getKey();
                                    survey.title = (String) surveyValues.get("title");
                                    survey.owner = (String) surveyValues.get("owner");
                                    survey.type = TextUtils.equals(survey.owner, firebaseUser.getUid()) ? Survey.Type.OWNER : Survey.Type.MEMBER;
                                    if (survey != null) {
                                        NotificationUtils.notifyUserAboutSurvey(NotificationService.this, survey);
                                    }
                                    surveys.remove(survey.key);
                                    if (surveys.size() == 0) {
                                        jobFinished(job, false);
                                    }
                                }
                            });
                        }
                    } else {
                        jobFinished(job, false);
                    }
                }
            };
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(valueEventListener);
        } else {
            jobFinished(job, false);
            NotificationUtils.cancelNotificationJob(this);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (databaseReference != null) {
            if (valueEventListener != null) {
                databaseReference.removeEventListener(valueEventListener);
                valueEventListener = null;
            }
        }
        return true;
    }
}
