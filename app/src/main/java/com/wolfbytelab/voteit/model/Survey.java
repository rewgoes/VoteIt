package com.wolfbytelab.voteit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.wolfbytelab.voteit.util.DateUtils;

import java.util.ArrayList;

public class Survey implements Parcelable {

    public String key;
    public String title;
    public String description;

    public String owner;
    public ArrayList<User> members;
    public ArrayList<Question> questions;

    public long startDate;
    public long endDate = DateUtils.DATE_NOT_SET;

    @Exclude
    public String answer = null;

    public Type type;

    public enum Type {
        OWNER,
        MEMBER
    }

    public Survey() {
    }

    // Parcelling part
    public Survey(Parcel in) {
        key = in.readString();
        title = in.readString();
        description = in.readString();
        owner = in.readString();
        startDate = in.readLong();
        endDate = in.readLong();
        type = Type.valueOf(in.readString());
    }

    public static final Creator<Survey> CREATOR = new Creator<Survey>() {
        @Override
        public Survey createFromParcel(Parcel in) {
            return new Survey(in);
        }

        @Override
        public Survey[] newArray(int size) {
            return new Survey[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(owner);
        parcel.writeLong(startDate);
        parcel.writeLong(endDate);
        parcel.writeString(type.name());
    }

    @Override
    public boolean equals(Object obj) {
        if (TextUtils.isEmpty(key)) {
            return false;
        } else if (obj instanceof String) {
            return TextUtils.equals(key, (String) obj);
        } else {
            return obj instanceof Survey && TextUtils.equals(key, ((Survey) obj).key);
        }
    }
}