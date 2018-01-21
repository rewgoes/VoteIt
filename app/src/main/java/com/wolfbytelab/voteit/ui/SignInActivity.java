package com.wolfbytelab.voteit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.wolfbytelab.voteit.BuildConfig;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.wolfbytelab.voteit.util.FirebaseUtils.EMAIL_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.ENCODED_EMAIL_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.NAME_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.USERS_KEY;

public class SignInActivity extends AppCompatActivity {

    final private int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Timber.d("onAuthStateChanged:signed_in: %s", user.getUid());
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Timber.d("onAuthStateChanged:signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                writeUser();

                Toast.makeText(this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } else {
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    return;
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.unknown_sign_in_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, R.string.unknown_sign_in_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, String> user = new HashMap<>();
        user.put(NAME_KEY, firebaseUser.getDisplayName());
        user.put(EMAIL_KEY, firebaseUser.getEmail());
        user.put(ENCODED_EMAIL_KEY, FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail()));

        DatabaseReference database = FirebaseUtils.getDatabase().getReference();
        database.child(USERS_KEY).child(firebaseUser.getUid()).setValue(user);
    }
}
