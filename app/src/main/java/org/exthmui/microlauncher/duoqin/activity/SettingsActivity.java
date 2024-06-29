package org.exthmui.microlauncher.duoqin.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.exthmui.microlauncher.duoqin.R;
import org.exthmui.microlauncher.duoqin.databinding.ActivitySettingsBinding;
import org.exthmui.microlauncher.duoqin.utils.RestartTool;

import java.util.List;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, EasyPermissions.PermissionCallbacks {

    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean reload_flag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = this.getSupportActionBar();
        sharedPreferences = getSharedPreferences(getPackageName()+"_preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding.settingsBack.setOnClickListener(new backFunc());
    }

    class backFunc implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            rebootLauncher(reload_flag);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            rebootLauncher(reload_flag);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HOME){
            rebootLauncher(reload_flag);
            finish();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            rebootLauncher(reload_flag);
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s){
            case "switch_preference_lunar":
            case "switch_preference_carrier_name":
            case "switch_preference_app_list_func":
            case "hide_app":
                binding.settingsBack.setText(getText(R.string.status_reload_launcher));
                reload_flag=true;
                break;
            case "switch_preference_callsms_counter":
                GrantPermissions(new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_SMS},1);
                binding.settingsBack.setText(getText(R.string.status_reload_launcher));
                reload_flag=true;
                break;
            case "preference_pound_func":
                if (sharedPreferences.getString("preference_pound_func","volume").equals("torch")){
                    GrantPermissions(new String[]{Manifest.permission.CAMERA},2);
                }
                break;
            case "dark_mode":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //判断暗色模式是否与设置一致
                    boolean isDarkMode = getApplicationContext().getResources().getConfiguration().isNightModeActive();
                    if (isDarkMode != sharedPreferences.getBoolean("dark_mode",false)){
                        Toasty.warning(getApplicationContext(),R.string.dark_mode_not_match,Toasty.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void GrantPermissions(String[] perms, int code){
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_required_title),
                    code, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case 1:
                editor.putBoolean("switch_preference_callsms_counter",true);
                editor.apply();
                break;
            case 2:
                editor.putString("preference_pound_func","torch");
                editor.apply();
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case 1:
                editor.putBoolean("switch_preference_callsms_counter",false);
                editor.apply();
                break;
            case 2:
                editor.putString("preference_pound_func","volume");
                editor.apply();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    private void rebootLauncher(boolean isReboot){
        if(isReboot){
            RestartTool.restartApp(getApplicationContext(),100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        rebootLauncher(reload_flag);
    }
}
