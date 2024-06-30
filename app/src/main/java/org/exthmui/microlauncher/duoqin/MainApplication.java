package org.exthmui.microlauncher.duoqin;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;

import org.exthmui.microlauncher.duoqin.utils.BuglyUtils;
import org.exthmui.microlauncher.duoqin.utils.Constants;
import org.exthmui.microlauncher.duoqin.utils.LauncherSettingsUtils;

/**
 * @author Maribel
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isBuglyEnabled = LauncherSettingsUtils.getInstance(this)
                .getBoolean("bugly_init",false);
        if (isBuglyEnabled && !BuildConfig.DEBUG) {
            BuglyUtils.initBugly(this);
        }
    }
}
