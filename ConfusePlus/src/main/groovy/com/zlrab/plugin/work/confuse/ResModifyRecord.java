package com.zlrab.plugin.work.confuse;

import com.zlrab.core.ResType;

import java.io.File;
import java.util.Objects;

/**
 * @author zlrab
 * @date 2021/1/8 17:18
 */
public class ResModifyRecord {
    /**
     * {@link com.zlrab.core.ResType}
     */
    private ResType resType;
    /**
     * 旧的资源引用名，不是文件名
     * example : app_name
     */
    private String oldName;
    /**
     * 新的资源引用名，不是文件名
     * example : new_app_name
     */
    private String newName;
    private File resFile;
    /**
     * true:这个资源是文件
     * false : 这个资源是一个值,如:string color integer
     */
    private boolean isFile;

    public ResModifyRecord(ResType resType, String oldName, String newName, File resFile, boolean isFile) {
        this.resType = resType;
        this.oldName = oldName;
        this.newName = newName;
        this.resFile = resFile;
        this.isFile = isFile;
    }

    public ResType getResType() {
        return resType;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public File getResFile() {
        return resFile;
    }

    public ResModifyRecord setResFile(File resFile) {
        this.resFile = resFile;
        return this;
    }

    public boolean isFile() {
        return isFile;
    }

    public ResModifyRecord setFile(boolean file) {
        isFile = file;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResModifyRecord that = (ResModifyRecord) o;

        if (isFile != that.isFile) return false;
        if (resType != that.resType) return false;
        if (!oldName.equals(that.oldName)) return false;
        return resFile.equals(that.resFile);
    }

    @Override
    public int hashCode() {
        int result = resType.hashCode();
        result = 31 * result + oldName.hashCode();
        result = 31 * result + resFile.hashCode();
        result = 31 * result + (isFile ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResModifyRecord{" +
                "resType=" + resType +
                ", oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                ", resFile=" + resFile +
                ", isFile=" + isFile +
                '}';
    }
}
