package com.wolfbytelab.voteit.ui.editor;

import android.os.Parcelable;
import android.view.ViewGroup;

public interface Editable extends Parcelable {

    void fillView(SectionView parent, ViewGroup view);

    void saveState();

    void setParent(SectionView sectionView);

    boolean isValid();

    boolean hasFocus();

    void requestFocus();
}
