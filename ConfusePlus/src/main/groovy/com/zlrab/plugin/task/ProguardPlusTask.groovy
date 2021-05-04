package com.zlrab.plugin.task

import com.zlrab.plugin.MainPlugin
import com.zlrab.plugin.MainTask
import com.zlrab.plugin.extension.ConfuseExtension
import com.zlrab.plugin.work.confuse.ConfusePlusImpl
import com.zlrab.tool.FileTool
import org.gradle.api.Action
import org.gradle.api.tasks.TaskAction

class ProguardPlusTask extends MainTask {

    @Override
    void init() {

    }

    @TaskAction
    run() {
        ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
        if (confuseExtension.isConfuseComponent())
            throw new RuntimeException("Obfuscation task and automatic obfuscation Transform cannot coexist." +
                    " Disable method: sdkActionSwitch {confuseComponent = false}")
        ConfusePlusImpl confusePlus = new ConfusePlusImpl(confuseExtension)
        confusePlus.processManifest(MainPlugin.baseConf.manifestFile)
        List<File> processFileList = new ArrayList<>();
        MainPlugin.baseConf.classesDirList.each { classDir ->
            FileTool.traversingFile(classDir, new Action<File>() {
                @Override
                void execute(File file) {
                    processFileList.add(file)
                }
            })
        }
        processFileList.each { file -> confusePlus.processClass(file) }
        confusePlus.end()
    }
}