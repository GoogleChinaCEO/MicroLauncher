package org.exthmui.microlauncher.duoqin.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LauncherViewModel extends ViewModel {

    // MainActivity 设置
    private MutableLiveData<Boolean> enableLunar;
    public MutableLiveData<Boolean> getEnableLunar() {
        if (enableLunar == null) {
            enableLunar = new MutableLiveData<>();
            enableLunar.postValue(true);
        }
        return enableLunar;
    }

    private MutableLiveData<Boolean> enableCarrier;
    public MutableLiveData<Boolean> getEnableCarrier() {
        if (enableCarrier == null) {
            enableCarrier = new MutableLiveData<>();
            enableCarrier.postValue(true);
        }
        return enableCarrier;
    }

    private MutableLiveData<Boolean> enableCounter;
    public MutableLiveData<Boolean> getEnableCounter() {
        if (enableCounter == null) {
            enableCounter = new MutableLiveData<>();
            enableCounter.postValue(false);
        }
        return enableCounter;
    }

    private MutableLiveData<String> clockLocate;
    public MutableLiveData<String> getClockLocate() {
        if (clockLocate == null) {
            clockLocate = new MutableLiveData<>();
            clockLocate.postValue("left");
        }
        return clockLocate;
    }

    private MutableLiveData<String> poundFuncName;
    public MutableLiveData<String> getPoundFuncName() {
        if (poundFuncName == null) {
            poundFuncName = new MutableLiveData<>();
            poundFuncName.postValue("volume");
        }
        return poundFuncName;
    }

    private MutableLiveData<String> clockTextSize;
    public MutableLiveData<String> getClockTextSize() {
        if (clockTextSize == null) {
            clockTextSize = new MutableLiveData<>();
            clockTextSize.postValue("44");
        }
        return clockTextSize;
    }

    private MutableLiveData<Boolean> enableXiaoAi;
    public MutableLiveData<Boolean> getEnableXiaoAi() {
        if (enableXiaoAi == null) {
            enableXiaoAi = new MutableLiveData<>();
            enableXiaoAi.postValue(true);
        }
        return enableXiaoAi;
    }

    private MutableLiveData<Boolean> enableDialPad;
    public MutableLiveData<Boolean> getEnableDialPad() {
        if (enableDialPad == null) {
            enableDialPad = new MutableLiveData<>();
            enableDialPad.postValue(true);
        }
        return enableDialPad;
    }

    private MutableLiveData<Boolean> enableTTS;
    public MutableLiveData<Boolean> getEnableTTS() {
        if (enableTTS == null) {
            enableTTS = new MutableLiveData<>();
            enableTTS.postValue(false);
        }
        return enableTTS;
    }

    private MutableLiveData<Boolean> enableDarkMode;
    public MutableLiveData<Boolean> getEnableDarkMode() {
        if (enableDarkMode == null) {
            enableDarkMode = new MutableLiveData<>();
            enableDarkMode.postValue(false);
        }
        return enableDarkMode;
    }

}
