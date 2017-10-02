package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.editor.Editable;
import com.wolfbytelab.voteit.ui.editor.SectionView;

public class Member implements Editable {

    private ViewGroup mView;
    private String email;
    private SectionView mParent;
    private int mPosition;
    private boolean isValid = true;

    public Member() {
    }

    private Member(Parcel in) {
        email = in.readString();
        isValid = in.readInt() == 1;
    }

    public String getEmail() {
        return email;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeInt(isValid ? 1 : 0);
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    @Override
    public void fillView(SectionView parent, ViewGroup view, int position) {
        mParent = parent;
        mView = view;
        mPosition = position;

        if (!isValid) {
            ((TextInputLayout) mView.findViewWithTag(mView.getContext().getString(R.string.email_text_input))).setError(mView.getContext().getString(R.string.invalid_email));
        }

        EditText emailView = mView.findViewWithTag(view.getContext().getString(R.string.tag_email));
        emailView.setText(email);

        emailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mPosition == mParent.getSize() - 1 && !TextUtils.isEmpty(charSequence)) {
                    mParent.addEditorView(new Member());
                    mView.findViewById(R.id.remove_member).setVisibility(View.VISIBLE);
                }
                ((TextInputLayout) mView.findViewWithTag(mView.getContext().getString(R.string.email_text_input))).setErrorEnabled(false);
                isValid = true;
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
            }
        });

        ImageView deleteView = mView.findViewById(R.id.remove_member);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mParent.removeViewGroup(Member.this, mView);
            }
        });

        if (position != mParent.getSize() - 1) {
            deleteView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void saveState() {
        email = ((EditText) mView.findViewWithTag(mView.getContext().getString(R.string.tag_email))).getText().toString();
        mParent = null;
    }

    @Override
    public void setParent(SectionView parent) {
        mParent = parent;
    }

    @Override
    public boolean isValid() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return false;
        } else {
            email = ((EditText) mView.findViewWithTag(mView.getContext().getString(R.string.tag_email))).getText().toString();
            if (!TextUtils.isEmpty(email)) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.equals(firebaseUser.getEmail(), email)) {
                    isValid = false;
                    ((TextInputLayout) mView.findViewWithTag(mView.getContext().getString(R.string.email_text_input))).setError(mView.getContext().getString(R.string.invalid_email));
                    return false;
                }
            }
        }
        return true;
    }
}
