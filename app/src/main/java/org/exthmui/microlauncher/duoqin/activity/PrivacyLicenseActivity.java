package org.exthmui.microlauncher.duoqin.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.databinding.ActivityPrivacyPolicyBinding;
import org.exthmui.microlauncher.duoqin.utils.BuglyUtils;

import es.dmoral.toasty.Toasty;

public class PrivacyLicenseActivity extends AppCompatActivity {
    private final String LICENSE_URL = "file:///android_asset/privacy.html";
    private ActivityPrivacyPolicyBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(getPackageName()+"_preferences", Context.MODE_PRIVATE);
        binding.webview.loadUrl(LICENSE_URL);
        binding.accept.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean("bugly_init", true).apply();
            BuglyUtils.initBugly(this);
            finish();
        });
        binding.reject.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean("disagree", true).apply();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Toasty.info(this, R.string.privacy_read_first, Toasty.LENGTH_SHORT).show();
    }
}
