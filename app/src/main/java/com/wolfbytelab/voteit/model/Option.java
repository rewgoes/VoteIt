package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.editor.Editable;
import com.wolfbytelab.voteit.ui.editor.SectionView;

public class Option extends Editable {
    public String title;

    private ViewGroup mView;
    private SectionView mParent;

    private boolean hasFocus;
    private int selectionPos;

    public Option() {
    }

    public Option(Parcel in) {
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<Option> CREATOR = new Creator<Option>() {
        @Override
        public Option createFromParcel(Parcel in) {
            return new Option(in);
        }

        @Override
        public Option[] newArray(int size) {
            return new Option[size];
        }
    };

    @Override
    public void saveState() {
        EditText titleView = mView.findViewById(R.id.option_title);
        hasFocus = titleView.hasFocus();
        selectionPos = titleView.getSelectionStart();
        title = titleView.getText().toString();
    }

    @Override
    public void fillView(SectionView parent, ViewGroup view) {
        mParent = parent;
        mView = view;

        TextInputEditText titleView = mView.findViewById(R.id.option_title);
        titleView.setText(title);
    }

    @Override
    public void setParent(SectionView parent) {
        mParent = parent;
    }

    @Override
    public boolean isValid() {
        title = ((EditText) mView.findViewById(R.id.option_title)).getText().toString();
        if (TextUtils.isEmpty(title)) {
            ((TextInputLayout) mView.findViewById(R.id.option_title_textinput)).setError(mView.getContext().getString(R.string.required_field));
            return false;
        }
        return true;
    }

    @Override
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void requestFocus() {
        mView.findViewById(R.id.option_title).post(new Runnable() {
            @Override
            public void run() {
                EditText emailView = mView.findViewById(R.id.option_title);
                emailView.requestFocus();
                emailView.setSelection(selectionPos);
            }
        });
    }
}
