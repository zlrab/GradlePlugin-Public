package com.zlrab.plugin.task

import com.zlrab.plugin.MainPlugin
import com.zlrab.plugin.MainTask
import com.zlrab.plugin.extension.ConfuseExtension
import com.zlrab.plugin.work.EncryptedStringImpl
import com.zlrab.tool.FileTool
import org.gradle.api.Action
import org.gradle.api.tasks.TaskAction

/**
 * @author zlrab* @date 2020/12/22 20:52
 */
class EncryptedStringTask extends MainTask {

    @Override
    void init() {
    }

    @TaskAction
    run() {
        ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
        if (confuseExtension.autoEncryptionString)
            throw new RuntimeException("The encrypted string task and the automatic encrypted string Transform cannot coexist," +
                    " the method of disabling: sdkActionSwitch {autoEncryptionString = false}")
        EncryptedStringImpl encryptedString = new EncryptedStringImpl(encryptedStringExtension)
        MainPlugin.baseConf.classesDirList.each { classDir ->
            FileTool.traversingFile(classDir, new Action<File>() {
                @Override
                void execute(File file) {
                    encryptedString.processClass(file)
                }
            })
        }
    }
}
