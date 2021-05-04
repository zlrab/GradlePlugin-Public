package com.zlrab.plugin.work;

import com.android.build.gradle.external.gson.NativeBuildConfigValue;
import com.android.build.gradle.external.gson.NativeLibraryValue;
import com.android.build.gradle.external.gson.NativeSourceFileValue;
import com.android.build.gradle.external.gson.PlainFileGsonTypeAdaptor;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.tasks.ExternalNativeBuildJsonTask;
import com.android.build.gradle.tasks.ExternalNativeBuildTaskUtils;
import com.android.build.gradle.tasks.ExternalNativeJsonGenerator;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zlrab.plugin.MainPlugin;
import com.zlrab.plugin.Reflect;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.proxy.ConfuseNativeStringProxy;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionListener;
import org.gradle.api.tasks.TaskState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import groovy.json.internal.Charsets;

public class TaskTrackManager implements TaskExecutionListener {
    private static TaskTrackManager sTaskTrackManager;

    private Map<String, Long> taskStartRunTimeRecordMap = new HashMap<>();

    private ConfuseExtension confuseExtension;

    private Gson gson = new GsonBuilder().registerTypeAdapter(File.class, new PlainFileGsonTypeAdaptor()).setPrettyPrinting().create();

    private TaskTrackManager(Project project, ConfuseExtension confuseExtension) {
        project.getGradle().addListener(this);
        this.confuseExtension = confuseExtension;
    }

    public static void initTaskTrackManager(Project project, ConfuseExtension confuseExtension) {
        if (sTaskTrackManager == null) {
            sTaskTrackManager = new TaskTrackManager(project, confuseExtension);
        } else {
            LogTool.w("TaskTrackManager.initTaskTrackManager(org.gradle.api.Project) Only allow to call once, do not call repeatedly");
        }
    }

    public static TaskTrackManager getInstance() {
        return sTaskTrackManager;
    }

    @Override
    public void beforeExecute(Task task) {
        taskStartRunTimeRecordMap.put(task.getName(), System.currentTimeMillis());
    }

    @Override
    public void afterExecute(Task task, TaskState taskState) {
        if (task.getName().startsWith("generateJsonModel") && confuseExtension.isConfuseNativeString()) {

        }
//        processCSourceCode((ExternalNativeCleanTask_Decorated) task);
    }

