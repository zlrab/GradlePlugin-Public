package com.zlrab.plugin.work;

import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.zclass.EncryptedStringClassVisitor;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.impl.ZClassImpl;
import com.zlrab.tool.LogTool;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/25 19:18
 */
@Deprecated
public class EncryptedStringImpl {

    private List<ZClass> zClassList = new ArrayList<>();

    public EncryptedStringImpl(ConfuseExtension confuseExtension) {
        EncryptionStringClassManager.dyLoadEncryptionPluginJar(confuseExtension);
    }

    public void processClass(File classFile) throws IOException {
        processClass(classFile, classFile);
    }

    public void processClass(File classFile, File outFile) throws IOException {
        if (classFile == null || !classFile.canRead()) {
            LogTool.w("classFile does not exist or has no read permission, please check! classFile = " + classFile);
            return;
        }
        if (outFile == null) {
            LogTool.w("outFile does not exist, please check! outFile = null");
            return;
        }
        if (outFile.exists()) {
            if (!outFile.canWrite()) {
                LogTool.w("outFile does not have write permission, please check! outFile = " + outFile);
                return;
            }
        } else {
            FileUtils.forceMkdir(outFile.getParentFile());
        }
        ZClass zClass = new ZClassImpl(classFile);
        zClassList.add(zClass);
        ClassReader classReader = new ClassReader(new FileInputStream(zClass.getFile()));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new EncryptedStringClassVisitor(Const.ASM_API, classWriter, zClass);
        classReader.accept(classVisitor, Const.ASM_API);
        FileUtils.writeByteArrayToFile(outFile, classWriter.toByteArray());
    }
}
