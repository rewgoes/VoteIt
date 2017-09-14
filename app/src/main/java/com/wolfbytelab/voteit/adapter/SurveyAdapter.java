package com.wolfbytelab.voteit.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wolfbytelab.voteit.R;
import com.wolfbytelab.voteit.model.Survey;
import com.wolfbytelab.voteit.util.DateUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder> {

    private ArrayList<Survey> mSurveys;
    private Context mContext;

    public SurveyAdapter(ArrayList<Survey> surveys, Context context) {
        mSurveys = surveys;
        mContext = context;
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

    public void addSurvey(Survey survey) {
        if (mSurveys == null) {
            mSurveys = new ArrayList<>();
        }
        mSurveys.add(survey);
        notifyItemInserted(mSurveys.size() - 1);
    }

    public void clear() {
        if (mSurveys != null) {
            mSurveys.clear();
            notifyDataSetChanged();
        }
    }

    class SurveyViewHolder extends RecyclerView.ViewHolder {
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
        }
    }

}
