package com.wolfbytelab.voteit.ui.editor;

import android.os.Parcelable;
import android.view.ViewGroup;

import com.google.firebase.database.Exclude;

public abstract class Editable implements Parcelable {
    @Exclude
    private boolean mIsEditable = true;

    public abstract void fillView(SectionView parent, ViewGroup view);

    public abstract void saveState();

    public abstract void setParent(SectionView sectionView);

    @Exclude
    public abstract boolean isValid();

    public abstract boolean hasFocus();

    public abstract void requestFocus();

    @Exclude
    public boolean isEditable() {
        return mIsEditable;
    }

    public void setEditable(boolean editable) {
        mIsEditable = editable;
    }
}
