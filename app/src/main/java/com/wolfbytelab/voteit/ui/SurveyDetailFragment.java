package com.wolfbytelab.voteit.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Member;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.ui.editor.SectionView;
import com.wolfbytelab.voteit.util.DateUtils;
import com.wolfbytelab.voteit.util.FirebaseUtils;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.wolfbytelab.voteit.util.FirebaseUtils.MEMBERS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;

public class SurveyDetailFragment extends Fragment implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String STATE_SURVEY_KEY = "state_survey_key";
    private static final String STATE_TIME_PICKER_SHOWN = "state_time_picker_shown";
    private static final String STATE_END_DATE = "state_end_date";

    private String mKey;

    @BindView(R.id.title)
    EditText mTitle;
    @BindView(R.id.title_input_layout)
    TextInputLayout mTitleInputLayout;
    @BindView(R.id.description)
    EditText mDescription;
    @BindView(R.id.members)
    SectionView mMembersLayout;
    @BindView(R.id.end_date_input_layout)
    TextInputLayout mDateInputLayout;
    @BindView(R.id.end_date_picker)
    EditText mDatePickerView;
    @BindView(R.id.end_time_input_layout)
    TextInputLayout mTimeInputLayout;
    @BindView(R.id.end_time_picker)
    EditText mTimePickerView;
    @BindView(R.id.clear_date)
    TextView mClearDateView;
    @BindView(R.id.focus_holder)
    View mFocusHolder;

    private ArrayList<Member> mMembers;

    private boolean mHasTimePickerShown = false;
    private long mEndDate = DateUtils.DATE_NOT_SET;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_survey_detail, container, false);

        setHasOptionsMenu(true);

        mUnbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState == null) {
            mMembersLayout.addEditorView(new Member());
        } else {
            mKey = savedInstanceState.getString(STATE_SURVEY_KEY);
            mHasTimePickerShown = savedInstanceState.getBoolean(STATE_TIME_PICKER_SHOWN, false);
            mEndDate = savedInstanceState.getLong(STATE_END_DATE, DateUtils.DATE_NOT_SET);
        }

        initView();

        mTitle.addTextChangedListener(new RequiredFieldTextWatcher(mTitleInputLayout));
        mTitle.setOnFocusChangeListener(new RequiredFieldFocusChangeListener(mTitleInputLayout));

        Calendar calendar = Calendar.getInstance();
        DateUtils.startCalendar(calendar, mEndDate);

        if (mEndDate != DateUtils.DATE_NOT_SET) {
            mClearDateView.setVisibility(View.VISIBLE);
        }

        mDatePickerDialog =
                new DatePickerDialog(getContext(), this, calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        mDatePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        mTimePickerDialog =
                new TimePickerDialog(getContext(), this, calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getContext()));

        return rootView;
    }

    private void initView() {
        if (isEditMode()) {
            mTitle.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SURVEY_KEY, mKey);
        outState.putBoolean(STATE_TIME_PICKER_SHOWN, mHasTimePickerShown);
        outState.putLong(STATE_END_DATE, mEndDate);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isEditMode()) {
            inflater.inflate(R.menu.add_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
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

    public void setKey(String key) {
        mKey = key;
    }

    private boolean isEditMode() {
        return !TextUtils.isEmpty(mKey);
    }

    @OnClick(R.id.end_date_picker)
    protected void showDatePicker() {
        mDatePickerDialog.show();
        mFocusHolder.requestFocus();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        DateUtils.startCalendar(calendar, mEndDate);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        mEndDate = calendar.getTimeInMillis();

        fillDate();

        if (!mHasTimePickerShown) {
            showTimePicker();
        }
    }

    @OnClick(R.id.end_time_picker)
    protected void showTimePicker() {
        mTimePickerDialog.show();
        mFocusHolder.requestFocus();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHasTimePickerShown = true;
        Calendar calendar = Calendar.getInstance();
        DateUtils.startCalendar(calendar, mEndDate);

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        mEndDate = calendar.getTimeInMillis();

        fillDate();
    }

    @OnClick(R.id.clear_date)
    protected void clearDate() {
        mEndDate = DateUtils.DATE_NOT_SET;
        mHasTimePickerShown = false;
        mClearDateView.setVisibility(View.INVISIBLE);
        mDateInputLayout.setErrorEnabled(false);
        mTimeInputLayout.setErrorEnabled(false);
        mDatePickerView.setText("");
        mTimePickerView.setText("");

        Calendar calendar = Calendar.getInstance();
        DateUtils.startCalendar(calendar, mEndDate);
        mDatePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mTimePickerDialog.updateTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private void fillDate() {
        mDatePickerView.setText(DateUtils.getFormattedDate(getContext(), mEndDate, false));
        mTimePickerView.setText(DateUtils.getFormattedTime(getContext(), mEndDate));
        mClearDateView.setVisibility(View.VISIBLE);
        mDateInputLayout.setErrorEnabled(false);
        mTimeInputLayout.setErrorEnabled(false);
    }

    private boolean isDataValid() {
        boolean isDataValid = true;

        if (mEndDate != DateUtils.DATE_NOT_SET) {
            if (mEndDate <= Calendar.getInstance().getTimeInMillis()) {
                mDateInputLayout.setError(getString(R.string.invalid_date));
                mTimeInputLayout.setError(" ");
                isDataValid = false;
            }
        }

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
                //TODO: get date from firebase server and also change it in isDataValid()
                survey.startDate = Calendar.getInstance().getTimeInMillis();
                if (mEndDate != DateUtils.DATE_NOT_SET) {
                    survey.endDate = mEndDate;
                }

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

                //noinspection StatementWithEmptyBody
                if (getActivity() instanceof SurveyDetailActivity) {
                    getActivity().finish();
                } else {
                    //TODO: switch to edit mode
                }
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
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!TextUtils.isEmpty(charSequence)) {
                mTextInputLayout.setErrorEnabled(false);
            }
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
