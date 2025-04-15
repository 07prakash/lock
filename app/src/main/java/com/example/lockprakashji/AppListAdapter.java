package com.example.lockprakashji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private List<AppInfo> appList;
    private OnAppSelectedListener listener;

    public interface OnAppSelectedListener {
        void onAppSelected(AppInfo appInfo, boolean isSelected);
    }

    public AppListAdapter(List<AppInfo> appList, OnAppSelectedListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo currentApp = appList.get(position);
        holder.appName.setText(currentApp.getName());
        holder.appIcon.setImageDrawable(currentApp.getIcon());
        holder.appSelectedCheckbox.setChecked(currentApp.isSelected());

        // Set listener for checkbox changes
        holder.appSelectedCheckbox.setOnCheckedChangeListener(null); // Prevent listener firing during binding
        holder.appSelectedCheckbox.setChecked(currentApp.isSelected());
        holder.appSelectedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentApp.setSelected(isChecked);
            if (listener != null) {
                listener.onAppSelected(currentApp, isChecked);
            }
        });

        // Also allow selecting by clicking the whole item row
        holder.itemView.setOnClickListener(v -> {
            holder.appSelectedCheckbox.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return appList == null ? 0 : appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        CheckBox appSelectedCheckbox;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            appSelectedCheckbox = itemView.findViewById(R.id.checkBoxAppSelected);
        }
    }
} 