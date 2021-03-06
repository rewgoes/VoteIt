package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.support.design.widget.TextInputEditText;
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

public class Member extends Editable {

    private ViewGroup mView;

    private String email;
    private SectionView mParent;
    private boolean isValid = true;
    private boolean hasFocus = false;
    private int selectionPos;

    public Member() {
    }

    private Member(Parcel in) {
        email = in.readString();
        isValid = in.readInt() == 1;
        hasFocus = in.readInt() == 1;
        selectionPos = in.readInt();
        setEditable(in.readInt() == 1);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeInt(isValid ? 1 : 0);
        parcel.writeInt(hasFocus ? 1 : 0);
        parcel.writeInt(selectionPos);
        parcel.writeInt(isEditable() ? 1 : 0);
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
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void fillView(SectionView parent, ViewGroup view) {
        mParent = parent;
        mView = view;

        if (!isValid) {
            ((TextInputLayout) mView.findViewById(R.id.member_email_textinput)).setError(mView.getContext().getString(R.string.invalid_email));
        }

        TextInputEditText emailView = mView.findViewById(R.id.member_email);
        emailView.setText(email);

        if (isEditable()) {
            emailView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (mParent.getIndexOf(Member.this) == mParent.getSize() - 1 && !TextUtils.isEmpty(charSequence)) {
                        mParent.addEditorView(new Member());
                        mView.findViewById(R.id.remove_member).setVisibility(View.VISIBLE);
                    }
                    ((TextInputLayout) mView.findViewById(R.id.member_email_textinput)).setErrorEnabled(false);
                    isValid = true;
                }

                @Override
                public void afterTextChanged(android.text.Editable editable) {
                }
            });
        } else {
            emailView.setEnabled(false);
        }

        ImageView deleteView = mView.findViewById(R.id.remove_member);
        if (isEditable()) {
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mParent.removeViewGroup(Member.this, mView);
                }
            });
        } else {
            deleteView.setVisibility(View.GONE);
        }

        if (mParent.getIndexOf(Member.this) != mParent.getSize() - 1 && isEditable()) {
            deleteView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void saveState() {
        EditText emailView = mView.findViewById(R.id.member_email);
        hasFocus = emailView.hasFocus();
        selectionPos = emailView.getSelectionStart();
        email = emailView.getText().toString();
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
            email = ((EditText) mView.findViewById(R.id.member_email)).getText().toString();
            if (!TextUtils.isEmpty(email)) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.equals(firebaseUser.getEmail(), email)) {
                    isValid = false;
                    ((TextInputLayout) mView.findViewById(R.id.member_email_textinput)).setError(mView.getContext().getString(R.string.invalid_email));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void requestFocus() {
        mView.findViewById(R.id.member_email).post(new Runnable() {
            @Override
            public void run() {
                EditText emailView = mView.findViewById(R.id.member_email);
                emailView.requestFocus();
                emailView.setSelection(selectionPos);
            }
        });
    }


}
