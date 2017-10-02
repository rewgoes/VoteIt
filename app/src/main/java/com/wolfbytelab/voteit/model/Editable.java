package com.wolfbytelab.voteit.model;

import android.os.Parcelable;
import android.view.ViewGroup;

public interface Editable extends Parcelable {
    void fillView(ViewGroup view);

    void saveState();
}
