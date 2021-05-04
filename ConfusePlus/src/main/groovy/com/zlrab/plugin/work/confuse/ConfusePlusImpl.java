package com.zlrab.plugin.work.confuse;


import com.zlrab.plugin.Const;
import com.zlrab.plugin.MainPlugin;
import com.zlrab.plugin.asm.zclass.ConfusePlusClassVisitor;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.impl.ZClassImpl;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zlrab
 * @date 2020/12/28 15:23
 */
@Deprecated
public class ConfusePlusImpl {
    /**
     * @key 原始的类签名 com/zlrab/Demo
     * @value 新的类签名 com/android/Proxy
     */
    private Map<String, String> activitySignModifyMap = new HashMap<>();
    private Map<String, String> receiverSignModifyMap = new HashMap<>();
    private Map<String, String> providerSignModifyMap = new HashMap<>();
    private Map<String, String> serviceSignModifyMap = new HashMap<>();

    private Map<String, String> customViewSignModifyMap = new HashMap<>();

    private Map<String, String> allSignModifyMap = new HashMap<>();

    private ConfuseExtension confuseExtension;

    private SAXReader saxReader = new SAXReader();

    private Document androidManifestDocument;

    private List<ZClass> zClassList = new ArrayList<>();

    private FileOutputStream fileOutputStream;

    public ConfusePlusImpl(ConfuseExtension confuseExtension) {
        this.confuseExtension = confuseExtension;
    }

    private void initMappingOut() {
        String confuseMappingOutPath = this.confuseExtension.getConfuseMappingOutPath();
        if (confuseMappingOutPath == null || confuseMappingOutPath.length() == 0)
            confuseMappingOutPath = MainPlugin.baseConf.getWriteConfuseMappingFile().getAbsolutePath();
        File mappingFile = new File(confuseMappingOutPath);
        if (mappingFile.exists()) {
            boolean delete = mappingFile.delete();
        } else {
            boolean mkdirs = mappingFile.getParentFile().mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(mappingFile, true);
            LogTool.e("Initialize the Mapping log output stream");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogTool.e("There is an exception in the initial Mappings output stream, please check, confuseMappingOutPath = " + confuseMappingOutPath);
        }
    }

    public void print() {
        Enhance.forEach(allSignModifyMap, entry -> LogTool.e("printModify : old = " + entry.getKey() + "\tnew = " + entry.getValue()));
    }

    public ConfusePlusImpl processManifest(File manifestFile) throws DocumentException {
        if (!confuseExtension.isConfuseComponent()) {
            LogTool.w("ConfusePlusComponent disabled");
            return this;
        }
        if (manifestFile == null || !manifestFile.exists()) return this;
        LogTool.w("start processManifest , file = " + manifestFile.getAbsolutePath());
        List<String> componentRules = confuseExtension.getConf().getMatchOperationRules().getComponent();
        if (componentRules == null || componentRules.size() == 0) {
            LogTool.w("Component confusion matching rule is empty, all components will not make any changes");
            return this;
        }
        List<String> componentWhiteList = confuseExtension.getConf().getWhitelist().getComponent();

        List<String> confusionDictionList = confuseExtension.getConf().getPackageClassDictionary();

        androidManifestDocument = saxReader.read(manifestFile);
        Element applicationElement = androidManifestDocument.getRootElement().element("application");
        applicationElement.elementIterator().forEachRemaining(element -> {
            String elementName = element.getName();
            Attribute nameAttr = element.attribute("name");
            switch (elementName) {
                case "activity":
                    processConfusionAttr(activitySignModifyMap, componentRules, componentWhiteList, confusionDictionList, nameAttr);
                    break;
                case "service":
                    processConfusionAttr(serviceSignModifyMap, componentRules, componentWhiteList, confusionDictionList, nameAttr);
                    break;
                case "receiver":
                    processConfusionAttr(receiverSignModifyMap, componentRules, componentWhiteList, confusionDictionList, nameAttr);
                    break;
                case "provider":
                    processConfusionAttr(providerSignModifyMap, componentRules, componentWhiteList, confusionDictionList, nameAttr);
                    break;
            }
        });
        writeInDocument(manifestFile, androidManifestDocument);
        if (fileOutputStream == null) initMappingOut();

        Enhance.forEach(activitySignModifyMap, entry -> writeLineToMappingsIO(buildWriteMappingLine("activityMapping", entry.getKey(), entry.getValue())));
        Enhance.forEach(serviceSignModifyMap, entry -> writeLineToMappingsIO(buildWriteMappingLine("serviceMapping", entry.getKey(), entry.getValue())));
        Enhance.forEach(receiverSignModifyMap, entry -> writeLineToMappingsIO(buildWriteMappingLine("receiverMapping", entry.getKey(), entry.getValue())));
        Enhance.forEach(providerSignModifyMap, entry -> writeLineToMappingsIO(buildWriteMappingLine("providerMapping", entry.getKey(), entry.getValue())));
        Enhance.forEach(customViewSignModifyMap, entry -> writeLineToMappingsIO(buildWriteMappingLine("customViewMapping", entry.getKey(), entry.getValue())));
        return this;
    }

