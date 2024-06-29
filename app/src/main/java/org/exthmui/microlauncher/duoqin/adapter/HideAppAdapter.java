package org.exthmui.microlauncher.duoqin.adapter;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.net.Uri;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.utils.Application;
import org.exthmui.microlauncher.duoqin.utils.Constants;
import org.exthmui.microlauncher.duoqin.utils.LauncherUtils;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class HideAppAdapter extends AppAdapter {

    private List<Application> mApplicationList;
    private static View mItemView;

    /**
     * @param applicationList : 类型为Application的List集
     * @param layoutMode      :      0，线性；1：网格
     */
    public HideAppAdapter(List<Application> applicationList, int layoutMode, boolean zoomItem) {
        super(applicationList, layoutMode, zoomItem);
        mApplicationList = applicationList;
        setHasStableIds(true);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item_linear, parent, false);
        return new ViewHolder(mItemView,this.mApplicationList);
    }

    private static class ViewHolder extends AppAdapter.ApplicationViewHolder {
        private final List<Application> mApplicationList;

        public ViewHolder(View itemView, List<Application> applicationList) {
            super(itemView, applicationList);
            mApplicationList = applicationList;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mApplicationList.get(getAdapterPosition()).getAppLabel());
            menu.add(0, 0, Menu.NONE, R.string.app_menu_open);
            menu.add(0, 1, Menu.NONE,  R.string.app_menu_uninstall);
            menu.add(0, 2, Menu.NONE,  R.string.app_menu_info);
            menu.add(0, 3, Menu.NONE,  R.string.shortcuts_title);
            menu.add(0,4, Menu.NONE, R.string.unhide_app_label);
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setOnMenuItemClickListener(this);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Application application = this.mApplicationList.get(getAdapterPosition());
            Intent intent = new Intent();
            switch (item.getItemId()){
                case 0:
                    mItemView.getContext().startActivity(application.getAppIntent());
                    break;
                case 1:
                    if(application.isSystemApp()){
                        Snackbar.make(itemView,
                                R.string.this_is_system_app,
                                Snackbar.LENGTH_SHORT
                        ).show();
                    }else{
                        Uri uri = Uri.fromParts("package",
                                application.getPkgName(), null);
                        if (application.isAppSelf()){
                            Toasty.error(mItemView.getContext(),
                                    R.string.abort_msg_uninstall_self, Toasty.LENGTH_LONG
                            ).show();
                        }else{
                            intent = new Intent(Intent.ACTION_DELETE, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mItemView.getContext().startActivity(intent);
                        }
                    }
                    break;
                case 2:
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package",
                            application.getPkgName(), null));
                    mItemView.getContext().startActivity(intent);
                    break;
                case 3:
                    List<ShortcutInfo> list = getAppsShortcutsList(application.getPkgName());
                    if (LauncherUtils.isDefaultLauncher(mItemView.getContext())) {
                        if (list != null && !list.isEmpty()) {
                            showShortcutsDialog(list);
                        } else {
                            Toasty.error(mItemView.getContext(),
                                    R.string.no_shortcuts_toast,
                                    Toasty.LENGTH_SHORT
                            ).show();
                        }
                    } else {
                        Toasty.error(mItemView.getContext(),
                                R.string.not_default_launcher_shortcut_msg,
                                Toasty.LENGTH_SHORT
                        ).show();
                    }
                    break;
                case 4:
                    LauncherUtils.showExcludeAppDialog(mItemView.getContext(),
                            (String) application.getAppLabel(), application.getPkgName(), false);
                    break;
            }
            return false;
        }
    }
}
