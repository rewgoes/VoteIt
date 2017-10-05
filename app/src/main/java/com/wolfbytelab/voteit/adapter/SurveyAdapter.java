package com.wolfbytelab.voteit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Survey;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Survey> mSurveys;

    public SurveyAdapter(ArrayList<Survey> surveys) {
        mSurveys = surveys;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public SurveyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.survey_list_item, parent, false);
        return new SurveyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SurveyViewHolder holder, int position) {
        Survey survey = mSurveys.get(position);
        holder.title.setText(survey.title);
        holder.description.setText(survey.description);
//        holder.owner.setText(survey.owner.name);
//        holder.date.setText(DateUtils.getFormattedDate(mContext, survey.startDate));
    }

    @Override
    public int getItemCount() {
        return mSurveys == null ? 0 : mSurveys.size();
    }

    public Survey getItem(int position) {
        if (mSurveys == null || position >= mSurveys.size()) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return mSurveys.get(position);
    }

    private void surveyItemClick(SurveyViewHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.getAdapterPosition());
        }
    }

    class SurveyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.owner)
        TextView owner;
        @BindView(R.id.date)
        TextView date;

        SurveyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            surveyItemClick(this);
        }
    }

}
