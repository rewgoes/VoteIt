package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.support.design.widget.TextInputEditText;
import android.view.ViewGroup;
import android.widget.EditText;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.editor.Editable;
import com.wolfbytelab.voteit.ui.editor.SectionView;

import java.util.ArrayList;

public class Question extends Editable {

    public String title;
    public ArrayList<Option> options;

    private ViewGroup mView;
    private SectionView mParent;

    private boolean hasFocus;
    private int selectionPos;

    private boolean isInitialized = false;

    public Question() {
    }

    public Question(Parcel in) {
        title = in.readString();
        options = in.readArrayList(Option.class.getClassLoader());
        isInitialized = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeList(options);
        dest.writeInt(isInitialized ? 1 : 0);
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
    }

    @Override
    public void fillView(SectionView parent, ViewGroup view) {
        mParent = parent;
        mView = view;

        TextInputEditText titleView = mView.findViewById(R.id.question_title);
        titleView.setText(title);

        SectionView optionsView = mView.findViewById(R.id.options);

        if (!isInitialized) {
            isInitialized = true;
            optionsView.addEditorView(new Option());
            optionsView.addEditorView(new Option());
        }
    }

    @Override
    public void setParent(SectionView parent) {
        mParent = parent;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public void requestFocus() {
        mView.findViewById(R.id.question_title).post(new Runnable() {
            @Override
            public void run() {
                EditText emailView = mView.findViewById(R.id.member_email);
                emailView.requestFocus();
                emailView.setSelection(selectionPos);
            }
        });
    }
}
