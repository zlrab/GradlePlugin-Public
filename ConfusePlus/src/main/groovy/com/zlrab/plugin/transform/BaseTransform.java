package com.zlrab.plugin.transform;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.zlrab.tool.Enhance;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * @author zlrab
 * @date 2020/12/28 14:34
 */
@Deprecated
public abstract class BaseTransform extends Transform {
    @Override
    public void transform(TransformInvocation transformInvocation) {
        Enhance.forEach(transformInvocation.getInputs(), transformInput -> {
            Enhance.forEach(transformInput.getDirectoryInputs(), directoryInput -> {
                try {
                    File input = directoryInput.getFile();
                    File output = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                    transformAll(transformInvocation, transformInput, directoryInput, input);
                    FileUtils.copyDirectory(input, output);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("An exception occurred when copying folders");
                }
            });
            Enhance.forEach(transformInput.getJarInputs(), jarInput -> {

                String jarFileName = jarInput.getName();
                String md5Hex = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (jarFileName.endsWith(".jar")) {
                    jarFileName = jarFileName.substring(0, jarFileName.length() - 4);
                }
                File dest = transformInvocation.getOutputProvider().getContentLocation(jarFileName + md5Hex, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                try {
                    FileUtils.copyFile(jarInput.getFile(), dest);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("An exception occurs when copying the jar file");
                }
            });
        });
    }

    public void transformAll(TransformInvocation transformInvocation, TransformInput transformInput, DirectoryInput directoryInput, File input) {
        transformAll(transformInput, directoryInput, input);
    }

    public void transformAll(TransformInput transformInput, DirectoryInput directoryInput, File input) {
        transformAll(directoryInput, input);
    }

    public void transformAll(DirectoryInput directoryInput, File input) {
        transformAll(input);
    }

    public void transformAll(File input) {

    }
}
