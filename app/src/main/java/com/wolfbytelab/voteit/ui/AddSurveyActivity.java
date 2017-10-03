package com.wolfbytelab.voteit.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Member;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.ui.editor.SectionView;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.wolfbytelab.voteit.util.FirebaseUtils.MEMBERS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;

public class AddSurveyActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.title)
    EditText mTitle;
    @BindView(R.id.title_input_layout)
    TextInputLayout mTitleInputLayout;
    @BindView(R.id.description)
    EditText mDescription;
    @BindView(R.id.members)
    SectionView mMembersLayout;
    @BindView(R.id.end_date_picker)
    EditText mDatePickerView;
    @BindView(R.id.end_time_picker)
    EditText mTimePickerView;

    private ArrayList<Member> mMembers;
    private int mEndDay;
    private int mEndMonth;
    private int mEndYear;
    private int mEndHour;
    private int mEndMinute;
    private boolean mHasTimePickerShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_survey);
        ButterKnife.bind(this);

        mTitle.addTextChangedListener(new RequiredFieldTextWatcher(mTitleInputLayout));
        mTitle.setOnFocusChangeListener(new RequiredFieldFocusChangeListener(mTitleInputLayout));

        if (savedInstanceState == null) {
            mMembersLayout.addEditorView(new Member());
        }


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

    @OnClick(R.id.end_date_picker)
    private void showDatePicker() {
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(this, this, mEndYear, mEndMonth, mEndDay);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mEndYear = year;
        mEndMonth = month;
        mEndDay = dayOfMonth;

        if (!mHasTimePickerShown) {
            showTimePicker();
        }
    }

    @OnClick(R.id.end_time_picker)
    private void showTimePicker() {
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(this, this, mEndHour, mEndMinute, false);
        timePickerDialog .show();
        mHasTimePickerShown = true;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mEndHour = hourOfDay;
        mEndMinute = minute;
    }

    private boolean isDataValid() {
        boolean isDataValid = true;

        if (mTitle.length() == 0) {
            mTitleInputLayout.setError(getString(R.string.fui_required_field));
            isDataValid = false;
        }

        //noinspection unchecked
        mMembers = (ArrayList<Member>) mMembersLayout.getData();
        if (mMembers == null) {
            isDataValid = false;
        }

        return isDataValid;
    }

    private void createSurvey() {
        if (isDataValid()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) { //user is logged in
                FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
                DatabaseReference surveyDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_KEY);
                DatabaseReference userDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail()));

                Survey survey = new Survey();
                survey.title = mTitle.getText().toString();
                survey.description = mDescription.getText().toString();
                survey.owner = firebaseUser.getUid();

                String surveyKey = surveyDatabaseReference.push().getKey();

                DatabaseReference memberDatabaseReference = firebaseDatabase.getReference().child(MEMBERS_KEY).child(surveyKey);

                surveyDatabaseReference.child(surveyKey).setValue(survey);
                userDatabaseReference.child(surveyKey).setValue(true);
                memberDatabaseReference.child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).setValue(true);

                for (Member member : mMembers) {
                    if (!TextUtils.isEmpty(member.getEmail())) {
                        String encodedEmail = FirebaseUtils.encodeAsFirebaseKey(member.getEmail());
                        //TODO: use Uri.decode(encodedEmail) in order to present this value
                        memberDatabaseReference.child(encodedEmail).setValue(false);

                        DatabaseReference invitePerUserDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(encodedEmail);
                        invitePerUserDatabaseReference.child(surveyKey).setValue(false);
                    }
                }

                finish();
            }
        }
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
