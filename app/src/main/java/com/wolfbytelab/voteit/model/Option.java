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

public class Option extends Editable {
    private String title;

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

    public Option() {
    }

    public Option(Parcel in) {
        title = in.readString();
        isValid = in.readInt() == 1;
        hasFocus = in.readInt() == 1;
        selectionPos = in.readInt();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(isValid ? 1 : 0);
        dest.writeInt(hasFocus ? 1 : 0);
        dest.writeInt(selectionPos);
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

        if (hasFocus) {
            requestFocus();
        }

        if (!isValid) {
            ((TextInputLayout) mView.findViewById(R.id.option_title_textinput)).setError(mView.getContext().getString(R.string.required_field));
        }

        TextInputEditText titleView = mView.findViewById(R.id.option_title);
        titleView.setText(title);

        if (isEditable()) {
            titleView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ((TextInputLayout) mView.findViewById(R.id.option_title_textinput)).setErrorEnabled(false);
                    isValid = true;
                }

                @Override
                public void afterTextChanged(android.text.Editable editable) {
                }
            });
        } else {
            titleView.setEnabled(false);
            ((TextInputLayout) mView.findViewById(R.id.option_title_textinput)).setCounterEnabled(false);
        }

        View deleteView = mView.findViewById(R.id.remove_option);
        if (isEditable()) {
            deleteView.setVisibility(View.VISIBLE);
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mParent.getSize() > Constants.MIN_OPTIONS) {
                        mParent.getIndexOf(Option.this);
                        mParent.removeViewGroup(Option.this, mView);
                    } else {
                        Toast.makeText(mView.getContext(), String.format(mView.getContext().getString(R.string.min_options_error_msg), Constants.MIN_OPTIONS), Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
        title = ((EditText) mView.findViewById(R.id.option_title)).getText().toString();
        if (TextUtils.isEmpty(title)) {
            ((TextInputLayout) mView.findViewById(R.id.option_title_textinput)).setError(mView.getContext().getString(R.string.required_field));
            isValid = false;
        }
        return isValid;
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
