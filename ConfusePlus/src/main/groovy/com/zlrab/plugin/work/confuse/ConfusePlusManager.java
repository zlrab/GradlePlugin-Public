package com.zlrab.plugin.work.confuse;

import com.zlrab.core.UnstableApi;
import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.asm.zclass.ConfusePlusVisitor;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.impl.ZClassImpl;
import com.zlrab.plugin.work.LogManager;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.FastPrintWriter;
import com.zlrab.tool.FileTool;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zlrab
 * @date 2020/12/30 21:02
 */
public class ConfusePlusManager {
    private static ConfusePlusManager confusePlusManager;
    private ConfuseExtension confuseExtension;
    /**
     * 开始执行asm aop修改class的标记
     * 当这个flag为true时，后续通过{@link ConfusePlusManager#addConfuseTask(ConfuseModifyClassRecord)}添加的混淆任务都不会被执行
     */
    private boolean startConfuseClassFlag;
    /**
     * key: com/zlrab/demo
     */
    private Map<String, ConfuseModifyClassRecord> confuseClassModifyMap = new HashMap<>();

    private SAXReader saxReader = new SAXReader();
    /**
     * 每次生成的class sign都会储存在这里面，在存入的是否需判断是否已存在，用于防止重复
     * example : com/zlrab/demo
     */
    private List<String> newSignList = new ArrayList<>();

    private List<ZClass> zClassList = new ArrayList<>();

    private ConfusePlusManager(ConfuseExtension confuseExtension) {
        this.confuseExtension = confuseExtension;
    }

    /**
     * 初始化混淆帮助类
     *
     * @param confuseExtension
     */
    public static void initConfusePlusManager(ConfuseExtension confuseExtension) {
        if (confusePlusManager == null) {
            confusePlusManager = new ConfusePlusManager(confuseExtension);
        } else {
            LogTool.w("ConfusePlusManager.initConfusePlusManager(String,com.zlrab.plugin.extension.ConfuseExtension) Only allow to call once, do not call repeatedly");
        }
    }

    /**
     * 获取混淆帮助类单例对象
     *
     * @return
     */
    public static ConfusePlusManager getInstance() {
        if (confusePlusManager == null)
            throw new RuntimeException("Please call initConfusePlusManager() first to initialize this class");
        return confusePlusManager;
    }

    /**
     * 添加需要混淆的类 必须在{@link ConfusePlusManager#confuseClass(java.io.File)}之前
     *
     * @param confuseModifyClassRecord
     * @return
     */
    public ConfusePlusManager addConfuseTask(ConfuseModifyClassRecord confuseModifyClassRecord) {
        if (startConfuseClassFlag) {
            LogTool.w("Refuse to add obfuscation task because the operation of obfuscating class has already started," +
                    " please add the task before calling ConfusePlusManager.confuseClass(java.lang.String)");
            return this;
        }
        /**
         * TODO
         * {@link com.zlrab.plugin.extension.ConfuseExtension.Conf.ConfuseClassRulesEntity}中如果配置了四大组件类名，将会出现覆盖问题
         */
        if (confuseClassModifyMap.containsKey(confuseModifyClassRecord.getOldSign())) {
            LogTool.w("[ " + confuseModifyClassRecord.getOldSign() + " ] Already exists, please do not add obfuscation tasks repeatedly");
            return this;
        }
        LogTool.e("addConfuseTask = oldSign = " + confuseModifyClassRecord.getOldSign() + "\tnewSign = " + confuseModifyClassRecord.getNewSign());
        confuseClassModifyMap.put(confuseModifyClassRecord.getOldSign(), confuseModifyClassRecord);
        return this;
    }

    /**
     * 扫描manifest文件中的四大组件
     * 根据匹配规则{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getComponent()}和白名单{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getWhitelist()}确定需改组件名
     *
     * @param manifestPath manifest文件路径
     * @return
     */
    public ConfusePlusManager scanManifest(String manifestPath) {
        return scanManifest(manifestPath, false);
    }

