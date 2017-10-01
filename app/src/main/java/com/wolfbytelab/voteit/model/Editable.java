package com.wolfbytelab.voteit.model;

import android.os.Parcelable;
import android.view.View;

public interface Editable extends Parcelable {
    void fillView(View view);

    void saveState();
}
