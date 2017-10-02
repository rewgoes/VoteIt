package com.wolfbytelab.voteit.ui.editor;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wolfbytelab.voteit.R;

import java.util.ArrayList;

public class SectionView extends LinearLayout {
    private int mLayout;
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
        LayoutTransition transition = new LayoutTransition();
        setLayoutTransition(transition);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        for (int i = 0; i < mChildren.size(); i++) {
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

        mChildren = ss.children;

        for (int i = 0; i < mChildren.size(); i++) {
            ViewGroup view = addEditorView(mChildren.get(i));
            mChildren.get(i).setParent(this);
            mChildren.get(i).fillView(this, view, i);
        }

        isRestoring = false;
    }

    public ViewGroup addEditorView(Editable child) {
        final ViewGroup view = (ViewGroup) mLayoutInflater.inflate(mLayout, this, false);
        addView(view);
        if (!isRestoring) {
            mChildren.add(child);
            child.fillView(this, view, mChildren.size() - 1);
        }
        return view;
    }

    public int getSize() {
        return mChildren.size();
    }

    public void removeViewGroup(Editable child, ViewGroup viewGroup) {
        mChildren.remove(child);
        removeView(viewGroup);
    }

    private static class SavedState extends BaseSavedState {
        ArrayList<Editable> children;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readList(children, null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
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

    public ArrayList<Editable> getData() {
        if (isDataValid()) {
            return mChildren;
        } else {
            return null;
        }
    }

    private boolean isDataValid() {
        boolean isValid = true;
        for (int i = 0; i < mChildren.size(); i++) {
            isValid = mChildren.get(i).isValid();
        }
        return isValid;
    }
}
