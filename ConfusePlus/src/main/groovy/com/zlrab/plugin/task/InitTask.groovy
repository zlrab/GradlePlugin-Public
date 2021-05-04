package com.zlrab.plugin.task

import com.zlrab.core.ModuleType
import com.zlrab.plugin.MainPlugin
import com.zlrab.plugin.MainTask
import com.zlrab.plugin.Reflect
import com.zlrab.tool.FileTool
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskAction

/**
 * @author zlrab* @date 2020/12/24 12:20
 */
class InitTask extends MainTask {
    @Override
    void init() {

    }

    @TaskAction
    run() {
        switch (MainPlugin.baseConf.moduleType) {
            case ModuleType.APPLICATION:
                initWorkForApplication()
                break
            case ModuleType.LIBRARY:
                initWorkForLibrary()
                break
            case ModuleType.NULL:
                throw new GradleScriptException("Unable to determine the module type, need com.android.application or com.android.library")
        }
    }

    void initWorkForLibrary() {
        File bundleDir = null
        androidLibraryExtension.libraryVariants.all { variantsLibraryVariantImpl ->
            if ("debug".equalsIgnoreCase(variantsLibraryVariantImpl.buildType.name)) {
                def libraryVariantData = Reflect.on(variantsLibraryVariantImpl).call("getVariantData").get()
                bundleDir = libraryVariantData.scope.baseBundleDir
            }
        }

        if (!bundleDir.exists())
            throw new RuntimeException("Cannot find the operation file, check whether the directory and its subfiles exist : $bundleDir.absolutePath")
        bundleDir.listFiles().each { childFile ->
            File out = new File(MainPlugin.baseConf.workBundlesDir, childFile.name)
            if (childFile.isFile()) {
                FileUtils.copyFile(childFile, out)
                if (FilenameUtils.isExtension(out.name, "jar")) {
                    def outDir = new File(out.getParentFile(), FilenameUtils.getBaseName(out.getName()))
                    FileTool.unZip(out, outDir, true)
                    MainPlugin.baseConf.classesDirList.add(outDir)
                }
            } else {
                FileUtils.copyDirectory(childFile, out)
            }
        }
    }

    void initWorkForApplication() {
        throw new RuntimeException("Stub")
    }
}
