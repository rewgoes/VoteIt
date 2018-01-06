package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.Exclude;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.editor.Editable;
import com.wolfbytelab.voteit.ui.editor.SectionView;
import com.wolfbytelab.voteit.util.Constants;

import java.util.ArrayList;

public class Question extends Editable {

    private String title;
    private ArrayList<Option> options;

    @Exclude
    private ViewGroup mView;
    @Exclude
    private SectionView mParent;
    @Exclude
    private boolean isValid = true;

    @Exclude
    private boolean hasFocus;
    @Exclude
    private int selectionPos;

    public Question() {
    }

    public Question(Parcel in) {
        title = in.readString();
        options = in.readArrayList(Option.class.getClassLoader());
        isValid = in.readInt() == 1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<Option> options) {
        this.options = options;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeList(options);
        dest.writeInt(isValid ? 1 : 0);
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public void saveState() {
        EditText titleView = mView.findViewById(R.id.question_title);
        hasFocus = titleView.hasFocus();
        selectionPos = titleView.getSelectionStart();
        title = titleView.getText().toString();
        SectionView optionsView = mView.findViewById(R.id.options);
        optionsView.saveState();
    }

    @Override
    public void fillView(SectionView parent, ViewGroup view) {
        mParent = parent;
        mView = view;

        if (!isValid) {
            ((TextInputLayout) mView.findViewById(R.id.question_title_textinput)).setError(mView.getContext().getString(R.string.required_field));
        }

        TextInputEditText titleView = mView.findViewById(R.id.question_title);
        titleView.setText(title);

        if (isEditable()) {
            titleView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ((TextInputLayout) mView.findViewById(R.id.question_title_textinput)).setErrorEnabled(false);
                    isValid = true;
                }

                @Override
                public void afterTextChanged(android.text.Editable editable) {
                }
            });
        } else {
            titleView.setEnabled(false);
        }

        final SectionView optionsView = mView.findViewById(R.id.options);

        View addOptionView = mView.findViewById(R.id.add_option);
        addOptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Option option = new Option();
                options.add(option);
                optionsView.addEditorView(option);
            }
        });

        optionsView.addOnRemoveChildListener(new SectionView.OnRemoveChildListener() {
            @Override
            public void onChildRemoved(int index) {
                if (options != null && options.size() > index) {
                    options.remove(index);
                }
            }
        });

        View deleteView = mView.findViewById(R.id.remove_question);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mParent.getSize() > Constants.MIN_QUESTIONS) {
                    mParent.removeViewGroup(Question.this, mView);
                } else {
                    Toast.makeText(mView.getContext(), String.format(mView.getContext().getString(R.string.min_questions_error_msg), Constants.MIN_QUESTIONS), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (options == null) {
            options = new ArrayList<>();
            options.add(new Option());
            options.add(new Option());
        }

        for (Option option : options) {
            optionsView.addEditorView(option);
        }
    }

    @Override
    public void setParent(SectionView parent) {
        mParent = parent;
    }

    @Exclude
    @Override
    public boolean isValid() {
        isValid = true;
        title = ((EditText) mView.findViewById(R.id.question_title)).getText().toString();
        if (TextUtils.isEmpty(title)) {
            ((TextInputLayout) mView.findViewById(R.id.question_title_textinput)).setError(mView.getContext().getString(R.string.required_field));
            isValid = false;
        }
        SectionView optionsView = mView.findViewById(R.id.options);

        //noinspection unchecked
        ArrayList<Option> newOptions = (ArrayList<Option>) optionsView.getData();
        if (newOptions == null || newOptions.size() < 2) {
            isValid = false;
        }

        if (newOptions != null) {
            options = newOptions;
        }

        return isValid;
    }

    @Override
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void requestFocus() {
        mView.findViewById(R.id.question_title).post(new Runnable() {
            @Override
            public void run() {
                EditText titleView = mView.findViewById(R.id.question_title);
                titleView.requestFocus();
                titleView.setSelection(selectionPos);
            }
        });
    }
}
