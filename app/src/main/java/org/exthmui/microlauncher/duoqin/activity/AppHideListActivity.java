package org.exthmui.microlauncher.duoqin.activity;

import static org.exthmui.microlauncher.duoqin.utils.Constants.launcherSettingsPref;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.adapter.HideAppAdapter;
import org.exthmui.microlauncher.duoqin.databinding.AppHideListViewBinding;
import org.exthmui.microlauncher.duoqin.utils.Application;
import org.exthmui.microlauncher.duoqin.utils.Constants;
import org.exthmui.microlauncher.duoqin.utils.LauncherUtils;
import org.exthmui.microlauncher.duoqin.utils.RestartTool;
import org.exthmui.microlauncher.duoqin.widgets.AppRecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AppHideListActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = AppHideListActivity.class.getSimpleName();

    private AppHideListViewBinding binding;
    private SharedPreferences sharedPreferences;
    private PkgDelReceiver mPkgDelReceiver;
    private HideAppReceiver hideAppReceiver;
    private List<String> excludePackagesList;
    private boolean reloadFlag = false;
    private boolean isFocusItemZoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppHideListViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding.menuBack.setOnClickListener(new funFunc());
        binding.menuExcludeMenu.setOnClickListener(new funFunc());
        sharedPreferences = getSharedPreferences(launcherSettingsPref, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadSettings();
        receiveSyscast();
        loadApp();
    }

    private void loadSettings(){
        isFocusItemZoom = sharedPreferences.getBoolean("app_list_focus_zoom",true);
        excludePackagesList = LauncherUtils.getExcludePackagesName(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadSettings();
    }

    /**
     * 加载应用列表
     */
    private void loadApp(){
        List<Application> mApplicationList = new ArrayList<>();
        //设置启动Intent
        Intent intent = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, 0)) {
            String appLabel = resolveInfo.loadLabel(getPackageManager()).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            boolean isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            Intent appIntent = new Intent().setClassName(packageName, resolveInfo.activityInfo.name);
            //初始化Application Bean
            Application application = new Application(
                    resolveInfo.loadIcon(getPackageManager()), //图标
                    appLabel, //名称
                    isSystemApp, //是否为系统应用
                    appIntent, //启动Intent
                    resolveInfo.activityInfo.packageName); //包名
            if (excludePackagesList.contains(packageName) && !appLabel.equals(getString(R.string.app_name))) {
                mApplicationList.add(application);
            }
        }
        AppRecyclerView mAppRecyclerView = binding.hideAppList;
        //列表布局
        mAppRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAppRecyclerView.setAdapter(new HideAppAdapter(mApplicationList, 0, isFocusItemZoom));
    }

    private void receiveSyscast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        if (mPkgDelReceiver == null && hideAppReceiver == null) {
            mPkgDelReceiver = new PkgDelReceiver();
            hideAppReceiver = new HideAppReceiver();
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(mPkgDelReceiver, intentFilter);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(hideAppReceiver,
                            new IntentFilter(Constants.HIDE_APP_ACTION));
        }
    }

    class PkgDelReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"detect package change...");
            Toasty.info(context, R.string.refreshing_pkg_list,Toasty.LENGTH_LONG).show();
            loadApp();
        }
    }

    class HideAppReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Receive hide app list refresh broadcast");
            excludePackagesList = LauncherUtils.getExcludePackagesName(AppHideListActivity.this);
            Toasty.info(context,R.string.refreshing_pkg_list,Toasty.LENGTH_LONG).show();
            loadApp();
        }
    }

    class funFunc implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v == binding.menuBack) {
                rebootLauncher(reloadFlag);
                finish();
            }else if(v == binding.menuExcludeMenu){
                showMenu(v);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exclude_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.exclude_list_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.clear_all_app) {
                LauncherUtils.showClearAllExcludeDialog(AppHideListActivity.this);
            }
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            rebootLauncher(reloadFlag);
            finish();
        } else if (id == R.id.clear_all_app) {
            LauncherUtils.showClearAllExcludeDialog(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void rebootLauncher(boolean isReboot){
        if(isReboot){
            RestartTool.restartApp(getApplicationContext(),100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPkgDelReceiver != null && hideAppReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPkgDelReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(hideAppReceiver);
            mPkgDelReceiver = null;
            hideAppReceiver = null;
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
