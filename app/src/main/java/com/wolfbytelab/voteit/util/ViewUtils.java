package com.wolfbytelab.voteit.util;

import android.widget.TextView;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class ViewUtils {

    public static void wrapTextInView(TextView view, int maxLines) {
        view.setMaxLines(maxLines);
        view.setEllipsize(null);
        view.setHorizontallyScrolling(false);
        view.setInputType(TYPE_TEXT_FLAG_MULTI_LINE | TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

}
