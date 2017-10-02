package com.wolfbytelab.voteit.ui.editor;

import android.os.Parcelable;
import android.view.ViewGroup;

public interface Editable extends Parcelable {
    void fillView(ViewGroup view, boolean isLatest);

    void saveState();

    void setParent(OnUpdateViewListener sectionView);
}
