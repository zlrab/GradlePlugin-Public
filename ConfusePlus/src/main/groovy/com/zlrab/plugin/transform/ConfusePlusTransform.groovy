package com.zlrab.plugin.transform

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.variant.LibraryVariantData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.zlrab.plugin.Reflect
import com.zlrab.plugin.extension.ConfuseExtension
import com.zlrab.plugin.work.confuse.ConfusePlusImpl
import com.zlrab.tool.Enhance
import com.zlrab.tool.FileTool
import com.zlrab.tool.LogTool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import com.android.build.gradle.internal.pipeline.TransformManager;

/**
 * @author zlrab* @date 2020/12/29 19:12
 */
@Deprecated
class ConfusePlusTransform extends Transform {
    private Project project
    private TestedExtension android
    private ConfusePlusImpl confusePlus

    ConfusePlusTransform(Project project, TestedExtension android) {
        this.project = project;
        this.android = android;

        processManifest();
    }

    private void processManifest() {
        android.libraryVariants.all { variantsLibraryVariantImpl ->
            if (confusePlus == null) {
                ConfuseExtension confuseExtension = project.extensions.findByName(ConfuseExtension.CONF_CONFUSE_NAME)
                confusePlus = new ConfusePlusImpl(confuseExtension)
            }

            BaseVariantOutput baseVariantOutput = variantsLibraryVariantImpl.outputs[0]
            ManifestProcessorTask manifestProcessorTask = baseVariantOutput.processManifest
            manifestProcessorTask.doLast {
                def libraryVariantData = (LibraryVariantData) Reflect.on(variantsLibraryVariantImpl).call("getVariantData").get()
                def bundleDir = libraryVariantData.scope.baseBundleDir
                def manifestFile = new File(bundleDir, "AndroidManifest.xml")
                if (manifestFile.exists()) {
                    confusePlus.processManifest(manifestFile)
                } else {
                    LogTool.w("manifestOutputFile does not exist, it may be misleading task")
                }
            }
        }
    }

    @Override
    String getName() {
        return ConfusePlusTransform.class.getName();
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
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (confusePlus == null) {
            throw new RuntimeException("confusePlus is empty", new Throwable())
        }
        confusePlus.print()
        LogTool.w("----------------------------------run ConfusePlusTransform---------------------------------------")
        Enhance.forEach(transformInvocation.getInputs(), { transformInput ->
            Enhance.forEach(transformInput.getDirectoryInputs(), { directoryInput ->
                try {
                    File input = directoryInput.getFile();
                    File output = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                    List<File> workFileList = new ArrayList<>()
                    FileTool.traversingFile(input, { file -> workFileList.add(file) })
                    workFileList.each { file ->
                        confusePlus.processClass(file)
                    }
                    FileUtils.copyDirectory(input, output);
                } catch (IOException e) {
                    e.printStackTrace()
                    throw new RuntimeException("An exception occurred when copying folders");
                }
            });
            Enhance.forEach(transformInput.getJarInputs(), { jarInput ->

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
        })
        if (confusePlus != null) confusePlus.end()
        LogTool.w("----------------------------------end ConfusePlusTransform---------------------------------------")

    }
}
