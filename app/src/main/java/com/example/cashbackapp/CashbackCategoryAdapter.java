package com.example.cashbackapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CashbackCategoryAdapter extends RecyclerView.Adapter<CashbackCategoryAdapter.VH> {

    public interface OnCategoryClickListener {
        void onClick(CashbackCategory category);
    }

    private final List<CashbackCategory> items;
    private final OnCategoryClickListener listener;

    public CashbackCategoryAdapter(List<CashbackCategory> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cashback_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CashbackCategory c = items.get(position);
        h.tvName.setText(c.name);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
