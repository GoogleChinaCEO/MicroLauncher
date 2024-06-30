package org.exthmui.microlauncher.duoqin.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.databinding.ActivityPrivacyPolicyBinding;
import org.exthmui.microlauncher.duoqin.utils.BuglyUtils;
import org.exthmui.microlauncher.duoqin.utils.Constants;
import org.exthmui.microlauncher.duoqin.utils.LauncherSettingsUtils;

import es.dmoral.toasty.Toasty;

public class PrivacyLicenseActivity extends AppCompatActivity {
    private final String LICENSE_URL = "file:///android_asset/privacy.html";
    private ActivityPrivacyPolicyBinding binding;
    private LauncherSettingsUtils settingsUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settingsUtils = LauncherSettingsUtils.getInstance(this);
        binding.webview.loadUrl(LICENSE_URL);
        binding.accept.setOnClickListener(v -> {
            settingsUtils.putBoolean("bugly_init", true);
            BuglyUtils.initBugly(this);
            finish();
        });
        binding.reject.setOnClickListener(v -> {
            settingsUtils.putBoolean("disagree", true);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Toasty.info(this, R.string.privacy_read_first, Toasty.LENGTH_SHORT).show();
    }
}
