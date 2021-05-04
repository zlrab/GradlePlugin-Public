package com.zlrab.plugin.work;

import com.zlrab.plugin.MainPlugin;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.tool.LogTool;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author zlrab
 * @date 2020/12/31 10:31
 */
public class LogManager {
    private static LogManager logManager;
    private String mappingOutPath;
    private String debugLogOutPath;

    private LogManager(ConfuseExtension confuseExtension) {
        mappingOutPath = confuseExtension.getConfuseMappingOutPath();
        if (mappingOutPath == null || mappingOutPath.length() == 0)
            mappingOutPath = MainPlugin.baseConf.getWriteConfuseMappingFile().getAbsolutePath();

        debugLogOutPath = confuseExtension.getDebugLogOutPath();
        if (debugLogOutPath == null || debugLogOutPath.length() == 0) {
            debugLogOutPath = MainPlugin.baseConf.getWriterLogFile().getAbsolutePath();
        }
    }

    public static boolean ready() {
        return logManager != null;
    }

    public static void initLogMappingManager(ConfuseExtension confuseExtension) {
        if (logManager == null) {
            logManager = new LogManager(confuseExtension);
        } else {
            LogTool.w("LogMappingManager.initLogMappingManager(String,com.zlrab.plugin.extensionConfuseExtension)Only allow to call once, do not call repeatedly");
        }
    }

    public static LogManager getInstance() {
        if (logManager == null)
            throw new RuntimeException("Please call initLogMappingManager() first to initialize this class");
        return logManager;
    }

    public LogManager mappingWrite(String line) {
        checkPath(mappingOutPath);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(mappingOutPath), true);
            fileWriter.write(line);
            fileWriter.write("\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
        return this;
    }

    public LogManager mappingWrite(StringBuilder stringBuilder) {
        return mappingWrite(stringBuilder.toString());
    }

    public String buildMappingLine(String type, String oldLine, String newLine) {
        return "[ " + type + " ]\told = " + oldLine + "\tnew = " + newLine;
    }

    public LogManager logWrite(String line) {
        checkPath(debugLogOutPath);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(debugLogOutPath), true);
            fileWriter.write(line);
            fileWriter.write("\r\n");
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
        return this;
    }

    private void checkPath(String path) {
        File debugLogFile = new File(path);
        if (!debugLogFile.exists()) {
            boolean mkdirs = debugLogFile.getParentFile().mkdirs();
        }
    }

    public LogManager logWrite(StringBuilder stringBuilder) {
        return logWrite(stringBuilder.toString());
    }
}
