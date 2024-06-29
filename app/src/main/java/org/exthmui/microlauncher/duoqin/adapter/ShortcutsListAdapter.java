package org.exthmui.microlauncher.duoqin.adapter;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.exthmui.microlauncher.duoqin.R;

import java.util.List;

public class ShortcutsListAdapter extends
        RecyclerView.Adapter<ShortcutsListAdapter.ViewHolder> {

    private static final String TAG = "ShortcutsListAdapter";
    private final List<ShortcutInfo> mShortcutInfoList;
    private static ShortcutCallback shortcutCallback;
    private LauncherApps mLauncherApps;

    public ShortcutsListAdapter(List<ShortcutInfo> mShortcutInfoList) {
        this.mShortcutInfoList = mShortcutInfoList;
    }

    public static void setShortcutCallback(ShortcutCallback callback) {
        shortcutCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shortcuts_item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShortcutInfo info = this.mShortcutInfoList.get(position);
        mLauncherApps = (LauncherApps) holder.itemView.getContext()
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (TextUtils.isEmpty(info.getLongLabel())){
            holder.mLabel.setText(info.getShortLabel());
        } else {
            holder.mLabel.setText(info.getLongLabel());
        }
        holder.mIcon.setImageDrawable(
                getIcon(holder.mIcon.getContext(),info, DisplayMetrics.DENSITY_DEVICE_STABLE));
        holder.itemView.setOnClickListener(v -> {
            try {
                mLauncherApps.startShortcut(info.getPackage(),info.getId(),
                        null,null, Process.myUserHandle());
                shortcutCallback.dismissDialog();
            } catch (Exception e) {
                Log.e(TAG, "Failed to start shortcut", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShortcutInfoList == null ? 0 : mShortcutInfoList.size();
    }

    public static Drawable getIcon(Context context, ShortcutInfo shortcutInfo, int density) {
        try {
            return context.getSystemService(LauncherApps.class)
                    .getShortcutIconDrawable(shortcutInfo, density);
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Failed to get shortcut icon", e);
            return null;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView mIcon;

        private final TextView mLabel;

        public ViewHolder(View view){
            super(view);
            mIcon = view.findViewById(R.id.shortcuts_icon);
            mLabel = view.findViewById(R.id.shortcuts_title);
        }
    }

}
