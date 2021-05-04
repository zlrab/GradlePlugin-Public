package com.zlrab.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask
import com.zlrab.core.ModuleType
import com.zlrab.plugin.extension.ConfuseExtension
import com.zlrab.plugin.transform.ConfuseTransform
import com.zlrab.plugin.transform.JavassistTransform
import com.zlrab.plugin.work.LogManager
import com.zlrab.plugin.work.TaskTrackManager
import com.zlrab.plugin.work.confuse.ConfusePlusManager
import com.zlrab.plugin.work.confuse.ResConfusePlusManager
import com.zlrab.tool.Enhance
import com.zlrab.tool.LogTool
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState

import java.lang.reflect.Method
import java.util.function.BiConsumer
import java.util.function.Consumer

class MainPlugin implements Plugin<Project> {

    public static BaseConf baseConf = new BaseConf()

    public static Logger logger

    MainPlugin() {
    }

    @Override
    void apply(Project project) {


        logger = project.logger

        initBaseConf(project)

        createExtensions(project)

        createTransform(project)

        createTask(project)

        lifeCycleCall(project)
    }

    void lifeCycleCall(Project project) {
        //开始配置前
        project.afterEvaluate {
            LogTool.e("ZLRAB = afterEvaluate")
        }
        //配置准备就绪
        project.gradle.projectsEvaluated {
            LogTool.e("ZLRAB = projectsEvaluated")
            ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
            LogTool.d(confuseExtension.toString())
            TaskTrackManager.initTaskTrackManager(project, confuseExtension)
            ConfusePlusManager.initConfusePlusManager(confuseExtension)
            ResConfusePlusManager.initResConfusePlusManager(confuseExtension)
            //初始化日志帮助类
            LogManager.initLogMappingManager(confuseExtension)
        }
        //构建结束 释放资源
        project.gradle.buildFinished {
            LogTool.e("ZLRAB = buildFinished")
        }

        project.gradle.addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {

            }

            @Override
            void afterExecute(Task task, TaskState taskState) {
                LogTool.d("afterExecute task : "+task.getName())
                if (task.getName().startsWith("generateJsonModel")) {
                    boolean confuse = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME).isConfuseNativeString()
                    LogTool.d("afterExecute run generateJsonModel open = "+confuse)
                    TaskTrackManager.getInstance().processCSourceCode(task)
                }
            }
        })
    }


    private void initBaseConf(Project project) {
        baseConf.workRootDir = new File(project.buildDir, Const.WORK_ROOT_DIR_NAME)
        baseConf.workBundlesDir = new File(baseConf.workRootDir, Const.WORK_BUNDLES_DIR_NAME)

        boolean isApplicationModule = project.plugins.hasPlugin(ModuleType.APPLICATION.pluginName)

        boolean isLibraryModule = project.plugins.hasPlugin(ModuleType.LIBRARY.pluginName)

        if (isApplicationModule && isLibraryModule)
            throw new RuntimeException("无法确定模块类型，[ $ModuleType.APPLICATION.pluginName ] 和 [ $ModuleType.LIBRARY.pluginName ] 只能二选一")
        if (isApplicationModule) {
            baseConf.moduleType = ModuleType.APPLICATION
            throw new RuntimeException("Unsupported module type")
        }
        if (isLibraryModule) baseConf.moduleType = ModuleType.LIBRARY
        if (baseConf.moduleType == ModuleType.NULL)
            throw new RuntimeException("无法确定模块类型，[ $ModuleType.APPLICATION.pluginName ] 和 [ $ModuleType.LIBRARY.pluginName ] 只能二选一")
    }

    private void createExtensions(Project project) {
        project.extensions.create(ConfuseExtension.CONF_CONFUSE_NAME, ConfuseExtension)
    }

    private void createTransform(Project project) {
        def android = null
        switch (baseConf.moduleType) {
            case ModuleType.APPLICATION:
                android = project.extensions.getByType(AppExtension)
                break
            case ModuleType.LIBRARY:
                android = project.extensions.getByType(LibraryExtension)
                break
        }
        def javassistTransform = new JavassistTransform(project)
        android.registerTransform(javassistTransform)

        def confuseTransform = new ConfuseTransform(project, android)
        android.registerTransform(confuseTransform)
    }

    private void createTask(Project project) {
//        createTask(project, Const.INIT_TASK_NAME, InitTask, "assembleDebug")
//        createTask(project, Const.ENCRYPTED_STRING_TASK_NAME, EncryptedStringTask, Const.INIT_TASK_NAME)
//        createTask(project, Const.PROGUARD_PLUS_TASK_NAME, ProguardPlusTask, Const.INIT_TASK_NAME)
//        createTask(project, Const.ENCRYPTED_STRING_AND_PROGUARD_PLUS_TASK_NAME, EncryptedStringAndProguardPlusTask, Const.ENCRYPTED_STRING_TASK_NAME)
    }

    static void reconstructionDir(def dir) {
        FileUtils.deleteDirectory(dir)
        FileUtils.forceMkdir(dir)
    }

    private static void createTask(Project project, String taskName, Object obj, String dependsOn) {
        if (project.tasks.findByName(taskName) == null) {
            def task = project.task(taskName, type: obj)
            task.dependsOn "$dependsOn" //TODO 依赖多个task的执行循序问题
        }
    }

    static void printlnReturn(Object obj) {
        Method[] methods = obj.getClass().methods
        for (Method method : methods) {
            if (method.name.startsWith("get") && method.parameterCount == 0) {
                method.setAccessible(true)
                Object result = method.invoke(obj)
                println("printlnReturn\tClass = " + obj.class + "\tmethodName = " + method.name + "\tresult = " + result)
            }
        }
    }
}