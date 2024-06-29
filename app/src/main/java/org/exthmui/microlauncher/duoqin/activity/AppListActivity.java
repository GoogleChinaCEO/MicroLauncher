package org.exthmui.microlauncher.duoqin.activity;

import static org.exthmui.microlauncher.duoqin.utils.Constants.launcherSettingsPref;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.exthmui.microlauncher.duoqin.BuildConfig;
import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.adapter.AppAdapter;
import org.exthmui.microlauncher.duoqin.databinding.AppListActivityBinding;
import org.exthmui.microlauncher.duoqin.utils.Application;
import org.exthmui.microlauncher.duoqin.utils.Constants;
import org.exthmui.microlauncher.duoqin.utils.LauncherUtils;
import org.exthmui.microlauncher.duoqin.utils.PinyinComparator;
import org.exthmui.microlauncher.duoqin.utils.PinyinUtils;
import org.exthmui.microlauncher.duoqin.utils.TextSpeech;
import org.exthmui.microlauncher.duoqin.widgets.AppRecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AppListActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, AppAdapter.OnItemSelectCallback{
    private final static String TAG = AppListActivity.class.getSimpleName();
    private static final int DELAY_TIMER_MILLIS = 500;
    private static final int ACTIVITY_TRIGGER_COUNT = 3;
    private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];
    private AppListActivityBinding binding;
    private PkgDelReceiver mPkgDelReceiver;
    private HideAppReceiver HideAppReceiver;
    private PinyinComparator mComparator;
    private SharedPreferences sharedPreferences;
    private String app_list_style;
    private String iconPackPkg;
    private List<String> excludePackagesList;
    private boolean isSimpleList;
    private boolean isTTSEnable;
    private boolean isFocusItemZoom;
    private boolean isSortByPinyin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.appBack.setOnClickListener(new funClick());
        binding.appMenu.setOnClickListener(new funClick());
        TextSpeech.getInstance(this);
        sharedPreferences = getSharedPreferences(launcherSettingsPref,Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadSettings(sharedPreferences);
        loadApp();
        receiveSyscast();
        changeTitle(isSimpleList);
    }

    private void changeTitle(boolean isSimpleTitle){
        if(isSimpleTitle){
            setTitle(R.string.menu);
            Log.d(TAG,"changeTitle true");
        }else{
            setTitle(R.string.app_list_title);
            Log.d(TAG,"changeTitle false");
        }
    }

    private void loadSettings(SharedPreferences sharedPreferences){
        app_list_style=sharedPreferences.getString("app_list_func","grid");
        isSimpleList=sharedPreferences.getBoolean("switch_preference_app_list_func",false);
        isSortByPinyin=sharedPreferences.getBoolean("switch_preference_app_list_sort",false);
        isTTSEnable = sharedPreferences.getBoolean("app_list_tts",false);
        iconPackPkg = sharedPreferences.getString("pref_iconPackPackage", "android");
        isFocusItemZoom = sharedPreferences.getBoolean("app_list_focus_zoom",true);
        excludePackagesList = LauncherUtils.getExcludePackagesName(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadSettings(sharedPreferences);
    }

    private void receiveSyscast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        if (mPkgDelReceiver == null && HideAppReceiver == null) {
            mPkgDelReceiver = new PkgDelReceiver();
            HideAppReceiver = new HideAppReceiver();
            getApplicationContext().registerReceiver(mPkgDelReceiver, intentFilter);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(HideAppReceiver,
                            new IntentFilter(Constants.HIDE_APP_ACTION));
        }
    }

    @Override
    public void onItemSelect(View v, int position) {
        Application application = ((AppAdapter) ((AppRecyclerView) v.getParent()).getAdapter()).getItem(position);
        if (application == null) {
            return;
        }
        if (isTTSEnable) {
            String appName = application.getAppLabel().toString();
            if (BuildConfig.DEBUG) Log.d(TAG,"TTS is enabled, reading content: " + appName);
            TextSpeech.read(appName);
        }
    }

    class PkgDelReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.e(TAG,"detect package change...");
            Toasty.info(context,R.string.refreshing_pkg_list,Toasty.LENGTH_SHORT).show();
            loadApp();
        }
    }

    class HideAppReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Receive hide app list refresh broadcast");
            excludePackagesList = LauncherUtils.getExcludePackagesName(AppListActivity.this);
            Toasty.info(context,R.string.refreshing_pkg_list,Toasty.LENGTH_SHORT).show();
            loadApp();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 3次按下数字键5触发
        if (keyCode == KeyEvent.KEYCODE_5) {
            arrayCopy();
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DELAY_TIMER_MILLIS)) {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setAction("org.exthmui.microlauncher.duoqin.action.HIDE_APP_LIST");
                startActivity(intent);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void arrayCopy() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
    }

    class funClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v == binding.appBack) {
                finish();
            }else if(v == binding.appMenu){
                showMenu(v);
            }
        }
    }

    /**
     * 加载应用列表
     */
    private void loadApp(){
        List<Application> mApplicationList = new ArrayList<>();
        mComparator = new PinyinComparator();
        //设置启动Intent
        Intent intent = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, 0)) {
            String appLabel = resolveInfo.loadLabel(getPackageManager()).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            boolean isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            Intent appIntent = new Intent().setClassName(packageName, resolveInfo.activityInfo.name);
            Drawable appIcon = resolveInfo.loadIcon(getPackageManager());
            //如果应用的包名在排除列表内
            if (excludePackagesList != null && excludePackagesList.contains(packageName)) {
                continue;
            }
            if (BuildConfig.DEBUG) Log.d(TAG,"packageName: " + packageName);
            Application application;
            //如果使用图标包
            if (iconPackPkg.equals("android")){
                //初始化Application Bean
                application = new Application(
                        appIcon, //图标
                        resolveInfo.loadLabel(getPackageManager()), //名称
                        isSystemApp, //是否为系统应用
                        appIntent, //启动Intent
                        resolveInfo.activityInfo.packageName); //包名
            } else {
                //初始化Application Bean
                application = new Application(
                        LauncherUtils.getFromIconPack(this, appIcon, packageName), //图标
                        resolveInfo.loadLabel(getPackageManager()), //名称
                        isSystemApp, //是否为系统应用
                        appIntent, //启动Intent
                        resolveInfo.activityInfo.packageName); //包名
            }
            //如果使用按拼音排序
            if (isSortByPinyin) {
                String pinyin = PinyinUtils.getPingYin(appLabel);
                String sortString = pinyin.substring(0, 1).toUpperCase();
                //如果是字母开头
                if (sortString.matches("[A-Za-z]")) {
                    application.setLetters(sortString.toUpperCase());
                //否则设置Label为#
                } else {
                    application.setLetters("#");
                }
            }
            //如果启用了工具箱功能
            if (isSimpleList) {
                //排除自身和工具箱以及非系统应用
                if(!appLabel.equals(getString(R.string.app_name)) && isSystemApp || appLabel.equals(getString(R.string.trd_apps))){
                    mApplicationList.add(application);
                }
            } else {
                //否则只排除自身和工具箱
                if(!appLabel.equals(getString(R.string.app_name)) && !appLabel.equals(getString(R.string.trd_apps))){
                    mApplicationList.add(application);
                }
            }
        }
        //如果使用按拼音排序
        if (isSortByPinyin) {
            mApplicationList.sort(mComparator);
        }
        AppRecyclerView mAppRecyclerView = findViewById(R.id.app_list);
        AppAdapter appAdapter;
        //如果是网格布局
        if(app_list_style.equals("grid")){
            appAdapter = new AppAdapter(mApplicationList, 1, isFocusItemZoom);
            //      设置布局管理器
            mAppRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
            //      设置适配器
            mAppRecyclerView.setAdapter(appAdapter);
        }else{
            appAdapter = new AppAdapter(mApplicationList, 0, isFocusItemZoom);
            //列表布局
            mAppRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mAppRecyclerView.setAdapter(appAdapter);
        }
        appAdapter.setOnItemSelectCallback(this);
    }

    @SuppressLint("NonConstantResourceId")
    private void showMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.app_option,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.menu_about_phone:
                    Log.e("Device Info","Device SDK="+Build.VERSION.SDK_INT);
                    if (Build.VERSION.SDK_INT >= 28){
                        Intent ia = new Intent();
                        ia.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ia.setClassName("com.android.settings",
                                "com.android.settings.Settings$MyDeviceInfoActivity");
                        startActivity(ia);
                    }else{
                        Intent ia = new Intent();
                        ia.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ia.setClassName("com.android.settings",
                                "com.android.settings.Settings$DeviceInfoSettingsActivity");
                        startActivity(ia);}
                    break;
                case R.id.menu_launcher_option:
                    Intent menu = new Intent(AppListActivity.this, MenuActivity.class);
                    menu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(menu);
                    finish();
                    break;
                case R.id.menu_volume_changer:
                    Intent vol_it = new Intent(AppListActivity.this, VolumeChanger.class);
                    vol_it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(vol_it);
                    break;
                case R.id.menu_app_sort_pinyin:
                    isSortByPinyin = true;
                    sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",true).apply();
                    loadApp();
                    break;
                case R.id.menu_app_sort_default:
                    isSortByPinyin = false;
                    sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",false).apply();
                    loadApp();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_option,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_about_phone:
                if (Build.VERSION.SDK_INT >= 28){
                    Log.e("Device Info","Device SDK="+Build.VERSION.SDK_INT);
                    Intent ia = new Intent();
                    ia.setClassName("com.android.settings",
                            "com.android.settings.Settings$MyDeviceInfoActivity");
                    startActivity(ia);
                }else{
                    Log.e("Device Info","Device SDK="+Build.VERSION.SDK_INT);
                    Intent ia = new Intent();
                    ia.setClassName("com.android.settings",
                        "com.android.settings.Settings$DeviceInfoSettingsActivity");
                    startActivity(ia);}
                break;
            case R.id.menu_launcher_option:
                Intent menu = new Intent(AppListActivity.this, MenuActivity.class);
                startActivity(menu);
                finish();
                break;
            case R.id.menu_volume_changer:
                Intent vol_it = new Intent(AppListActivity.this, VolumeChanger.class);
                startActivity(vol_it);
                break;
            case R.id.menu_app_sort_pinyin:
                isSortByPinyin = true;
                sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",true).apply();
                loadApp();
                break;
            case R.id.menu_app_sort_default:
                isSortByPinyin = false;
                sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",false).apply();
                loadApp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPkgDelReceiver != null && HideAppReceiver != null){
            getApplicationContext().unregisterReceiver(mPkgDelReceiver);
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(HideAppReceiver);
            mPkgDelReceiver = null;
            HideAppReceiver = null;
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
