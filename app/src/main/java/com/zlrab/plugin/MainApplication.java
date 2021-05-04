package com.zlrab.plugin;

import android.app.Application;

/**
 * @author zlrab
 * @date 2020/12/29 14:46
 * @project ZLRab@GradlePlugin
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("ZLRab");
    }
}
