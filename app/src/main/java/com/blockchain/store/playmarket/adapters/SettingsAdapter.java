package com.blockchain.store.playmarket.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class SettingTitle extends RecyclerView.ViewHolder {

        public SettingTitle(View itemView) {
            super(itemView);
        }
    }

    class SettingMenu extends RecyclerView.ViewHolder {

        public SettingMenu(View itemView) {
            super(itemView);
        }
    }
}
