package com.zlrab.plugin.transform;

import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.work.EncryptedStringImpl;
import com.zlrab.tool.FileTool;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Set;


/**
 * @author zlrab
 * @date 2020/12/25 19:44
 */
@Deprecated
public class EncryptedStringTransform extends BaseTransform {

    private Project project;
    private EncryptedStringImpl encryptedString;
    private ConfuseExtension confuseExtension;

    public EncryptedStringTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return EncryptedStringTransform.class.getName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_LIBRARY;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transformAll(File input) {
        //TODO 在上一个transform迁移class后，不会出现在后续transform中
        if (confuseExtension == null) {
            confuseExtension = (ConfuseExtension) project.getExtensions().findByName(ConfuseExtension.CONF_CONFUSE_NAME);
        }
        if (encryptedString == null) {
            encryptedString = new EncryptedStringImpl(confuseExtension);
        }

        if (confuseExtension.isAutoEncryptionString()) {
            FileTool.traversingFile(input, file -> {
                try {
                    encryptedString.processClass(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("An exception occurred while processing the original class");
                }
            });
        }
    }
}
