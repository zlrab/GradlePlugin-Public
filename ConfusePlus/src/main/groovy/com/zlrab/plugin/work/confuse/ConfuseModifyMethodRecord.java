package com.zlrab.plugin.work.confuse;

import com.zlrab.tool.RandomTool;

/**
 * @author zlrab
 * @date 2020/12/30 21:15
 */
public class ConfuseModifyMethodRecord {
    /**
     * example : 10
     */
    private int access;
    /**
     * example : attachBaseContext
     */
    private String oldName;
    /**
     * example : (Landroid/content/Context;)V
     */
    private String descriptor;
    /**
     * 方法新的名字
     */
    private String newName;
    /**
     * 操作方式:  remove , confuse
     */
    private String action;

    public ConfuseModifyMethodRecord(int access, String oldName, String descriptor, String action) {
        this(access, oldName, descriptor, action, RandomTool.randomName());
    }

    public ConfuseModifyMethodRecord(int access, String oldName, String descriptor, String action, String newName) {
        this.access = access;
        this.oldName = oldName;
        this.descriptor = descriptor;
        this.newName = newName;
        this.action = action;
    }

    public int getAccess() {
        return access;
    }

    public String getOldName() {
        return oldName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getNewName() {
        return newName;
    }

    public boolean isRemove() {
        return "remove".equals(action);
    }

    public boolean isConfuse() {
        return "confuse".equals(action);
    }

    @Override
    public String toString() {
        return "ConfuseModifyMethodRecord{" +
                "access=" + access +
                ", oldName='" + oldName + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", newName='" + newName + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
