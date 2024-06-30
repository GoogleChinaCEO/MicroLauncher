package org.exthmui.microlauncher.duoqin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LauncherSettingsUtils {

    private static LauncherSettingsUtils utils;
    private Context context;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public LauncherSettingsUtils(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(Constants.launcherSettingsPref,
                Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static LauncherSettingsUtils getInstance(Context context) {
        if (utils == null) {
            utils = new LauncherSettingsUtils(context);
        }
        return utils;
    }

    /**
     * 向SP存入指定key对应的数据
     * 其中value可以是String、boolean、float、int、long等各种基本类型的值
     * @param key
     * @param value
     */
    public boolean putString(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean putFloat(String key, float value) {
        editor.putFloat(key, value);
        return editor.commit();
    }

    public boolean putInt(String key, int value) {
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean putLong(String key, long value) {
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 清空SP里所以数据
     */
    public boolean clear() {
        editor.clear();
        return editor.commit();
    }

    /**
     * 删除SP里指定key对应的数据项
     * @param key
     */
    public boolean remove(String key) {
        editor.remove(key);
        return editor.commit();
    }

    /**
     * 获取SP数据里指定key对应的value。如果key不存在，则返回默认值defValue。
     * @param key
     * @param defValue
     * @return
     */
    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    /**
     * 判断SP是否包含特定key的数据
     * @param key
     * @return
     */
    public boolean contains(String key){
        return preferences.contains(key);
    }

}
