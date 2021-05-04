package com.zlrab.plugin.transform

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.variant.LibraryVariantData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.zlrab.plugin.Const
import com.zlrab.plugin.Reflect
import com.zlrab.plugin.work.EncryptionStringClassManager
import com.zlrab.plugin.work.confuse.ConfusePlusManager
import com.zlrab.plugin.work.confuse.ResConfusePlusManager
import com.zlrab.tool.Enhance
import com.zlrab.tool.FileTool
import com.zlrab.tool.LogTool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project;


/**
 * @author zlrab* @date 2021/1/4 19:01
 */
class ConfuseTransform extends Transform {
    private Project project
    private TestedExtension android

    ConfuseTransform(Project project, TestedExtension android) {
        this.project = project
        this.android = android

        processManifest()
    }

    private void processManifest() {
        android.libraryVariants.all { variantsLibraryVariantImpl ->
            BaseVariantOutput baseVariantOutput = variantsLibraryVariantImpl.outputs[0]
            baseVariantOutput.processManifest.doLast {
                def libraryVariantData = (LibraryVariantData) Reflect.on(variantsLibraryVariantImpl).call("getVariantData").get()
                def bundleDir = libraryVariantData.scope.baseBundleDir
                def manifestFile = new File(bundleDir, "AndroidManifest.xml")
                if (manifestFile.exists()) {
                    ConfusePlusManager.getInstance().scanManifest(manifestFile.absolutePath)
                } else {
                    LogTool.w("manifestOutputFile does not exist, it may be misleading task")
                }
            }
        }
    }

    @Override
    String getName() {
        return ConfuseTransform.class.name
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_LIBRARY
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        android.libraryVariants.all { variantsLibraryVariantImpl ->
            def libraryVariantData = (LibraryVariantData) Reflect.on(variantsLibraryVariantImpl).call("getVariantData").get()
            def bundleDir = libraryVariantData.scope.baseBundleDir
            ResConfusePlusManager.getInstance().parsing(bundleDir)
        }
        LogTool.w("----------------------------------run ConfuseTransform---------------------------------------")
        Enhance.forEach(transformInvocation.getInputs(), { transformInput ->
            Enhance.forEach(transformInput.getDirectoryInputs(), { directoryInput ->
                try {
                    File input = directoryInput.getFile();
                    File output = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                    List<File> workFileList = new ArrayList<>()
                    FileTool.traversingFile(input, { file -> workFileList.add(file) })
                    LogTool.d("--------------start parsingClass---------------")
                    workFileList.each { file ->
                        ConfusePlusManager.getInstance().parsingClass(file)
                    }
                    LogTool.d("--------------end   parsingClass---------------")
                    EncryptionStringClassManager.dyLoadEncryptionPluginJar(ConfusePlusManager.getInstance().confuseExtension);
                    LogTool.d("--------------start processClass---------------")
                    workFileList.each { file ->
                        File newPathFIle = ConfusePlusManager.getInstance().confuseClass(file)
                        workFileList.set(workFileList.indexOf(file), newPathFIle)
                    }
                    LogTool.d("--------------end   processClass---------------")
                    FileUtils.copyDirectory(input, output)
                } catch (IOException e) {
                    e.printStackTrace()
                    throw new RuntimeException("An exception occurred when copying folders")
                }
            })
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
                    e.printStackTrace()
                    throw new RuntimeException("An exception occurs when copying the jar file")
                }
            })
        })
        LogTool.w("----------------------------------end ConfuseTransform---------------------------------------")
    }
}
