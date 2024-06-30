package org.exthmui.microlauncher.duoqin;

import android.app.Application;

import org.exthmui.microlauncher.duoqin.utils.BuglyUtils;
import org.exthmui.microlauncher.duoqin.utils.LauncherSettingsUtils;

/**
 * @author Maribel
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LauncherSettingsUtils.getInstance(this)
                .getBoolean("bugly_init",false)) {
            BuglyUtils.initBugly(this);
        }
    }
}
