package com.zlrab.plugin

import org.gradle.TaskExecutionRequest
import org.gradle.api.DefaultTask

abstract class MainTask extends DefaultTask {

    def androidLibraryExtension

    MainTask() {
        group 'encrypted'
        androidLibraryExtension = project.extensions.android

        //过滤非目标任务
        def taskRequests = project.gradle.startParameter.taskRequests
        if (taskRequests == null || taskRequests.size() == 0) return
        for (TaskExecutionRequest taskExecutionRequest : taskRequests) {
            def args = taskExecutionRequest.args
            if (args == null || args.size() == 0) return
            for (String arg : args) {
                if (!arg.equalsIgnoreCase(name)) return
            }
        }

        init()
    }

    abstract void init()

    void printlnObject(String tag, Object object) {
        println("[ZLRab] tag = $tag\tobj = $object")
    }
}