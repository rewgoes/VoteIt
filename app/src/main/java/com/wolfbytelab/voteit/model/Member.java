package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.ui.editor.Editable;
import com.wolfbytelab.voteit.ui.editor.SectionView;

public class Member implements Editable {

    private ViewGroup mView;
    private String email;
    private SectionView mParent;
    private int mPosition;

    public Member(SectionView parent) {
        mParent = parent;
    }

    private Member(Parcel in) {
        email = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
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
    public void fillView(ViewGroup view, int position) {
        mView = view;
        mPosition = position;

        EditText emailView = mView.findViewWithTag(view.getContext().getString(R.string.tag_email));
        emailView.setText(email);

        if (position == mParent.getSize() - 1) {
            emailView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (mPosition == mParent.getSize() - 1 && !TextUtils.isEmpty(charSequence)) {
                        mParent.addEditorView(new Member(mParent));
                        mView.findViewById(R.id.remove_member).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable editable) {
                }
            });
        }

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
}
