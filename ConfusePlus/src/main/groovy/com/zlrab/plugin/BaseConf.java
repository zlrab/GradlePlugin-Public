package com.zlrab.plugin;

import com.zlrab.core.ModuleType;
import com.zlrab.plugin.extension.ConfuseExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BaseConf {
    File workRootDir;

    File workBundlesDir;

    List<File> classesDirList = new ArrayList<>();

    public static ModuleType moduleType = ModuleType.NULL;

    public File getWorkRootDir() {
        return workRootDir;
    }

    public void setWorkRootDir(File workRootDir) {
        this.workRootDir = workRootDir;
        MainPlugin.reconstructionDir(this.workRootDir);
    }

    public File getWorkBundlesDir() {
        return workBundlesDir;
    }

    public void setWorkBundlesDir(File workBundlesDir) {
        this.workBundlesDir = workBundlesDir;
        MainPlugin.reconstructionDir(this.workBundlesDir);
    }

    public List<File> getClassesDirList() {
        return classesDirList;
    }

    public File getManifestFile() {
        return new File(workBundlesDir, "AndroidManifest.xml");
    }

    public File getResDir() {
        return new File(workBundlesDir, "res");
    }

    public File getAssetsDir() {
        return new File(workBundlesDir, "assets");
    }

    public File getWriteConfuseMappingFile() {
        File mappingsDir = new File(workRootDir, "mappings");
        if (!mappingsDir.exists()) mappingsDir.mkdirs();
        return new File(mappingsDir, ConfuseExtension.CONF_CONFUSE_NAME + "_mapping.txt");
    }

    public File getWriterLogFile() {
        File mappingsDir = new File(workRootDir, "mappings");
        if (!mappingsDir.exists()) mappingsDir.mkdirs();
        return new File(mappingsDir, Const.WORK_ROOT_DIR_NAME + "_log.txt");
    }

    public File getJniSourceWorkFile(File originalSourceFile) {
        String oldName = originalSourceFile.getName();
        File mappingsDir = new File(workRootDir, "cmake");
        if (!mappingsDir.exists()) mappingsDir.mkdirs();
        File workFile = new File(mappingsDir, oldName);
        if (workFile.exists()) workFile.delete();
        return workFile;
    }

    public static ModuleType getModuleType() {
        return moduleType;
    }

    public static void setModuleType(ModuleType moduleType) {
        BaseConf.moduleType = moduleType;
    }
}