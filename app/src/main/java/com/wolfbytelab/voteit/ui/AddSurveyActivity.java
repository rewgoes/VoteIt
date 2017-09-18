package com.wolfbytelab.voteit.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.adapter.MemberAdapter;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wolfbytelab.voteit.util.FirebaseUtils.INVITES_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.INVITES_PER_USER_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.MEMBERS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;

public class AddSurveyActivity extends AppCompatActivity implements MemberAdapter.RemoveItemListener {

    private static final String STATE_MEMBERS = "state_members";

    @BindView(R.id.title)
    EditText mTitle;
    @BindView(R.id.title_input_layout)
    TextInputLayout mTitleInputLayout;
    @BindView(R.id.description)
    EditText mDescription;
    @BindView(R.id.members_view)
    RecyclerView mMembersView;

    private ArrayList<String> mMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_survey);
        ButterKnife.bind(this);

        mTitle.addTextChangedListener(new RequiredFieldTextWatcher(mTitleInputLayout));
        mTitle.setOnFocusChangeListener(new RequiredFieldFocusChangeListener(mTitleInputLayout));

        if (savedInstanceState != null) {
            mMembers = savedInstanceState.getStringArrayList(STATE_MEMBERS);
        } else {
            mMembers = new ArrayList<>();
            mMembers.add("");
        }

        MemberAdapter memberAdapter = new MemberAdapter(mMembers, this);

        mMembersView.setLayoutManager(new LinearLayoutManager(this));
        mMembersView.setHasFixedSize(true);
        mMembersView.setAdapter(memberAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_MEMBERS, mMembers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_menu:
                createSurvey();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isDataValid() {
        boolean isDataValid = true;

        if (mTitle.length() == 0) {
            mTitleInputLayout.setError(getString(R.string.fui_required_field));
            isDataValid = false;
        }

        for (String member : mMembers) {
            if (!TextUtils.isEmpty(member)) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(member).matches()) {
                    //TODO: show error message
                    Toast.makeText(this, member, Toast.LENGTH_SHORT).show();
                    isDataValid = false;
                }
            }
        }

        return isDataValid;
    }

    private void createSurvey() {
        if (isDataValid()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) { //user is logged in
                FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
                DatabaseReference surveyDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_KEY);
                DatabaseReference userDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(firebaseUser.getUid());

                Survey survey = new Survey();
                survey.title = mTitle.getText().toString();
                survey.description = mDescription.getText().toString();
                survey.owner = firebaseUser.getUid();

                String surveyKey = surveyDatabaseReference.push().getKey();

                DatabaseReference memberDatabaseReference = firebaseDatabase.getReference().child(MEMBERS_KEY).child(surveyKey);

                surveyDatabaseReference.child(surveyKey).setValue(survey);
                userDatabaseReference.child(surveyKey).setValue(true);
                memberDatabaseReference.child(firebaseUser.getUid()).setValue(true);

                //invite members
                for (String member : mMembers) {
                    if (!TextUtils.isEmpty(member)) {
                        String encodedEmail = FirebaseUtils.encodeAsFirebaseKey(member);
                        //TODO: use Uri.decode(encodedEmail) in order to present this value
                        DatabaseReference inviteDatabaseReference = firebaseDatabase.getReference().child(INVITES_KEY).child(surveyKey);
                        inviteDatabaseReference.child(encodedEmail).setValue(member);

                        DatabaseReference invitePerUserDatabaseReference = firebaseDatabase.getReference().child(INVITES_PER_USER_KEY).child(encodedEmail);
                        invitePerUserDatabaseReference.child(surveyKey).setValue(true);
                    }
                }

                finish();
            }
        }
    }

    @Override
    public void onRemoveItemClicked(int position) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMembersView.requestLayout();
            }
        }, 100);
    }

    private class RequiredFieldTextWatcher implements TextWatcher {

        TextInputLayout mTextInputLayout;

        RequiredFieldTextWatcher(TextInputLayout textInputLayout) {
            mTextInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mTextInputLayout.setErrorEnabled(false);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class RequiredFieldFocusChangeListener implements View.OnFocusChangeListener {

        TextInputLayout mTextInputLayout;

        RequiredFieldFocusChangeListener(TextInputLayout textInputLayout) {
            mTextInputLayout = textInputLayout;
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                if (view instanceof EditText) {
                    if (((EditText) view).getText().length() == 0) {
                        mTextInputLayout.setError(getString(R.string.fui_required_field));
                    }
                }
            }
        }
    }
}
