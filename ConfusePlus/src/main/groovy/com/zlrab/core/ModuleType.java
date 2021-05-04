package com.zlrab.core;


/**
 * @author zlrab
 * @date 2020/12/25 20:19
 */
public enum ModuleType {
    APPLICATION("com.android.application"), LIBRARY("com.android.library"), NULL("null");
    private String pluginName;

    public String getPluginName() {
        return pluginName;
    }

    ModuleType(String pluginName) {
        this.pluginName = pluginName;
    }

    public static ModuleType index(String pluginName) {
        ModuleType[] values = ModuleType.values();
        for (ModuleType moduleType : values) {
            if (moduleType.pluginName.equals(pluginName)) return moduleType;
        }
        return NULL;
    }
}
