package com.zlrab.core

import com.zlrab.plugin.extension.ConfuseExtension
import com.zlrab.plugin.work.LogManager
import com.zlrab.tool.LogTool
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class GradleLifeCycleCallback implements BuildListener, TaskExecutionListener {
    private Project project

    GradleLifeCycleCallback(Project project) {
        this.project = project
        project.beforeEvaluate { beforeProject(project) }
        project.afterEvaluate { afterProject(project) }
        project.gradle.projectsEvaluated {
            ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
            LogTool.e(" project.gradle.projectsEvaluated , confuseExtension = "+confuseExtension.toString())
        }
    }

    @Override
    void buildStarted(Gradle gradle) {
        LogTool.e("ZLRAB=buildStarted")
    }

    @Override
    void settingsEvaluated(Settings settings) {
        LogTool.e("ZLRAB=settingsEvaluated")
    }

    @Override
    void projectsLoaded(Gradle gradle) {
        LogTool.e("ZLRAB=projectsLoaded")
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
        LogTool.e("ZLRAB=projectsEvaluated")
    }

    @Override
    void buildFinished(BuildResult buildResult) {
        LogTool.e("ZLRAB=buildFinished = " + buildResult.action)
    }

    void beforeProject(Project project) {
        LogTool.e("ZLRAB=beforeProject = " + project.projectDir)
    }

    void afterProject(Project project) {
        LogTool.e("ZLRAB=afterProject = " + project.projectDir)
    }

    @Override
    void beforeExecute(Task task) {

    }

    @Override
    void afterExecute(Task task, TaskState taskState) {

    }
}