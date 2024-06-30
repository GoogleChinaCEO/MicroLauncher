package org.exthmui.microlauncher.duoqin.utils;

import static org.exthmui.microlauncher.duoqin.utils.Constants.appExcludePref;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.SPUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.exthmui.microlauncher.duoqin.BuildConfig;
import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.activity.AppHideListActivity;
import org.exthmui.microlauncher.duoqin.bean.AppExcludeBean;
import org.exthmui.microlauncher.duoqin.icons.IconPack;
import org.exthmui.microlauncher.duoqin.icons.providers.IconPackProvider;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class LauncherUtils {

    private static final String TAG = LauncherUtils.class.getSimpleName();

    private static SPUtils sp;

    /**
     * 判断是否为默认桌面
     * @param context 默认Context
     * @return true为默认桌面，false则不是
     */
    public static boolean isDefaultLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = context.getPackageManager().resolveActivity(intent, 0);
        if (info != null) {
            return BuildConfig.APPLICATION_ID.equals(info.activityInfo.packageName);
        }
        return false;
    }

    /**
     * 设置启动器的暗色模式
     * @param context 默认Context
     * @param isDarkMode 是否为暗色模式
     */
    public static void setDarkMode(Context context, boolean isDarkMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (BuildConfig.DEBUG) Log.d(TAG, "setDarkMode: " + isDarkMode);
            AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            Toasty.error(context, R.string.dark_mode_not_support, Toasty.LENGTH_SHORT).show();
        }
    }

    /**
     * 从图标包获取图标
     * @param icon
     * @param packageName
     * @return
     */
    public static Drawable getFromIconPack(Context context,Drawable icon, String packageName) {
        final IconPack iconPack = IconPackProvider.loadAndGetIconPack(context);
        if (iconPack == null) {
            return null;
        }

        final Drawable iconMask = iconPack.getIcon(packageName, null, "");
        return iconMask == null ? icon : iconMask;
    }

    /**
     * 弹出添加需要排除的应用的对话框
     * @param context 默认Context
     * @param appLabel 应用名称
     * @param packageName 应用包名
     * @param isAdd 是否为添加
     */
    public static void showExcludeAppDialog(Context context,String appLabel, String packageName, boolean isAdd) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        if (isAdd) {
            builder.setTitle(context.getString(R.string.hide_app_dialog_title)
                    + " " + appLabel);
            builder.setMessage(R.string.hide_app_dialog_message);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (addExcludeApp(context,packageName)) {
                    Toasty.success(context, R.string.hide_success, Toasty.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.HIDE_APP_ACTION));
                } else {
                    Toasty.error(context, R.string.hide_failed, Toasty.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setTitle(context.getString(R.string.unhide_app_dialog_title)
                    + " " + appLabel);
            builder.setMessage(R.string.unhide_app_dialog_message);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (removeExcludeApp(context, packageName)) {
                    Toasty.success(context, R.string.unhide_success, Toasty.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.HIDE_APP_ACTION));
                } else {
                    Toasty.error(context, R.string.unhide_failed, Toasty.LENGTH_SHORT).show();
                }
            });
        }
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    public static void showClearAllExcludeDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.menu_clear_all_hide)
                .setMessage(R.string.clear_all_app_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    cleanAllExcludeApp(context);
                    sp = SPUtils.getInstance(appExcludePref, Context.MODE_PRIVATE);
                    if (sp.contains("excludeList")) {
                        Toasty.success(context, R.string.unhide_success, Toasty.LENGTH_SHORT).show();
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.HIDE_APP_ACTION));
                    } else {
                        Toasty.error(context, R.string.unhide_failed, Toasty.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * 从SharedPreferences中获取应用排除列表
     * @param context
     * @return 应用包名列表，若无该字段则返回null
     */
    public static List<String> getExcludePackagesName(Context context) {
        sp = SPUtils.getInstance(appExcludePref, Context.MODE_PRIVATE);
        String json = sp.getString("excludeList", null);
        if (json != null) {
            //初始化Gson
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setLenient()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .enableComplexMapKeySerialization()
                    .create();
            AppExcludeBean bean = gson.fromJson(json, AppExcludeBean.class);
            return bean.getExcludePackagesName();
        }
        //若无该字段则新建该字段并返回null
        sp.clear();
        sp.put("excludeList", context.getString(R.string.hide_app_initial_json));
        return null;
    }

    /**
     * 清空应用排除列表
     * @param context
     */
    public static void cleanAllExcludeApp(Context context) {
        sp = SPUtils.getInstance(appExcludePref, Context.MODE_PRIVATE);
        sp.clear();
        sp.put("excludeList", context.getString(R.string.hide_app_initial_json));
    }

    /**
     * 添加应用到排除列表
     * @param context
     * @param excludePackagesName 应用包名
     * @return true为添加成功，false则添加失败
     */
    public static boolean addExcludeApp(Context context, String excludePackagesName) {
        sp = SPUtils.getInstance(appExcludePref, Context.MODE_PRIVATE);
        List<String> appList = getExcludePackagesName(context);
        if (appList != null) {
            int totalCount = appList.size();
            int versionCode = BuildConfig.VERSION_CODE;
            //初始化Gson
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setLenient()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .enableComplexMapKeySerialization()
                    .create();
            //初始化Bean
            AppExcludeBean bean = new AppExcludeBean();
            appList.add(excludePackagesName);
            bean.setExcludePackagesName(appList);
            bean.setTotalCount(totalCount + 1);
            bean.setVersionCode(versionCode);
            String json = gson.toJson(bean);
            sp.put("excludeList", json);
            return true;
        }
        return false;
    }

    /**
     * 从排除列表中移除应用
     * @param context
     * @param excludePackagesName 应用包名
     * @return true为移除成功，false则移除失败
     */
    public static boolean removeExcludeApp(Context context, String excludePackagesName){
        sp = SPUtils.getInstance(appExcludePref, Context.MODE_PRIVATE);
        List<String> appList = getExcludePackagesName(context);
        if (appList != null) {
            int totalCount = appList.size();
            int versionCode = BuildConfig.VERSION_CODE;
            //初始化Gson
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setLenient()
                    .serializeNulls()
                    .setPrettyPrinting()
                    .enableComplexMapKeySerialization()
                    .create();
            //初始化Bean
            AppExcludeBean bean = new AppExcludeBean();
            appList.remove(excludePackagesName);
            bean.setExcludePackagesName(appList);
            bean.setTotalCount(totalCount - 1);
            bean.setVersionCode(versionCode);
            String json = gson.toJson(bean);
            sp.put("excludeList", json);
            return true;
        }
        return false;
    }

    /**
     * 判断是否为DuoQin设备
     * @return true为DuoQin设备，false则不是
     */
    public static boolean isQinDevice() {
        return Constants.DeviceList.contains(Build.DEVICE);
    }

}