    /**
     * 扫描manifest文件中的四大组件
     * 根据匹配规则{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getComponent()}和白名单{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getWhitelist()}确定需改组件名
     *
     * @param manifestPath         manifest文件路径
     * @param alwaysModifyManifest 在混淆操作已开始的情况下，是否继续混淆manifest(混淆class的任务任然不会被执行)
     * @return
     */
    public ConfusePlusManager scanManifest(String manifestPath, boolean alwaysModifyManifest) {
        if (!confuseExtension.isConfuseComponent()) {
            LogTool.w("ConfusePlusComponent disabled , Open method : confuseConf { confuseComponent = true }");
            return this;
        }
        return scanManifest(new File(manifestPath), alwaysModifyManifest);
    }

    /**
     * 扫描manifest文件中的四大组件
     * 根据匹配规则{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getComponent()}和白名单{@link ConfuseExtension.Conf.MatchOperationRulesEntity#getWhitelist()}确定需改组件名
     *
     * @param manifestFile
     * @param alwaysModifyManifest 在混淆操作已开始的情况下，是否继续混淆manifest(混淆class的任务任然不会被执行)
     * @return
     */
    public ConfusePlusManager scanManifest(File manifestFile, boolean alwaysModifyManifest) {
        if (!confuseExtension.isConfuseComponent()) {
            LogTool.w("ConfusePlusComponent disabled , Open method : confuseConf { confuseComponent = true }");
            return this;
        }
        if (startConfuseClassFlag) {//如果任务已开启
            if (!alwaysModifyManifest) {
                LogTool.w("the obfuscation operation has started, and you set alwaysModifyManifest to false, " +
                        "the manifest will not be changed, and the class associated with the component declared in the manifest will not change.");
                return this;
            } else {
                LogTool.w("The obfuscation operation has started. Because you set alwaysModifyManifest to true, " +
                        "the component declaration in the manifest will change, but the class associated with the declared component will not change.");
            }
        }
        if (manifestFile == null || !manifestFile.exists() || !manifestFile.canRead() || !manifestFile.canWrite()) {
            LogTool.w("The manifest file cannot be scanned because the input manifest file does not exist or cannot be read or written, path = [ " + manifestFile + " ]");
            return this;
        }
        LogTool.d("start scan manifest , path [ " + manifestFile.getAbsolutePath() + " ]");
        List<String> componentRules = confuseExtension.getConf().getMatchOperationRules().getComponent();
        if (componentRules == null || componentRules.size() == 0) {
            LogTool.w("Component confusion matching rule is empty, all components will not make any changes," +
                    "Please configure component confusion matching rules in ConfuseConf.json");
            return this;
        }

        try {
            Document manifestDocument = saxReader.read(manifestFile);
            Element rootElement = manifestDocument.getRootElement();
            String packageName = rootElement.attributeValue("package");
            rootElement.element("application").elementIterator().forEachRemaining(componentElement -> processComponentElement(componentElement, packageName));
            FileTool.writeInDocument(manifestFile, manifestDocument);
            StringBuilder stringBuilder = new StringBuilder();
            Enhance.forEach(confuseClassModifyMap, entry -> {
                ConfuseModifyClassRecord confuseModifyClassRecord = entry.getValue();
                stringBuilder.append(LogManager.getInstance().buildMappingLine(
                        confuseModifyClassRecord.getExtra(),
                        confuseModifyClassRecord.getOldSign(),
                        confuseModifyClassRecord.getNewSign()
                ));
                stringBuilder.append("\r\n");
            });
            LogManager.getInstance().mappingWrite(stringBuilder.toString());
        } catch (DocumentException e) {
            LogTool.e("An error occurred while parsing the manifest , path [ " + manifestFile.getAbsolutePath() + " ]", e);
        }
        return this;
    }

