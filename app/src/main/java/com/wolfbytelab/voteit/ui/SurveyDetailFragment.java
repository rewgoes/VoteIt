package com.wolfbytelab.voteit.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.listener.SimpleValueEventListener;
import com.wolfbytelab.voteit.model.Member;
import com.wolfbytelab.voteit.model.Question;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.ui.editor.SectionView;
import com.wolfbytelab.voteit.util.Constants;
import com.wolfbytelab.voteit.util.DateUtils;
import com.wolfbytelab.voteit.util.FirebaseUtils;
import com.wolfbytelab.voteit.util.PreferenceUtils;
import com.wolfbytelab.voteit.util.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.wolfbytelab.voteit.util.FirebaseUtils.ANSWERS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.MEMBERS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_KEY;
import static com.wolfbytelab.voteit.util.FirebaseUtils.SURVEYS_PER_USER_KEY;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.ADD;
import static com.wolfbytelab.voteit.util.PreferenceUtils.EditSurveyAction.REMOVE;

public class SurveyDetailFragment extends Fragment implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    private static final String STATE_SURVEY_KEY = "state_survey_key";
    private static final String STATE_TIME_PICKER_SHOWN = "state_time_picker_shown";
    private static final String STATE_END_DATE = "state_end_date";
    private static final String STATE_SURVEY_TYPE = "state_survey_type";
    private static final String STATE_SURVEY_MEMBERS = "state_survey_members";
    private long membersCount;

    interface OnSurveyChangedListener {
        void onSurveyCreated(String surveyKey);

        void onSurveyDeleted();

        void onSurveyVoted(String surveyKey, Survey.Type mSurveyType);
    }

    private OnSurveyChangedListener mOnSurveyChangedListener;
    private String mSurveyKey;
    private Survey.Type mSurveyType;
    private String mAnswer;

    @BindView(R.id.survey_detail)
    View mSurveyDetailView;
    @BindView(R.id.progress_bar)
    View mProgressBarView;

    @BindView(R.id.title)
    TextInputEditText mTitle;
    @BindView(R.id.title_input_layout)
    TextInputLayout mTitleInputLayout;
    @BindView(R.id.description)
    TextInputEditText mDescription;
    @BindView(R.id.not_editable_members)
    SectionView mNotEditableMembersLayout;
    @BindView(R.id.members)
    SectionView mMembersLayout;
    @BindView(R.id.end_date_input_layout)
    TextInputLayout mDateInputLayout;
    @BindView(R.id.end_date_picker)
    TextInputEditText mDatePickerView;
    @BindView(R.id.end_time_input_layout)
    TextInputLayout mTimeInputLayout;
    @BindView(R.id.end_time_picker)
    TextInputEditText mTimePickerView;
    @BindView(R.id.clear_date)
    TextView mClearDateView;
    @BindView(R.id.focus_holder)
    View mFocusHolder;
    @BindView(R.id.date_time_layout)
    View mDateTimeLayoutGroup;
    @BindView(R.id.description_input_layout)
    TextInputLayout mDescriptionInputLayout;
    @BindView(R.id.questions)
    SectionView mQuestionsLayout;
    @BindView(R.id.add_question)
    View mAddQuestionView;

    private ArrayList<Member> mMembers;
    private ArrayList<Question> mQuestions;

    private boolean mHasTimePickerShown = false;
    private long mEndDate = DateUtils.DATE_NOT_SET;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private Unbinder mUnbinder;
    private DatabaseReference mSurveyDatabaseReference;

    private Survey mSurvey;

    private HashMap<String, Boolean> mSurveyMembers = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_survey_detail, container, false);

        setHasOptionsMenu(true);

        mUnbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState == null) {
            if (isAddMode()) {
                mMembersLayout.addEditorView(new Member());
                mQuestionsLayout.addEditorView(new Question());
            } else {
                mFocusHolder.requestFocus();
            }
        } else {
            mSurveyKey = savedInstanceState.getString(STATE_SURVEY_KEY);
            mSurveyType = (Survey.Type) savedInstanceState.getSerializable(STATE_SURVEY_TYPE);
            mHasTimePickerShown = savedInstanceState.getBoolean(STATE_TIME_PICKER_SHOWN, false);
            mEndDate = savedInstanceState.getLong(STATE_END_DATE, DateUtils.DATE_NOT_SET);
            mSurveyMembers = (HashMap<String, Boolean>) savedInstanceState.getSerializable(STATE_SURVEY_MEMBERS);
        }

        mTitle.addTextChangedListener(new RequiredFieldTextWatcher(mTitleInputLayout));
        mTitle.setOnFocusChangeListener(new RequiredFieldFocusChangeListener(mTitleInputLayout));

        if (isAddMode()) {
            initView();
        } else {
            disableInputLayoutAnimation();
            mNotEditableMembersLayout.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void disableInputLayoutAnimation() {
        mTitleInputLayout.setHintAnimationEnabled(false);
        mDateInputLayout.setHintAnimationEnabled(false);
        mTimeInputLayout.setHintAnimationEnabled(false);
        mDescriptionInputLayout.setHintAnimationEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAddMode()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
                mSurveyDatabaseReference = firebaseDatabase.getReference();

                // read user survey
                mSurveyDatabaseReference.child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).child(mSurveyKey).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.getValue() instanceof String) {
                                // check if survey was answered by current user
                                mAnswer = (String) dataSnapshot.getValue();
                            }
                        } else {
                            initView();
                        }

                        // read survey data
                        mSurveyDatabaseReference.child(SURVEYS_KEY).child(mSurveyKey).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot surveySnapshot) {
                                mSurvey = surveySnapshot.getValue(Survey.class);

                                if (mSurvey != null) {
                                    mSurvey.key = surveySnapshot.getKey();

                                    initMemberLayout();

                                    boolean isEnded = mSurvey.members != null && mSurvey.answers != null && mSurvey.members.size() == mSurvey.answers.size();
                                    if (isEnded) {
                                        mSurveyDatabaseReference.child(ANSWERS_KEY).child(mSurveyKey).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, ArrayList<Long>> answers = (HashMap<String, ArrayList<Long>>) dataSnapshot.getValue();
                                                ArrayList<int[]> answerCount = new ArrayList<>();

                                                for (Question question : mSurvey.questions) {
                                                    answerCount.add(new int[question.getOptions().size()]);
                                                }

                                                int[] mostAnsweredCount = new int[mSurvey.questions.size()];

                                                if (answers != null) {
                                                    for (Map.Entry<String, ArrayList<Long>> answer : answers.entrySet()) {
                                                        for (int questionIndex = 0; questionIndex < answer.getValue().size(); questionIndex++) {
                                                            answerCount.get(questionIndex)[answer.getValue().get(questionIndex).intValue()]++;
                                                            if (mostAnsweredCount[questionIndex] < answerCount.get(questionIndex)[answer.getValue().get(questionIndex).intValue()]) {
                                                                mostAnsweredCount[questionIndex] = answerCount.get(questionIndex)[answer.getValue().get(questionIndex).intValue()];
                                                            }
                                                        }

                                                        if (answer.getKey().equals(mAnswer)) {
                                                            for (int questionIndex = 0; questionIndex < mSurvey.questions.size(); questionIndex++) {
                                                                mSurvey.questions.get(questionIndex).setAnswered(true);
                                                                mSurvey.questions.get(questionIndex).setSelectedOption(answer.getValue().get(questionIndex).intValue());
                                                            }
                                                        }
                                                    }

                                                    for (int questionIndex = 0; questionIndex < answerCount.size(); questionIndex++) {
                                                        for (int optionIndex = 0; optionIndex < answerCount.get(questionIndex).length; optionIndex++) {
                                                            if (answerCount.get(questionIndex)[optionIndex] == mostAnsweredCount[questionIndex]) {
                                                                mSurvey.questions.get(questionIndex).getOptions().get(optionIndex).setMostSelectedOption(true);
                                                            }
                                                        }
                                                    }
                                                }


                                                initView();
                                            }
                                        });
                                    } else {
                                        // in case the survey was answered
                                        if (!TextUtils.isEmpty(mAnswer)) {
                                            mSurveyDatabaseReference.child(ANSWERS_KEY).child(mSurveyKey).child(mAnswer).addListenerForSingleValueEvent(new SimpleValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    ArrayList<Long> answers = (ArrayList) dataSnapshot.getValue();

                                                    for (int questionIndex = 0; questionIndex < mSurvey.questions.size(); questionIndex++) {
                                                        mSurvey.questions.get(questionIndex).setAnswered(true);
                                                        mSurvey.questions.get(questionIndex).setSelectedOption(answers.get(questionIndex).intValue());
                                                    }

                                                    initView();
                                                }
                                            });
                                        } else {
                                            initView();
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void initMemberLayout() {
        membersCount = mSurvey.members.size();

        mNotEditableMembersLayout.enableLayoutTransition(false);

        for (String email : mSurvey.members.keySet()) {
            if (!mSurveyMembers.containsKey(email)) {
                mSurveyMembers.put(email, mSurvey.members.get(email));

                Member member = new Member();
                member.setEmail(FirebaseUtils.decodeFirebaseKey(email));
                member.setEditable(false);

                mNotEditableMembersLayout.addEditorView(member);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnSurveyChangedListener = null;
    }

    public void setOnSurveyCreateListener(OnSurveyChangedListener listener) {
        mOnSurveyChangedListener = listener;
    }

    private void initView() {
        if (getActivity() != null) {
            if (!isAddMode()) {
                if (mSurvey == null) {
                    mSurveyDetailView.setVisibility(View.GONE);
                    mProgressBarView.setVisibility(View.GONE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.survey_unavailable));
                    builder.setMessage(getString(R.string.survey_unavailable_message));

                    String positiveText = getString(android.R.string.ok);
                    builder.setPositiveButton(positiveText,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    return;
                } else {
                    mTitle.setEnabled(false);
                    mTitle.setFocusable(false);
                    mTitle.setText(mSurvey.title);
                    if (TextUtils.isEmpty(mSurvey.description)) {
                        mDescriptionInputLayout.setVisibility(View.GONE);
                    } else {
                        mDescription.setEnabled(false);
                        mDescription.setFocusable(false);
                        ViewUtils.wrapTextInView(mDescription, 3);
                        mDescription.setText(mSurvey.description);
                    }

                    if (mSurvey.endDate == DateUtils.DATE_NOT_SET) {
                        mDateTimeLayoutGroup.setVisibility(View.GONE);
                    } else {
                        mEndDate = mSurvey.endDate;
                        fillDate();
                    }

                    mQuestionsLayout.enableLayoutTransition(false);

                    if (mSurvey != null) {
                        if (mQuestionsLayout.getSize() == 0) {
                            for (Question question : mSurvey.questions) {
                                question.setEditable(false);
                                mQuestionsLayout.addEditorView(question);
                            }
                        }
                    }

                    mDescriptionInputLayout.setCounterEnabled(false);
                    mTitleInputLayout.setCounterEnabled(false);
                }
            } else {
                mDateTimeLayoutGroup.setVisibility(View.GONE);
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

                mAddQuestionView.setVisibility(View.VISIBLE);
                mAddQuestionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mQuestionsLayout.addEditorView(new Question());
                    }
                });
            }

            getActivity().invalidateOptionsMenu();
            mProgressBarView.setVisibility(View.GONE);
            mSurveyDetailView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        mSurvey = null;
        if (mMembers != null) {
            for (Member member : mMembers) {
                member.setParent(null);
            }
        }
        mMembers = null;

        if (mQuestions != null) {
            for (Question question : mQuestions) {
                question.setParent(null);
            }
        }
        mQuestions = null;

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SURVEY_KEY, mSurveyKey);
        outState.putSerializable(STATE_SURVEY_TYPE, mSurveyType);
        outState.putBoolean(STATE_TIME_PICKER_SHOWN, mHasTimePickerShown);
        outState.putLong(STATE_END_DATE, mEndDate);
        outState.putSerializable(STATE_SURVEY_MEMBERS, mSurveyMembers);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mSurvey != null) {
            MenuItem vote = menu.findItem(R.id.vote_menu);
            if (vote != null) {
                vote.setVisible(TextUtils.isEmpty(mAnswer));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAddMode()) {
            inflater.inflate(R.menu.add_menu, menu);
        } else {
            switch (mSurveyType) {
                case MEMBER:
                    inflater.inflate(R.menu.member_menu, menu);
                    break;
                case OWNER:
                    inflater.inflate(R.menu.owner_menu, menu);
                    break;
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_menu:
                createSurvey();
                return true;
            case R.id.vote_menu:
                vote();
                return true;
            case R.id.delete_menu:
                deleteSurvey();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSurvey() {
        Map<String, Object> map = new HashMap<>();

        for (String user : mSurveyMembers.keySet()) {
            map.put(SURVEYS_PER_USER_KEY + "/" + user + "/" + mSurveyKey, new HashMap<>().put(mSurveyKey, null));
        }

        map.put(SURVEYS_KEY + "/" + mSurveyKey, null);
        map.put(ANSWERS_KEY + "/" + mSurveyKey, null);

        mSurveyDatabaseReference.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    PreferenceUtils.editSurveyList(getContext(), REMOVE, mSurveyKey);
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    if (mOnSurveyChangedListener == null) {
                        getActivity().finish();
                    } else {
                        mOnSurveyChangedListener.onSurveyDeleted();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    Timber.d(databaseError.getMessage());
                }
            }
        });
    }

    public void setSurveyKeyType(String key, Survey.Type surveyType) {
        mSurveyKey = key;
        mSurveyType = surveyType;
    }

    private boolean isAddMode() {
        return TextUtils.isEmpty(mSurveyKey);
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
        mDateInputLayout.setErrorEnabled(false);
        mTimeInputLayout.setErrorEnabled(false);
        if (!isAddMode()) {
            mDatePickerView.setClickable(false);
            mDatePickerView.setEnabled(false);
            mTimePickerView.setClickable(false);
            mTimePickerView.setEnabled(false);
            mClearDateView.setVisibility(View.GONE);
        } else {
            mClearDateView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isDataValid() {
        boolean isDataValid = true;

        if (isAddMode()) { //survey creation
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
            } else {
                int membersCount = 0;
                for (Member member : mMembers) {
                    if (!TextUtils.isEmpty(member.getEmail())) {
                        membersCount++;
                    }
                }
                if (membersCount < Constants.MIN_MEMBERS) {
                    isDataValid = false;
                    Toast.makeText(getContext(), String.format(getString(R.string.min_members_error_msg), Constants.MIN_MEMBERS), Toast.LENGTH_SHORT).show();
                }
            }

            //noinspection unchecked
            mQuestions = (ArrayList<Question>) mQuestionsLayout.getData();
            if (mQuestions == null) {
                isDataValid = false;
            }
        } else { //survey vote
            mQuestions = (ArrayList<Question>) mQuestionsLayout.getData();
            if (mQuestions == null) {
                isDataValid = false;
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
                DatabaseReference userDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail()));

                Survey survey = new Survey();
                survey.title = mTitle.getText().toString();
                survey.description = mDescription.getText().toString();
                survey.owner = firebaseUser.getUid();
                survey.startDate = Calendar.getInstance().getTimeInMillis();
                survey.ownerEmail = firebaseUser.getEmail();
                if (mEndDate != DateUtils.DATE_NOT_SET) {
                    survey.endDate = mEndDate;
                }
                survey.questions = mQuestions;

                String surveyKey = surveyDatabaseReference.push().getKey();

                PreferenceUtils.editSurveyList(getContext(), ADD, surveyKey);

                DatabaseReference memberDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_KEY).child(surveyKey).child(MEMBERS_KEY);

                surveyDatabaseReference.child(surveyKey).setValue(survey);
                userDatabaseReference.child(surveyKey).setValue(true);
                memberDatabaseReference.child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).setValue(true);

                for (Member member : mMembers) {
                    if (!TextUtils.isEmpty(member.getEmail())) {
                        String encodedEmail = FirebaseUtils.encodeAsFirebaseKey(member.getEmail());
                        memberDatabaseReference.child(encodedEmail).setValue(true);

                        DatabaseReference invitePerUserDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(encodedEmail);
                        invitePerUserDatabaseReference.child(surveyKey).setValue(false);
                    }
                }

                Toast.makeText(getContext(), R.string.survey_created, Toast.LENGTH_SHORT).show();

                if (mOnSurveyChangedListener == null) {
                    getActivity().finish();
                } else {
                    mOnSurveyChangedListener.onSurveyCreated(surveyKey);
                }
            }
        }
    }

    private void vote() {
        if (isDataValid()) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                FirebaseDatabase firebaseDatabase = FirebaseUtils.getDatabase();
                DatabaseReference answerDatabaseReference = firebaseDatabase.getReference().child(ANSWERS_KEY);
                DatabaseReference surveyDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_KEY).child(mSurveyKey).child(ANSWERS_KEY);
                DatabaseReference userDatabaseReference = firebaseDatabase.getReference().child(SURVEYS_PER_USER_KEY).child(FirebaseUtils.encodeAsFirebaseKey(firebaseUser.getEmail())).child(mSurveyKey);

                Map<String, Integer> answers = new HashMap<>();
                for (int questionIndex = 0; questionIndex < mQuestions.size(); questionIndex++) {
                    answers.put(Integer.toString(questionIndex), mQuestions.get(questionIndex).getSelectedOption());
                }

                DatabaseReference answer = answerDatabaseReference.child(mSurveyKey).push();
                answer.setValue(answers);
                userDatabaseReference.setValue(answer.getKey());

                surveyDatabaseReference.child(answer.getKey()).setValue(true);

                Toast.makeText(getContext(), R.string.voted, Toast.LENGTH_SHORT).show();

                if (mOnSurveyChangedListener == null) {
                    getActivity().finish();
                } else {
                    mOnSurveyChangedListener.onSurveyVoted(mSurveyKey, mSurveyType);
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