    public ConfusePlusImpl processCustomView() {
        return this;
    }

    public File processClass(File classFile) throws IOException {
        if (classFile == null || !classFile.canRead())
            throw new RuntimeException("classFile does not exist or has no read permission, please check! classFile =" + classFile);
        LogTool.e("processClass = " + classFile.getAbsolutePath());
        ZClass zClass = new ZClassImpl(classFile);
        zClassList.add(zClass);

        FileInputStream fileInputStream = new FileInputStream(zClass.getFile());
        ClassReader classReader = new ClassReader(fileInputStream);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ConfusePlusClassVisitor(Const.ASM_API, classWriter, zClass, allSignModifyMap, confuseExtension.isRemoveSource());
        classReader.accept(classVisitor, Const.ASM_API);
        FileUtils.writeByteArrayToFile(classFile, classWriter.toByteArray());
        fileInputStream.close();
        //这个class文件没有在processManifest&processCustomView期间被重命名 直接返回
        if (!allSignModifyMap.containsKey(classReader.getClassName())) {
            return classFile;
        }

        String oldSign = classReader.getClassName().replace("/", File.separator) + ".class";
        String newSign = allSignModifyMap.get(classReader.getClassName()).replace("/", File.separator) + ".class";
        File newClassFile = new File(classFile.getAbsolutePath().replace(oldSign, newSign));
        if (newClassFile.exists()) throw new RuntimeException("The new class file already exists, the migration failed, it may be a dictionary conflict");
        newClassFile.getParentFile().mkdirs();
        FileUtils.copyFile(classFile, newClassFile);
        boolean delete = classFile.delete();
        if (!delete) {
            LogTool.e("Failed to delete original file , path = " + classFile.getAbsolutePath());
        }
        writeLineToMappingsIO(buildWriteMappingLine("moveClassFileMapping", classFile.getAbsolutePath(), newClassFile.getAbsolutePath()));
        return newClassFile;
    }

    private void writeLineToMappingsIO(String line) {
        try {
            fileOutputStream.write(line.getBytes());
            fileOutputStream.write("\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildWriteMappingLine(String type, String oldLine, String newLine) {
        return "[ " + type + " ]\toldName = " + oldLine + "\tnewName = " + newLine;
    }

    private void writeInDocument(File inFile, Document document) {
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

    public void end() {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                fileOutputStream = null;
            }
        }
        LogTool.e("Turn off mapping log output");
    }

    private String getPackageName() {
        return androidManifestDocument.getRootElement().attributeValue("package");
    }

    /**
     * 修改组件的名字
     *
     * @param componentSignModifyMap
     * @param rulesList
     * @param whitelist
     * @param confusionDictionList
     * @param nameAttr
     */
    private void processConfusionAttr(Map<String, String> componentSignModifyMap, List<String> rulesList, List<String> whitelist, List<String> confusionDictionList, Attribute nameAttr) {
        String content = nameAttr.getValue();
        boolean matchOperation = false;
        for (String pattern : rulesList) {
            boolean result = wildcardStarMatch(pattern, content);
            if (result) {
                matchOperation = true;
                break;
            }
        }
        if (!matchOperation) {
            LogTool.w("Component: ["+ content +"] is filtered because it does not match the matching rules");
            return;
        }
        boolean beFiltered = false;
        for (String white : whitelist) {
            boolean result = wildcardStarMatch(white, content);
            if (result) {
                beFiltered = true;
                break;
            }
        }
        if (beFiltered) {
            LogTool.w("Component: [ " + content + " ] Filtered due to whitelist rules");
            return;//被白名单过滤
        }
        if (content.startsWith(".")) {
            nameAttr.setValue(getPackageName() + content);
        }
        String newContent = buildComponentName(confusionDictionList);
        nameAttr.setValue(newContent);
        String oldSign = content.replace('.', '/');
        String newSign = newContent.replace('.', '/');
        componentSignModifyMap.put(oldSign, newSign);
        allSignModifyMap.put(oldSign, newSign);
    }

    /**
     * 构建新的组件名，当组件名已存在时，将递归，直至获得新的组件名
     *
     * @param confusionDictionList
     * @return
     */
    private String buildComponentName(List<String> confusionDictionList) {
        int count = RandomTool.randomRangeNumber(4, 6);
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            stringBuilder.append(confusionDictionList.get(RandomTool.randomRangeNumber(0, confusionDictionList.size()))).append('.');
        }
        String componentName = stringBuilder.toString();
        componentName = componentName.substring(0, componentName.length() - 1);
        if (allSignModifyMap.containsValue(componentName.replace('.', '/'))) {
            return buildComponentName(confusionDictionList);
        } else {
            return componentName;
        }
    }

    /**
     * 根据指定的规则匹配字符串
     *
     * @param pattern
     * @param content
     * @return
     */
    private boolean wildcardStarMatch(String pattern, String content) {
        int strLength = content.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0, patternLength = pattern.length(); patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                while (strIndex < strLength) {
                    if (wildcardStarMatch(pattern.substring(patternIndex + 1), content.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else {
                if ((strIndex >= strLength) || (ch != content.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }
}
