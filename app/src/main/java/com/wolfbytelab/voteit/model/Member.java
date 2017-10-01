package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.view.View;
import android.widget.EditText;

import com.wolfbytelab.voteit.R;

public class Member implements Editable {

    private View mView;
    private String email;

    public Member() {

    }

    public Member(Parcel in) {
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
    public void fillView(View view) {
        mView = view;
        ((EditText) mView.findViewById(R.id.member_email)).setText(email);
    }

    @Override
    public void saveState() {
        email = ((EditText) mView.findViewById(R.id.member_email)).getText().toString();
    }
}