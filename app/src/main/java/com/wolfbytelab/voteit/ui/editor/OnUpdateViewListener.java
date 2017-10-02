package com.wolfbytelab.voteit.ui.editor;

import android.view.ViewGroup;

public interface OnUpdateViewListener {
    void addView(Editable editable);

    void removeViewGroup(Editable editable, ViewGroup viewGroup);
}
