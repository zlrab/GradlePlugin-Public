package com.zlrab.tool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.gradle.api.Action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author zlrab
 * @date 2020/12/24 10:36
 */
public class FileTool {
    public static final String encoding = "GBK";

    /**
     * 压碎文件
     * 将在源文件同级目录下生成压缩文件，命名规则为：源文件名字.zip
     *
     * @param srcFile 源文件
     * @return 压缩结果
     */
    public static void compression(File srcFile) throws IOException {
        compression(srcFile, new File(srcFile.getParentFile(), FilenameUtils.getBaseName(srcFile.getName()) + ".zip"));
    }

    /**
     * 压缩文件
     * 在destFile存在的情况时，新生成的压缩文件将覆盖旧的destFile
     *
     * @param srcFile  源文件
     * @param destFile 输出文件
     * @return 压缩结果
     */
    public static void compression(File srcFile, File destFile) throws IOException {
        compression(srcFile, destFile, true);
    }

    /**
     * 压缩文件
     *
     * @param srcFile     源文件
     * @param destFile    输出文件
     * @param forceRemove 当destFile存在的情况下，是否强制删除
     * @return 压缩结果
     */
    public static void compression(File srcFile, File destFile, boolean forceRemove) throws IOException {
        compression(srcFile, destFile, forceRemove, encoding);
    }

    /**
     * 压缩文件
     *
     * @param srcFile     源文件
     * @param destFile    输出文件
     * @param forceRemove 当destFile存在的情况下，是否强制删除
     * @param encoding    编码格式
     * @return 压缩结果
     */
    public static void compression(File srcFile, File destFile, boolean forceRemove, String encoding) throws IOException {
        if (srcFile == null || !srcFile.canRead())
            throw new FileNotFoundException("源文件不存在或没有读取权限 , srcFile = " + srcFile);
        if (destFile == null)
            throw new FileNotFoundException("destFile为null , 请指定具体的输出目录 , srcFile = " + srcFile);
        if (destFile.exists()) {
            if (forceRemove) {
                if (destFile.isDirectory()) {
                    FileUtils.deleteDirectory(destFile);
                } else {
                    boolean delete = destFile.delete();
                }
            } else {
                throw new RuntimeException("destFile已存在，可以指定forceRemove为true执行删除操作 srcFile = " + srcFile + "\tdestFile = " + destFile);
            }
        } else {
            FileUtils.forceMkdir(destFile.getParentFile());
        }
        Project project = new Project();
        FileSet fileSet = new FileSet();
        fileSet.setProject(project);
        if (srcFile.isDirectory()) {
            fileSet.setDir(srcFile);
        } else {
            fileSet.setFile(srcFile);
        }
        Zip zip = new Zip();
        zip.setProject(project);
        zip.setDestFile(destFile);
        zip.addFileset(fileSet);
        zip.setEncoding(encoding);
        zip.execute();
    }

    /**
     * 解压zip
     *
     * @param srcZip zip源文件
     * @throws IOException 文件操作异常
     */
    public static void unZip(File srcZip) throws IOException {
        unZip(srcZip, new File(srcZip.getParentFile(), FilenameUtils.getBaseName(srcZip.getName())));
    }

    /**
     * 解压zip
     *
     * @param srcZip  zip源文件
     * @param outFile 输出目录
     * @throws IOException 文件操作异常
     */
    public static void unZip(File srcZip, File outFile) throws IOException {
        unZip(srcZip, outFile, true);
    }

    /**
     * 解压zip
     *
     * @param srcZip      zip源文件
     * @param outFile     输出目录
     * @param forceRemove 在输出目录存在情况下，是否强制删除
     * @throws IOException 文件操作异常
     */
    public static void unZip(File srcZip, File outFile, boolean forceRemove) throws IOException {
        unZip(srcZip, outFile, forceRemove, encoding);
    }

    /**
     * 解压zip
     *
     * @param srcZip      zip源文件
     * @param outFile     输出目录
     * @param forceRemove 在输出目录存在情况下，是否强制删除
     * @param encoding    编码格式
     * @throws IOException 文件操作异常
     */
    public static void unZip(File srcZip, File outFile, boolean forceRemove, String encoding) throws IOException {
        if (srcZip == null || !srcZip.canRead())
            throw new FileNotFoundException("源文件不存在或没有读取权限 , srcZip = " + srcZip);
        if (outFile == null)
            throw new FileNotFoundException("outFile为null , 请指定具体的输出目录 , srcZip = " + srcZip);
        if (outFile.exists()) {
            if (forceRemove) {
                if (outFile.isDirectory()) {
                    FileUtils.deleteDirectory(outFile);
                } else {
                    boolean delete = outFile.delete();
                }
            } else {
                throw new RuntimeException("destFile已存在，可以指定forceRemove为true执行删除操作 srcZip = " + srcZip + "\toutFile = " + outFile);
            }
        } else {
            FileUtils.forceMkdir(outFile.getParentFile());
        }
        Project project = new Project();
        Expand expand = new Expand();
        expand.setProject(project);
        expand.setTaskType("unzip");
        expand.setTaskName("unzip");
        expand.setEncoding(encoding);
        expand.setSrc(srcZip);
        expand.setDest(outFile);
        expand.execute();
    }

    public static void traversingFile(File file, Action<File> action) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (File value : files) {
                    traversingFile(value, action);
                }
            }
        } else {
            action.execute(file);
        }
    }

    public static void writeInDocument(File inFile, Document document) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(inFile);
            XMLWriter xmlWriter = new XMLWriter(fileOutputStream);
            xmlWriter.write(document);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    public static void writeInStrings(File inFile, String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inFile))) {
            bw.write(data);
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }

    /**
     * @param file
     * @param level {@link FileTool#NOT_EMPTY}
     *              {@link FileTool#NOT_EMPTY_AND_EXISTS}
     *              {@link FileTool#NOT_EMPTY_AND_EXISTS_AND_CAN_READ}
     *              {@link FileTool#NOT_EMPTY_AND_EXISTS_AND_CAN_WRITE}
     *              {@link FileTool#NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN}
     * @return
     */
    public static boolean checkAccessPermission(File file, int level) {
        switch (level) {
            case NOT_EMPTY:
                return file != null;
            case NOT_EMPTY_AND_EXISTS:
                return file != null && file.exists();
            case NOT_EMPTY_AND_EXISTS_AND_CAN_READ:
                return file != null && file.exists() && file.canRead();
            case NOT_EMPTY_AND_EXISTS_AND_CAN_WRITE:
                return file != null && file.exists() && file.canWrite();
            case NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN:
                return file != null && file.exists() && file.canRead() && file.canWrite();
        }
        return false;
    }

    public static final int NOT_EMPTY = -1;
    public static final int NOT_EMPTY_AND_EXISTS = 0;
    public static final int NOT_EMPTY_AND_EXISTS_AND_CAN_READ = 1;
    public static final int NOT_EMPTY_AND_EXISTS_AND_CAN_WRITE = 2;
    public static final int NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN = 3;

}
