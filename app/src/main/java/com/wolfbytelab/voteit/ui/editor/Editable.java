package com.wolfbytelab.voteit.ui.editor;

import android.os.Parcelable;
import android.view.ViewGroup;

public interface Editable extends Parcelable {
    void fillView(ViewGroup view, int position);

    void saveState();

    void setParent(SectionView sectionView);

    boolean isValid();
}
