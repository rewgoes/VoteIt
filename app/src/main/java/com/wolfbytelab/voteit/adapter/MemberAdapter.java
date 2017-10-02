package com.wolfbytelab.voteit.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfbytelab.voteit.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private final RemoveItemListener mListener;
    private ArrayList<String> mMembers;

    public interface RemoveItemListener {
        void onRemoveItemClicked(int position);
    }

    public MemberAdapter(ArrayList<String> members, @NonNull RemoveItemListener listener) {
        mMembers = members;
        mListener = listener;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_member_item, parent, false);
        return new MemberViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MemberViewHolder holder, final int position) {
        String mMember = mMembers.get(holder.getAdapterPosition());
        holder.textListener.updatePosition(holder.getAdapterPosition());
        holder.textListener.updateViewHolder(holder);
        holder.memberEmail.setText(mMember);
        holder.clickListener.updatePosition(holder.getAdapterPosition());
        holder.clickListener.updateViewHolder(holder);

        if (holder.getAdapterPosition() == mMembers.size() - 1) {
            holder.removeMember.setVisibility(View.INVISIBLE);
        } else {
            holder.removeMember.setVisibility(View.VISIBLE);
        }
    }

    private void addMember() {
        if (mMembers == null) {
            mMembers = new ArrayList<>();
        }
        mMembers.add("");
        notifyItemInserted(mMembers.size() - 1);
        notifyItemRangeChanged(mMembers.size() - 1, mMembers.size());
    }

    private void removeMember(int position) {
        mMembers.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mMembers.size());
    }

    @Override
    public int getItemCount() {
        return mMembers == null ? 0 : mMembers.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        //        @BindView(R.id.member_email)
        TextView memberEmail;
        //        @BindView(R.id.remove_member)
        ImageView removeMember;
        EditTextListener textListener;
        ViewHolderClickListener clickListener;

        MemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            textListener = new EditTextListener();
            memberEmail.addTextChangedListener(textListener);
            clickListener = new ViewHolderClickListener();
            removeMember.setOnClickListener(clickListener);
        }
    }

    private class EditTextListener implements TextWatcher {
        private int position;
        private MemberViewHolder holder;

        void updatePosition(int position) {
            this.position = position;
        }

        void updateViewHolder(MemberViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mMembers.set(position, charSequence.toString());
            if (position == mMembers.size() - 1 && !TextUtils.isEmpty(charSequence)) {
                addMember();
                holder.removeMember.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class ViewHolderClickListener implements View.OnClickListener {
        private int position;
        private MemberViewHolder holder;

        void updatePosition(int position) {
            this.position = position;
        }

        void updateViewHolder(MemberViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View view) {
            if (mMembers.size() > 1) {
                holder.memberEmail.requestFocus();
                removeMember(position);
                mListener.onRemoveItemClicked(position);
            }
        }
    }
}
