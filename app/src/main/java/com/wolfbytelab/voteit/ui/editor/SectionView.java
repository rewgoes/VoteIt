package com.wolfbytelab.voteit.ui.editor;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Editable;

import java.util.ArrayList;

public class SectionView extends LinearLayout {
    private int mLayout;
    private int mNumChildren;
    private ArrayList<Editable> mChildren = new ArrayList<>();
    private boolean isRestoring = false;
    private LayoutInflater mLayoutInflater;

    public SectionView(Context context) {
        super(context);
    }

    public SectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        loadLayout(context, attrs, 0, 0);
    }

    public SectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadLayout(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SectionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadLayout(context, attrs, defStyleAttr, defStyleRes);
    }

    private void loadLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SectionView,
                defStyleAttr, defStyleRes
        );

        try {
            mLayout = a.getResourceId(R.styleable.SectionView_childrenLayout, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.numMemberChildren = mNumChildren;

        for (int i = 0; i < mNumChildren; i++) {
            mChildren.get(i).saveState();
        }

        ss.children = mChildren;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        isRestoring = true;

        mNumChildren = ss.numMemberChildren;
        mChildren = ss.children;

        for (int i = 0; i < mNumChildren; i++) {
            ViewGroup view = addEditorView(mChildren.get(i));
            mChildren.get(i).fillView(view);
        }

        isRestoring = false;
    }

    public ViewGroup addEditorView(Editable child) {
        final ViewGroup view = (ViewGroup) mLayoutInflater.inflate(mLayout, this, false);
        addView(view);
        if (!isRestoring) {
            mNumChildren++;
            mChildren.add(child);
            child.fillView(view);
        }
        return view;
    }

    private static class SavedState extends BaseSavedState {
        int numMemberChildren;
        ArrayList<Editable> children;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            numMemberChildren = in.readInt();
            in.readList(children, null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(numMemberChildren);
            out.writeTypedList(children);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