    /**
     * 处理组件节点
     *
     * @param componentElement
     * @param packageName
     */
    private void processComponentElement(Element componentElement, String packageName) {
        List<String> componentRules = confuseExtension.getConf().getMatchOperationRules().getComponent();
        List<String> componentWhiteList = confuseExtension.getConf().getWhitelist().getComponent();
        Attribute nameAttr = componentElement.attribute("name");
        if (nameAttr == null) return;
        String nameValue = nameAttr.getValue();
        boolean matchOperation = false;
        for (String pattern : componentRules) {
            boolean result = Enhance.wildcardStarMatch(pattern, nameValue);
            if (result) {
                matchOperation = true;
                break;
            }
        }
        if (!matchOperation) {
            LogTool.w("Component: [" + nameValue + "] is filtered because it does not match the matching rules");
            return;
        }
        boolean beFiltered = false;
        for (String white : componentWhiteList) {
            boolean result = Enhance.wildcardStarMatch(white, nameValue);
            if (result) {
                beFiltered = true;
                break;
            }
        }
        if (beFiltered) {
            LogTool.w("Component: [ " + nameValue + " ] Filtered due to whitelist rules");
            return;//被白名单过滤
        }
        if (nameValue.startsWith(".")) {
            nameAttr.setValue(packageName + nameValue);
        }
        String newNameValue = buildClassName();
        nameAttr.setValue(newNameValue);
        String oldSign = ConversionTool.classNameToSign(nameValue);
        String newSign = ConversionTool.classNameToSign(newNameValue);
        String elementName = componentElement.getName();
        ConfuseModifyClassRecord confuseModifyClassRecord = new ConfuseModifyClassRecord(oldSign, newSign, elementName + "ModifyMapping");
        addConfuseTask(confuseModifyClassRecord);
    }

    /**
     * 构建新的组件名，当组件名已存在时，将递归，直至获得新的组件名
     *
     * @return
     */
    private String buildClassName() {
        List<String> confusionDictionList = confuseExtension.getConf().getPackageClassDictionary();
        int count = RandomTool.randomRangeNumber(4, 6);
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            stringBuilder.append(confusionDictionList.get(RandomTool.randomRangeNumber(0, confusionDictionList.size()))).append('.');
        }
        String componentName = stringBuilder.toString();
        componentName = componentName.substring(0, componentName.length() - 1);

