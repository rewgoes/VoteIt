package com.wolfbytelab.voteit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.util.Constants;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SurveyListFragment.OnSurveyClickListener {

    private boolean mTwoPane;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Timber.d("onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            fm.beginTransaction()
                    .add(R.id.survey_list_container, new SurveyListFragment())
                    .commit();
        }

        if (findViewById(R.id.survey_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                SurveyDetailFragment surveyDetailFragment = new SurveyDetailFragment();
                fm.beginTransaction()
                        .add(R.id.survey_detail_container, surveyDetailFragment)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSurveySelected(String surveyKey) {
        if (mTwoPane) {
            FragmentManager fm = getSupportFragmentManager();

            SurveyDetailFragment surveyDetailFragment = new SurveyDetailFragment();
            surveyDetailFragment.setKey(surveyKey);

            fm.beginTransaction()
                    .replace(R.id.survey_detail_container, surveyDetailFragment)
                    .commit();
        } else {
            final Intent intent = new Intent(this, SurveyDetailActivity.class);
            intent.putExtra(Constants.EXTRA_SURVEY_KEY, surveyKey);
            startActivity(intent);
        }
    }
}