    public void processCSourceCode(ExternalNativeBuildJsonTask externalNativeBuildJsonTask) {
        LogTool.d("start processCSourceCode");

        ConfuseNativeStringProxy confuseNativeStringProxy = EncryptionStringClassManager.getInstance().getNativeProxy();
        if (!confuseNativeStringProxy.check()) {
            LogTool.d("confuseNativeStringProxy check failed");
            return;
        }
        ExternalNativeJsonGenerator externalNativeJsonGenerator = externalNativeBuildJsonTask.getExternalNativeJsonGenerator();
        try {
            Field abiField = ExternalNativeJsonGenerator.class.getDeclaredField("abis");
            abiField.setAccessible(true);
            List<Abi> abiList = (List<Abi>) abiField.get(externalNativeJsonGenerator);
            File jsonFolder = externalNativeJsonGenerator.getJsonFolder();
            for (Abi abi : abiList) {
                File androidGradleBuildJsonFile = ExternalNativeBuildTaskUtils.getOutputJson(jsonFolder, abi.getName());

                Map<String, String> srcMap = new HashMap<>();
                //High-risk operations, add cautiously
                Map<String, String> confuseCMethodNameMap = new HashMap<>();
                confuseCMethodNameMap.put("confuse_method_1", RandomTool.randomName());
                confuseCMethodNameMap.put("confuse_method_2", RandomTool.randomName());
                confuseCMethodNameMap.put("confuse_method_3", RandomTool.randomName());
                confuseCMethodNameMap.put("confuse_method_4", RandomTool.randomName());
                confuseCMethodNameMap.put("confuse_method_5", RandomTool.randomName());
                confuseCMethodNameMap.put("confuse_field_1", RandomTool.randomName());

                NativeBuildConfigValue nativeBuildConfigValue = gson.fromJson(new FileReader(androidGradleBuildJsonFile), NativeBuildConfigValue.class);
                Map<String, NativeLibraryValue> libraries = nativeBuildConfigValue.libraries;
                for (Map.Entry<String, NativeLibraryValue> entry : libraries.entrySet()) {
                    NativeLibraryValue nativeLibraryValue = entry.getValue();
                    Collection<NativeSourceFileValue> files = nativeLibraryValue.files;
                    for (NativeSourceFileValue nativeSourceFileValue : files) {
                        File srcJavaFile = nativeSourceFileValue.src;
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(srcJavaFile));
                        List<String> sourceList = new ArrayList<>();
                        Stream<String> lines = bufferedReader.lines();
                        lines.forEach(sourceList::add);
                        //替换字段
                        sourceList.forEach(line -> {
                            if (line.startsWith("/") || line.startsWith("*") || !line.contains("\""))
                                return;
                            for (Map.Entry<String, String> modifyEntry : confuseNativeStringProxy.getModifyMap().entrySet()) {
                                if (line.contains(ConversionTool.stringName(modifyEntry.getKey()))) {
                                    String newLine = line.replace(ConversionTool.stringName(modifyEntry.getKey()), ConversionTool.stringName(modifyEntry.getValue()));
                                    LogTool.d("native replace string oldLine = " + line + "\tnewLine = " + newLine);
                                    sourceList.set(sourceList.indexOf(line), newLine);
                                }
                            }
                        });

                        sourceList.forEach(line->{
                            for (Map.Entry<String, String> modifyEntry : confuseCMethodNameMap.entrySet()) {
                                if (line.contains(modifyEntry.getKey())) {
                                    String newLine = line.replace(modifyEntry.getKey(), modifyEntry.getValue());
                                    sourceList.set(sourceList.indexOf(line), newLine);
                                }
                            }
                        });
//                        //混淆字段
//                        int size = sourceList.size();
//                        LogTool.d("sourceList size = " + size);
//                        for (int index = 0; index < size; index++) {
//                            List<String> strings = readSourceLineStr(sourceList.get(index));
//                            if (strings != null && strings.size() > 0) {
//                                for (String str : strings) {
//                                    String key = RandomTool.randomName();
//                                    String encode = confuseNativeStringProxy.encode(str, key);
//                                    String decode = confuseNativeStringProxy.decode(encode, key);
//                                    if (str.equals(decode)) {
//                                        String callMethodLineBuilder = confuseNativeStringProxy.getNativeDecodeMethodName() +
//                                                "(" +
//                                                ConversionTool.stringName(encode) +
//                                                "," +
//                                                ConversionTool.stringName(key) +
//                                                ")";
//                                        sourceList.set(index, sourceList.get(index).replace(ConversionTool.stringName(str), callMethodLineBuilder));
//                                    }
//
//                                }
//                            }
//
//                        }

                        StringBuilder stringBuilder = new StringBuilder();
                        sourceList.forEach(s -> {
                            stringBuilder.append(s).append("\r\n");
                            System.out.println("line = " + s);
                        });
                        File jniSourceWorkFile = MainPlugin.baseConf.getJniSourceWorkFile(srcJavaFile);
                        nativeSourceFileValue.src = jniSourceWorkFile;
                        String srcJavaPath = srcJavaFile.getAbsolutePath().replace(File.separator, "/");
                        String jniWorkPath = jniSourceWorkFile.getAbsolutePath().replace(File.separator, "/");
                        LogTool.d("oldCFile = " + srcJavaPath + "\tnewCFile = " + jniWorkPath);

                        srcMap.put(srcJavaPath.substring(srcJavaPath.indexOf("/")), jniWorkPath.substring(jniWorkPath.indexOf("/")));

                        String srcJavaPathReplace = srcJavaPath.replace("/", "\\");
                        String jniWorkPathReplace = jniWorkPath.replace("/", "\\");

                        srcMap.put(srcJavaPathReplace.substring(srcJavaPathReplace.indexOf("\\")),
                                jniWorkPathReplace.substring(jniWorkPathReplace.indexOf("\\")));

                        Files.write(stringBuilder.toString(), jniSourceWorkFile, Charsets.UTF_8);
                    }
                }
                String newContent = gson.toJson(nativeBuildConfigValue);
                Files.write(newContent, androidGradleBuildJsonFile, Charsets.UTF_8);

                srcMap.forEach((s, s2) -> LogTool.d("oldSign = " + s + "\tnewSign = " + s2));

                Enhance.forEach(androidGradleBuildJsonFile.getParentFile().listFiles(file -> !file.getName().equals(androidGradleBuildJsonFile.getName()) && file.isFile()), file -> {
                    LogTool.d("workFile = " + file.getAbsolutePath());
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        StringBuilder stringBuilder = new StringBuilder();
                        bufferedReader.lines().forEach(line -> {
                            String newLine = line;
                            for (Map.Entry<String, String> entry : srcMap.entrySet()) {
                                if (line.contains(entry.getKey())) {
                                    newLine = newLine.replace(entry.getKey(), entry.getValue());
                                }
                            }
                            stringBuilder.append(newLine).append("\r\n");
                        });
                        Files.write(stringBuilder.toString(), file, Charsets.UTF_8);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Throwable e) {
            LogTool.e("processCSourceCode error", e);
        }
    }

    /**
     * 读取源码上的字符串
     *
     * @param line
     * @return
     */
    private List<String> readSourceLineStr(String line) {
        if (line == null || line.length() == 0 || line.startsWith("/") || line.startsWith("*") || !line.contains("\""))
            return null;
        List<String> strList = new ArrayList<>();
        boolean flag = false;
        StringBuilder strBuilder = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"' && strBuilder.length() == 0) {
                flag = true;
            }
            if (c == '"' && strBuilder.length() != 0) {
                flag = false;
                String s = strBuilder.toString();
                strList.add(s.substring(1));
                strBuilder.delete(0, strBuilder.length());
            }
            if (flag) {
                strBuilder.append(c);
            }
        }
        return strList;
    }

    public void beforeRun(String taskName) {

//TODO
    }

    public void afterRun(String taskName) {
//TODO
    }
}
