package com.zlrab.plugin.transform

import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.build.api.transform.QualifiedContent
import com.zlrab.plugin.extension.ConfuseExtension
import org.gradle.api.Project

class JavassistTransform extends BaseTransform {

    private Project project;

    JavassistTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return JavassistTransform.class.getName();
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_LIBRARY;
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transformAll(File input) {
        ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
        if (confuseExtension.autoJavassistInject) {
            //TODO
        }
    }
}