        if (newSignList.contains(componentName.replace('.', '/')) || componentName.startsWith("a")) {
            return buildClassName();
        } else {
            newSignList.add(componentName.replace('.', '/'));
            return componentName;
        }
    }

    public ConfusePlusManager parsingClass(File classFile) {
        if (startConfuseClassFlag) {
            LogTool.w("Because the operation of obfuscating the class has already started, it refuses to parse the class file." +
                    " Please parse before calling ConfusePlusManager.confuseClass (java.lang.String)");
            return this;
        }
        if (classFile == null || !classFile.canRead() || !classFile.canWrite()) {
            LogTool.w("classFile does not exist or has no read and write permissions, please check! classFile =" + classFile);
            return this;
        }
        LogTool.d("start parsingClass = " + classFile.getAbsolutePath());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(classFile);
            ClassReader classReader = new ClassReader(fileInputStream);
            String className = classReader.getClassName();
            ConfuseExtension.Conf.ConfuseClassRulesEntity confuseClassRules = confuseExtension.getConf().getConfuseClassRules();
            List<String> whiteFilter = Enhance.filter(confuseClassRules.getWhitelist(), s -> Enhance.wildcardStarMatch(s, className));
            if (whiteFilter.size() != 0) {
                LogTool.d("whiter filter , file = " + classFile.getAbsolutePath());
                return this;//匹配到白名单 不操作
            }
            List<ConfuseExtension.Conf.ConfuseClassRulesEntity.MatchEntity> workFilter = Enhance.filter(confuseClassRules.getMatch(), matchEntity -> Enhance.wildcardStarMatch(matchEntity.getClassNameRules(), className));
            if (workFilter.size() == 0) {
                LogTool.d("not work filter , className = " + className);
                return this;//不在操作名单内 不操作
            }
            String oldClassSign = ConversionTool.classNameToSign(className);
            String newClassSign = ConversionTool.classNameToSign(buildClassName());
            ConfuseModifyClassRecord confuseModifyClassRecord = new ConfuseModifyClassRecord(oldClassSign, newClassSign);
            if (workFilter.size() == 1) {//确定模式
                ConfuseExtension.Conf.ConfuseClassRulesEntity.MatchEntity matchEntity = workFilter.get(0);
                Enhance.forEach(matchEntity.getMethods(), methodsEntity -> {
                    ConfuseModifyMethodRecord confuseModifyMethodRecord = new ConfuseModifyMethodRecord(
                            methodsEntity.getAccess(),
                            methodsEntity.getName(),
                            methodsEntity.getDescriptor(),
                            methodsEntity.getAction()
                    );
                    confuseModifyClassRecord.addMethodRecord(confuseModifyMethodRecord);
                });
            } else {//模糊模式
                //TODO
            }
            addConfuseTask(confuseModifyClassRecord);
            parsingInnerClass(classFile, classReader, confuseModifyClassRecord);
        } catch (IOException e) {
            LogTool.e("parsingClass error , filePath = " + classFile.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return this;
    }

    private void parsingInnerClass(File outerFile, ClassReader classReader, ConfuseModifyClassRecord outerClassRecord) throws IOException {
        LogTool.e("parsingInnerClass1 outerClassRecord oldSign = " + outerClassRecord.getOldSign() + "\t newSign = " + outerClassRecord.getNewSign());
        //处理匿名内部类
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassVisitor(Const.ASM_API, classWriter) {
            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                LogTool.e("visitInnerClass  name = " + name + "\tclassReadName = " + classReader.getClassName() + "\touterName = " + outerName + "\tinnerName = " + innerName + "\taccess = " + access);
                if (!name.equals(classReader.getClassName()) && name.length() > classReader.getClassName().length() && (access == 1 || access == 0/*||access==8*/)) {
                    String newSign = name.replace(outerClassRecord.getOldSign(), outerClassRecord.getNewSign());
                    ConfuseModifyClassRecord confuseModifyClassRecord = new ConfuseModifyClassRecord(name, newSign);
                    ConfusePlusManager.getInstance().addConfuseTask(confuseModifyClassRecord);
                    File currentInnerFile = new File(outerFile.getAbsolutePath().replace(ConversionTool.signToFileSys(classReader.getClassName()), ConversionTool.signToFileSys(name)));
                    if (currentInnerFile.exists()) {
                        LogTool.e("outerFile = " + outerFile.getAbsolutePath());
                        LogTool.e("childInnerFile = " + currentInnerFile.getAbsolutePath());
                        try {
                            FileInputStream fileInputStream = new FileInputStream(currentInnerFile);
                            ClassReader innerReader = new ClassReader(fileInputStream);
//                            parsingInnerClass2(currentInnerFile, innerReader, confuseModifyClassRecord);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                super.visitInnerClass(name, outerName, innerName, access);
            }
        };
        classReader.accept(classVisitor, Const.ASM_API);
        FileUtils.writeByteArrayToFile(outerFile, classWriter.toByteArray());
    }

    public File confuseClass(File classFile) throws IOException {
//        startConfuseClassFlag = true;
        if (classFile == null || !classFile.canRead() || !classFile.canWrite()) {
            LogTool.w("classFile does not exist or has no read and write permissions, please check! classFile =" + classFile);
            return classFile;
        }
        LogTool.d("start confuseClass = " + classFile.getAbsolutePath());
        FileInputStream fileInputStream = new FileInputStream(classFile);
        ClassReader classReader = new ClassReader(fileInputStream);
        String classSign = classReader.getClassName();
        //判断是否删除
        List<String> matchList = confuseExtension.getConf().getRemoveClassRules().getMatch();
        if (matchList != null && matchList.size() > 0) {
            List<String> matchFilterResult = Enhance.filter(matchList, match -> Enhance.wildcardStarMatch(match, classSign));
            if (matchFilterResult.size() > 0) {
                List<String> whitelist = confuseExtension.getConf().getRemoveClassRules().getWhitelist();
                List<String> whiteFilterList = Enhance.filter(whitelist, white -> Enhance.wildcardStarMatch(white, classSign));
                if (whiteFilterList.size() == 0) {
                    fileInputStream.close();
                    boolean delete = classFile.delete();
                    LogTool.d("class [ " + classSign + " ] It matches the deletion rule and is not in the whitelist and is deleted , deleteResult = " + delete);
                    return classFile;
                }
            }
        }
        //Aop注入
        ZClass zClass = new ZClassImpl(classFile);
        zClassList.add(zClass);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ConfusePlusVisitor(Const.ASM_API, classWriter, zClass);
        classReader.accept(classVisitor, Const.ASM_API);
        FileUtils.writeByteArrayToFile(classFile, classWriter.toByteArray());
        fileInputStream.close();

        //迁移文件
        if (!confuseClassModifyMap.containsKey(classSign)) {//这个class因为不在混淆名单内，所以不需要迁移，直接返回
            return classFile;
        }

        String oldFileSys = ConversionTool.signToFileSys(classSign) + ".class";
        ConfuseModifyClassRecord confuseModifyClassRecord = confuseClassModifyMap.get(classSign);
        String newFileSys = ConversionTool.signToFileSys(confuseModifyClassRecord.getNewSign()) + ".class";
        LogTool.e("oldFileSys = " + oldFileSys + "\tnewFileSys = " + newFileSys);
        File newClassFile = new File(classFile.getAbsolutePath().replace(oldFileSys, newFileSys));
        if (newClassFile.exists())
            throw new RuntimeException("The new class file already exists, the migration failed, it may be a dictionary conflict , newClassFile = " + newClassFile.getAbsolutePath());
        newClassFile.getParentFile().mkdirs();
        FileUtils.copyFile(classFile, newClassFile);
        boolean delete = classFile.delete();
        if (!delete) {
            LogTool.e("Failed to delete original file , path = " + classFile.getAbsolutePath());
        }
        LogManager.getInstance().mappingWrite(LogManager.getInstance().buildMappingLine("moveClassFileMapping", classFile.getAbsolutePath(), newClassFile.getAbsolutePath()));
        return newClassFile;
    }

    @UnstableApi
    public ConfuseExtension getConfuseExtension() {
        return confuseExtension;
    }

    public ConfuseModifyClassRecord accordingToSignReadRecord(String sign) {
        return confuseClassModifyMap.get(sign);
    }

    public Collection<ConfuseModifyClassRecord> readAllRecord() {
        return confuseClassModifyMap.values();
    }

    /**
     * @param shortDesc Lcom/zlrab/Demo;
     * @return
     */
    public ConfuseModifyClassRecord accordingToShortDescReadRecord(String shortDesc) {
        boolean b = checkDescHasObjectType(shortDesc);
        if (!b) return null;
        return confuseClassModifyMap.get(ConversionTool.descriptorToSign(shortDesc));
    }

    /**
     * @param longDescriptor (Ljava/lang/String;L)Ljava/lang/String;
     * @return
     */
    public Collection<ConfuseModifyClassRecord> accordingToLongDescReadRecord(String longDescriptor) {
        boolean b = checkDescHasObjectType(longDescriptor);
        if (!b) return null;
        return Enhance.mapFilter(confuseClassModifyMap, (key, value) -> longDescriptor.contains(ConversionTool.signToDescriptor(key))).values();
    }

    private boolean checkDescHasObjectType(String desc) {
        return desc != null && desc.length() > 2 && desc.contains("L") && desc.contains(";");
    }

    public Collection<ConfuseModifyClassRecord> accordingToLongSignatureReadRecord(String signature) {
        return accordingToLongDescReadRecord(signature);
    }

    /**
     * 确认方法的操作模式（忽略方法访问权限）
     *
     * @param sign
     * @param name
     * @param descriptor
     * @return
     */
    public ConfuseModifyMethodRecord determineMethodOperationModeIgnoreAccess(String sign, String name, String descriptor) {
        ConfuseModifyClassRecord confuseModifyClassRecord = confuseClassModifyMap.get(sign);
        if (confuseModifyClassRecord == null) return null;
        List<ConfuseModifyMethodRecord> methodRecordList = confuseModifyClassRecord.getMethodRecordList();
        for (ConfuseModifyMethodRecord confuseModifyMethodRecord : methodRecordList) {
            if (confuseModifyMethodRecord.getOldName().equals(name)
                    && confuseModifyMethodRecord.getDescriptor().equals(descriptor)) {
                return confuseModifyMethodRecord;
            }
        }
        return null;
    }

    public ConfuseModifyMethodRecord determineMethodOperationMode(String sign, int access, String name, String descriptor) {
        ConfuseModifyClassRecord confuseModifyClassRecord = confuseClassModifyMap.get(sign);
        if (confuseModifyClassRecord == null) return null;
        List<ConfuseModifyMethodRecord> methodRecordList = confuseModifyClassRecord.getMethodRecordList();
        for (ConfuseModifyMethodRecord confuseModifyMethodRecord : methodRecordList) {
            if (confuseModifyMethodRecord.getAccess() == access
                    && confuseModifyMethodRecord.getOldName().equals(name)
                    && confuseModifyMethodRecord.getDescriptor().equals(descriptor)) {
                return confuseModifyMethodRecord;
            }
        }
        return null;
    }

    public boolean autoRemoveClassSource() {
        return confuseExtension.isRemoveSource();
    }

    public boolean autoEncryptedString() {
        return confuseExtension.isAutoEncryptionString();
    }
}
