package com.wolfbytelab.voteit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wolfbytelab.voteit.R;

import java.util.HashMap;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_RETAINED_FRAGMENT = "TAG_RETAINED_FRAGMENT";
    private boolean mTwoPane;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public RetainedFragment mRetainedFragment;

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
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(TAG_RETAINED_FRAGMENT);

        // create the fragment and data the first time
        if (mRetainedFragment == null) {
            // add the fragment
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, TAG_RETAINED_FRAGMENT).commit();
        }

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

        if (isFinishing()) {
            FragmentManager fm = getSupportFragmentManager();
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            fm.beginTransaction().remove(mRetainedFragment).commit();
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

    public <T> T getState(String key) {
        //noinspection unchecked
        return (T) mRetainedFragment.map.get(key);
    }

    public void saveState(String key, Object value) {
        mRetainedFragment.map.put(key, value);
    }

    public static class RetainedFragment extends Fragment {

        HashMap<String, Object> map = new HashMap<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

    }
}
