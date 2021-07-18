package com.qmuiteam.qmuidemo.fragment.components.endlessrv.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SimpleStringAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mValues;
    private LinearLayoutManager layoutManager;
    private int pending;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public String mBoundString;
        public TextView mTextView;

        public ItemViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }

    /**
     * @param layoutManager 管理器
     * @param pending       加载更多的高度 或者 宽度
     * @param strings       数据
     */
    public SimpleStringAdapter(LinearLayoutManager layoutManager, int pending, ArrayList<String> strings) {
        mValues = strings;
        if (layoutManager == null) {
            throw new IllegalArgumentException("layoutManager must not null");
        }
        this.layoutManager = layoutManager;
        this.pending = pending;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.mBoundString = mValues.get(position);
            itemViewHolder.mTextView.setText("  [ " + position + "  " + itemViewHolder.mBoundString + " ]   ");
            itemViewHolder.mTextView.setMinHeight((50 + mValues.get(position).length() * 10));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void appendItems(List<String> items) {
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    public void insertItems(List<String> items) {
        mValues.addAll(0, items);
        notifyDataSetChanged();
        layoutManager.scrollToPositionWithOffset(items.size() + 1, pending);

    }


    public void clear() {
        mValues.clear();
    }
}
