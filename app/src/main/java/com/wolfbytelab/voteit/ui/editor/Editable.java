package com.wolfbytelab.voteit.ui.editor;

import android.os.Parcelable;
import android.view.ViewGroup;

public abstract class Editable implements Parcelable {

    private boolean mIsEditable = true;

    public abstract void fillView(SectionView parent, ViewGroup view);

    public abstract void saveState();

    public abstract void setParent(SectionView sectionView);

    public abstract boolean isValid();

    public abstract boolean hasFocus();

    public abstract void requestFocus();

    public boolean isEditable() {
        return mIsEditable;
    }

    public void setEditable(boolean editable) {
        mIsEditable = editable;
    }
}
