package com.zlrab.plugin.work.confuse;

import com.zlrab.core.ResType;
import com.zlrab.plugin.Const;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.work.LogManager;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.FileTool;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zlrab
 * @date 2021/1/8 16:45
 */
public class ResConfusePlusManager {
    private static ResConfusePlusManager resConfusePlusManager;
    private final ConfuseExtension confuseExtension;
    /**
     * com.zlrab.demo
     */
    private String packageName;
    private final Map<ResType, Set<ResModifyRecord>> resTypeSetMap = new ConcurrentHashMap<>();

    protected void addResModifyRecord(ResModifyRecord resModifyRecord) {
        ResType resType = resModifyRecord.getResType();
        getResModifyRecords(resType).add(resModifyRecord);
        LogManager.getInstance().mappingWrite(
                LogManager.getInstance().buildMappingLine(
                        resModifyRecord.getResType().getRName(),
                        resModifyRecord.getOldName(),
                        resModifyRecord.getNewName()
                )
        );
    }

    /**
     * 不用通过此函数的返回值添加修改记录 统一使用{@link ResConfusePlusManager#addResModifyRecord(ResModifyRecord)}添加记录
     *
     * @param resType {@link ResType}
     * @return 指定resType的修改记录容器
     */
    protected Set<ResModifyRecord> getResModifyRecords(ResType resType) {
        Set<ResModifyRecord> resModifyRecords = resTypeSetMap.get(resType);
        if (resModifyRecords == null) {
            resModifyRecords = new HashSet<>();
            resTypeSetMap.put(resType, resModifyRecords);
        }
        return resModifyRecords;
    }

    protected boolean hasKey(ResType resType) {
        return resTypeSetMap.containsKey(resType);
    }

    public ResModifyRecord position(ResType resType, String oldName) {
        Set<ResModifyRecord> resModifyRecords = getResModifyRecords(resType);
        return Enhance.findByElement(resModifyRecords, resModifyRecord -> resModifyRecord.getOldName().equals(oldName));
    }

    private ResConfusePlusManager(ConfuseExtension confuseExtension) {
        this.confuseExtension = confuseExtension;
    }

    public static void initResConfusePlusManager(ConfuseExtension confuseExtension) {
        LogTool.d("call initResConfusePlusManager");
        if (resConfusePlusManager == null) {
            synchronized (ResConfusePlusManager.class) {
                if (resConfusePlusManager == null) {
                    resConfusePlusManager = new ResConfusePlusManager(confuseExtension);
                }
            }
        } else {
            LogTool.w("ResConfusePlusManager.initResConfusePlusManager(String,com.zlrab.plugin.extension.ConfuseExtension) Only allow to call once, do not call repeatedly");
        }
    }

    public static ResConfusePlusManager getInstance() {
        if (resConfusePlusManager == null)
            throw new RuntimeException("Please call initResConfusePlusManager() first to initialize this class");
        return resConfusePlusManager;
    }

    public void parsing(File bundleDir) throws FileNotFoundException {
        parsing(new File(bundleDir, "res"), new File(bundleDir, "AndroidManifest.xml"), new File(bundleDir, "R.txt"));
    }

    public void parsing(File resDir, File androidManifestFile, File rFile) throws FileNotFoundException {
        LogTool.d("start parsing res");
        if (!confuseExtension.isConfuseResName()) {
            LogTool.d("confuseExtension.confuseResName is false");
            return;
        }
        if (!FileTool.checkAccessPermission(resDir, FileTool.NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN)) {
            LogTool.w("No read and write permissions, path = " + resDir);
            return;
        }
        if (!FileTool.checkAccessPermission(androidManifestFile, FileTool.NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN)) {
            LogTool.w("No read and write permissions, path = " + androidManifestFile);
            return;
        }
        if (!FileTool.checkAccessPermission(rFile, FileTool.NOT_EMPTY_AND_EXISTS_AND_CAN_READ_AND_WRITTEN)) {
            LogTool.w("No read and write permissions, path = " + rFile);
            return;
        }
        packageName = parsingPackageName(androidManifestFile);
        //修改资源名
        Enhance.forEach(resDir.listFiles(), childResourceDir -> {
            String dirName = childResourceDir.getName();
            if (Enhance.wildcardStarMatch("anim*", dirName)) {
                parsingResDir(ResType.anim, childResourceDir);
            } else if (Enhance.wildcardStarMatch("animator*", dirName)) {
                parsingResDir(ResType.animator, childResourceDir);
            } else if (Enhance.wildcardStarMatch("color*", dirName)) {
                parsingResDir(ResType.color, childResourceDir);
            } else if (Enhance.wildcardStarMatch("drawable*", dirName)) {
                parsingResDir(ResType.drawable, childResourceDir);
            } else if (Enhance.wildcardStarMatch("font*", dirName)) {
                parsingResDir(ResType.font, childResourceDir);
            } else if (Enhance.wildcardStarMatch("layout*", dirName)) {
                parsingResDir(ResType.layout, childResourceDir);
            } else if (Enhance.wildcardStarMatch("menu*", dirName)) {
                parsingResDir(ResType.menu, childResourceDir);
            } else if (Enhance.wildcardStarMatch("mipmap*", dirName)) {
                parsingResDir(ResType.mipmap, childResourceDir);
            } else if (Enhance.wildcardStarMatch("navigation*", dirName)) {
                parsingResDir(ResType.navigation, childResourceDir);
            } else if (Enhance.wildcardStarMatch("raw*", dirName)) {
                parsingResDir(ResType.raw, childResourceDir);
            } else if (Enhance.wildcardStarMatch("transition*", dirName)) {
                parsingResDir(ResType.transition, childResourceDir);
            } else if (Enhance.wildcardStarMatch("xml*", dirName)) {
                parsingResDir(ResType.xml, childResourceDir);
            } else if (Enhance.wildcardStarMatch("values*", dirName)) {
                FileTool.traversingFile(childResourceDir, this::parsingResValue);
            }
        });
        //修改id
        FileTool.traversingFile(resDir, file -> {
            if (!ResType.xml.getRName().equals(FilenameUtils.getExtension(file.getName()))) return;
            parsingResId(file);
        });

        //修改资源在xml中的引用
        modifyResReferenceInXml(androidManifestFile);
        FileTool.traversingFile(resDir, file -> {
            if (!ResType.xml.getRName().equals(FilenameUtils.getExtension(file.getName()))) return;
            modifyResReferenceInXml(file);
        });
        //修改资源在R.txt中的声明
        modifyResReferrerInR(rFile);
    }


    private void parsingResDir(ResType resType, File resDir) {
        LogTool.d("parsingResFile resType  = " + resType + "\tresDir = " + resDir.getAbsolutePath());
        FileTool.traversingFile(resDir, resFile -> modifyFileTypeRes(resType, resFile));
    }

    private void parsingResValue(File valueFile) {
        SAXReader saxReader = new SAXReader();
        try {
            Document valueXmlDocument = saxReader.read(valueFile);
            Element rootElement = valueXmlDocument.getRootElement();
            rootElement.elementIterator().forEachRemaining(childElement -> {
                String elementName = childElement.getName();
                if (ResType.string.getRName().equals(elementName)) {
                    modifyValueTypeRes(ResType.string, valueFile, childElement);
                } else if ("declare-styleable".equals(elementName)) {
                    //顺序不可颠倒
                    //先解析attr
                    modifyStyleableAttrTypeRes(valueFile, childElement);
                    modifyValueTypeRes(ResType.styleable, valueFile, childElement);
                } else if (ResType.style.getRName().equals(elementName)) {
                    modifyValueTypeRes(ResType.style, valueFile, childElement);
                } else if (ResType.bool.getRName().equals(elementName)) {
                    modifyValueTypeRes(ResType.bool, valueFile, childElement);
                } else if (ResType.integer.getRName().equals(elementName)) {
                    modifyValueTypeRes(ResType.integer, valueFile, childElement);
                } else if (ResType.dimen.getRName().equals(elementName)) {
                    modifyValueTypeRes(ResType.dimen, valueFile, childElement);
                }else if (ResType.color.getRName().equals(elementName)){
                    modifyValueTypeRes(ResType.color,valueFile,childElement);
                }
            });
            FileTool.writeInDocument(valueFile, valueXmlDocument);
        } catch (Throwable e) {
            LogTool.e("parsingResValue error", e);
        }
    }

    private void parsingResId(File xmlFile) {
        SAXReader saxReader = new SAXReader();
        try {
            Document xmlReadDocument = saxReader.read(xmlFile);
            Enhance.recursiveAllAttr(xmlReadDocument.getRootElement(), (rootElement, currentElement, attribute) -> {
                String value = attribute.getValue();
                if (!value.startsWith("@+" + ResType.id.getRName() + "/")) return;

                String oldResName = value.substring(("@+" + ResType.id.getRName() + "/").length());

                String rName = ResType.id.buildResReferrer(oldResName);
                if (checkResNameNeedModify(rName)) return;

                List<ResModifyRecord> filter = Enhance.filter(getResModifyRecords(ResType.id), resModifyRecord -> oldResName.equals(resModifyRecord.getOldName()));
                ResModifyRecord resModifyRecord;
                if (filter.size() == 0) {
                    resModifyRecord = new ResModifyRecord(ResType.id,
                            oldResName,
                            randomResName(),
                            xmlFile,
                            false);
                    addResModifyRecord(resModifyRecord);
                } else if (filter.size() == 1) {
                    resModifyRecord = filter.get(0);
                } else {
                    throw new RuntimeException("parsingResId error , Because of duplicate records", new Throwable());
                }
                attribute.setValue("@+id/" + resModifyRecord.getNewName());
            });
            FileTool.writeInDocument(xmlFile, xmlReadDocument);
        } catch (Throwable e) {
            LogTool.e("parsingResId error", e);
        }
    }

    /**
     * 修改值类型的资源名字
     *
     * @param resType   {@link ResType}
     * @param valueFile values.xml
     * @param element   节点
     */
    private void modifyValueTypeRes(ResType resType, File valueFile, Element element) {
        Attribute nameAttr = element.attribute("name");
        if (nameAttr == null) return;
        //根据操作名单和白名单确定当前节点是否需要修改
        //app_name
        String oldResName = nameAttr.getValue();
        //R.string.app_name
        String rName = resType.buildResReferrer(oldResName);
        if (checkResNameNeedModify(rName)) return;


        List<ResModifyRecord> filter = Enhance.filter(getResModifyRecords(resType), resModifyRecord -> oldResName.equals(resModifyRecord.getOldName()));
        ResModifyRecord resModifyRecord;
        if (filter.size() == 0) {
            resModifyRecord = new ResModifyRecord(resType,
                    oldResName,
                    randomResName(),
                    valueFile,
                    false);
            addResModifyRecord(resModifyRecord);
        } else if (filter.size() == 1) {
            resModifyRecord = filter.get(0);
        } else {
            throw new RuntimeException("modifyFileTypeRes error , Because of duplicate records", new Throwable());
        }
        nameAttr.setValue(resModifyRecord.getNewName());
    }

    private void modifyStyleableAttrTypeRes(File valueFile, Element element) {
        Attribute nameAttr = element.attribute("name");
        if (nameAttr == null) return;
        String customViewName = nameAttr.getValue();

        element.elementIterator().forEachRemaining(childElement -> {
            Attribute formatAttr = childElement.attribute("format");
            if (formatAttr == null) return;
            Attribute attr = childElement.attribute("name");
            if (attr == null) return;
            String oldResName = customViewName + "_" + attr.getValue();
            String rName = ResType.styleable.buildResReferrer(customViewName + "_" + attr.getValue());
            if (checkResNameNeedModify(rName)) return;
            List<ResModifyRecord> filter = Enhance.filter(getResModifyRecords(ResType.styleable), resModifyRecord -> oldResName.equals(resModifyRecord.getOldName()));
            StyleableModifyRecord styleableModifyRecord;

            if (filter.size() == 0) {
                styleableModifyRecord = new StyleableModifyRecord(ResType.styleable, oldResName, randomResName(), valueFile);
                addResModifyRecord(styleableModifyRecord);
            } else if (filter.size() == 1) {
                styleableModifyRecord = (StyleableModifyRecord) filter.get(0);
            } else {
                throw new RuntimeException("modifyStyleableTypeRes error , Because of duplicate records", new Throwable());
            }
            attr.setValue(styleableModifyRecord.getNewName());
            String formatValue = formatAttr.getValue();
            if ("enum".equals(formatValue)) {
                Iterator<Element> iterator = childElement.elementIterator();
                while (iterator.hasNext()) {
                    Element enumElement = iterator.next();
                    Attribute enumNameAttr = enumElement.attribute("name");
                    if (enumNameAttr == null) continue;
                    StyleableModifyRecord.StyleableEnum styleableEnum = new StyleableModifyRecord.StyleableEnum(enumNameAttr.getValue(), randomResName(), oldResName);
                    enumNameAttr.setValue(styleableEnum.getNewName());
                    styleableModifyRecord.addStyleableEnum(styleableEnum);
                }
            }
        });
    }

    /**
     * 修改文件类型的资源名字
     *
     * @param resType {@link ResType}
     * @param resFile 资源文件
     */
    private void modifyFileTypeRes(ResType resType, File resFile) {
        //根据操作名单和白名单确定当前资源文件是否需要修改
        String resFileName = resFile.getName();
        String rName = resType.buildResReferrer(FilenameUtils.getBaseName(resFileName));
        if (checkResNameNeedModify(rName)) return;


        String temp = FilenameUtils.getBaseName(resFile.getName());
        String oldResName = temp.endsWith(".9") ? temp.substring(0, temp.length() - 2) : temp;

        List<ResModifyRecord> filter = Enhance.filter(getResModifyRecords(resType), resModifyRecord -> oldResName.equals(resModifyRecord.getOldName()));
        ResModifyRecord resModifyRecord;
        if (filter.size() == 0) {
            resModifyRecord = new ResModifyRecord(resType,
                    oldResName,
                    randomResName(),
                    resFile,
                    true);
            addResModifyRecord(resModifyRecord);
        } else if (filter.size() == 1) {
            resModifyRecord = filter.get(0);
        } else {
            throw new RuntimeException("modifyFileTypeRes error , Because of duplicate records", new Throwable());
        }
        try {
            FileUtils.moveFile(resFile, new File(resFile.getParent(), resFile.getName().replace(oldResName, resModifyRecord.getNewName())));
        } catch (IOException e) {
            LogTool.e("move res file error , Do not process this resource , file = " + resFile.getAbsolutePath());
        }
    }

    /**
     * 修改在xml中对资源名的引用
     *
     * @param xmlFile xml文件
     */
    public void modifyResReferenceInXml(File xmlFile) {
        SAXReader saxReader = new SAXReader();
        try {
            Document xmlReadDocument = saxReader.read(xmlFile);
            Element xmlRootElement = xmlReadDocument.getRootElement();
            AtomicBoolean hasCustomViewNameSpace = new AtomicBoolean(false);
            AtomicReference<String> nameSpaceRef = new AtomicReference<>("");
            xmlRootElement.additionalNamespaces().forEach(namespace -> {
                if (Const.CUSTOM_VIEW_NAMESPACE.equals(namespace.getURI())) {
                    hasCustomViewNameSpace.set(true);
                    nameSpaceRef.set(namespace.getPrefix());
                }
            });
            Set<ResModifyRecord> styleableResModifyRecords = getResModifyRecords(ResType.styleable);
            //修改资源引用

            Enhance.recursiveAllAttrIterator(xmlRootElement, (rootElement, currentElement, attributeIterator) -> {
                List<AttributeRecord> attributeRecordList = new ArrayList<>();
                String text = currentElement.getTextTrim();

                if (text != null && text.length() > 0) {
                    ResType textResType = indexResReferrerType(text);
                    if (textResType != null) {
                        Set<ResModifyRecord> resModifyRecords = getResModifyRecords(textResType);
                        String resName = textResType.cropNameFromXmlReferrer(text);
                        ResModifyRecord resModifyRecord = Enhance.findByElement(resModifyRecords, filterResModifyRecord -> filterResModifyRecord.getOldName().equals(resName));
                        if (resModifyRecord != null) {
                            String newValue = textResType.buildXmlReferrer(resModifyRecord.getNewName());
                            currentElement.setText(newValue);
                        }
                    }
                }

                while (attributeIterator.hasNext()) {
                    Attribute attribute = attributeIterator.next();
                    //@string/app_name
                    String value = attribute.getValue();
                    ResType resType = indexResReferrerType(value);
                    if (resType != null) {
                        Set<ResModifyRecord> resModifyRecords = getResModifyRecords(resType);
                        String resName = resType.cropNameFromXmlReferrer(value);
                        ResModifyRecord resModifyRecord = Enhance.findByElement(resModifyRecords, filterResModifyRecord -> filterResModifyRecord.getOldName().equals(resName));
                        if (resModifyRecord != null) {
                            String newValue = resType.buildXmlReferrer(resModifyRecord.getNewName());
                            attribute.setValue(newValue);
                        }
                    }

                    //包含自定义view命名空间且当前attr的nameSpace等于定义的顶级nameSpace
                    if (hasCustomViewNameSpace.get() && nameSpaceRef.get().equals(attribute.getNamespace().getPrefix())) {
                        String customViewName = currentElement.getName();
                        String temp = customViewName + "_" + attribute.getName();
                        ResModifyRecord resModifyRecord = Enhance.findByElement(styleableResModifyRecords, filterResModifyRecord -> temp.endsWith(filterResModifyRecord.getOldName()));
                        if (resModifyRecord instanceof StyleableModifyRecord) {
                            StyleableModifyRecord styleableModifyRecord = (StyleableModifyRecord) resModifyRecord;
                            AttributeRecord attributeRecord = new AttributeRecord(attribute.getValue(), QName.get(styleableModifyRecord.getNewName(), attribute.getNamespace()));
                            StyleableModifyRecord.StyleableEnum styleableEnum = Enhance.findByElement(styleableModifyRecord.getStyleableEnumList(), filterStyleableEnum -> attribute.getValue().equals(filterStyleableEnum.getOldName()));
                            if (styleableEnum != null) {
                                attributeRecord.newValue = styleableEnum.getNewName();
                            }
                            attributeIterator.remove();
                            attributeRecordList.add(attributeRecord);
                        }
                    }
                }
                attributeRecordList.forEach(attributeRecord -> currentElement.addAttribute(attributeRecord.qName, attributeRecord.newValue));
            });

            Enhance.recursiveAllAttr(xmlRootElement, (rootElement, currentElement, attribute) -> {


            });

            FileTool.writeInDocument(xmlFile, xmlReadDocument);
        } catch (DocumentException e) {
            LogTool.e("modifyResReferenceInXml error", e);
        }
    }

    /**
     * int[] styleable TestView { 0x7f010009, 0x7f01000a, 0x7f01000b }
     * int styleable TestView_testIcon 2
     *
     * @param rFile R.txt
     * @throws FileNotFoundException R.txt无法访问异常
     */
    private void modifyResReferrerInR(File rFile) throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(rFile));
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.lines().forEach(line -> {
            String[] lines = line.split(" ");
            ResType resType = ResType.positionResType(lines[1]);
            String oldName = lines[2];
            String newLine = line;
            if (resType != null && hasKey(resType)) {
                Set<ResModifyRecord> resModifyRecords = getResModifyRecords(resType);
                ResModifyRecord resModifyRecord = Enhance.findByElement(resModifyRecords, resModifyRecord1 -> resModifyRecord1.getResType() == resType && resModifyRecord1.getOldName().equals(oldName));
                if (resModifyRecord != null) {
                    lines[2] = resModifyRecord.getNewName();
                    StringBuilder lineStringBuilder = new StringBuilder();
                    Enhance.forEach(lines, s -> lineStringBuilder.append(s).append(" "));
                    newLine = lineStringBuilder.toString();
                    newLine = newLine.substring(0, newLine.length() - 1);
                }
            }
            stringBuilder.append(newLine).append("\r\n");
        });
        FileTool.writeInStrings(rFile, stringBuilder.toString());
    }

    private String parsingPackageName(File androidManifestFile) {
        SAXReader saxReader = new SAXReader();
        try {
            Document read = saxReader.read(new FileInputStream(androidManifestFile));
            return read.getRootElement().attributeValue("package");
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    private String randomResName() {
        String randomName = (confuseExtension.getConf().getConfuseResName().getPrefix() + RandomTool.randomName().toLowerCase());
        if (resNameList.contains(randomName)) {
            return randomResName();
        }
        resNameList.add(randomName);
        return randomName;
    }

    private final List<String> resNameList = new ArrayList<>();

    private boolean checkResNameNeedModify(String resRReferrer) {
        List<String> whiteList = Enhance.filter(confuseExtension.getConf().getConfuseResName().getWhitelist(), s -> Enhance.wildcardStarMatch(s, resRReferrer));
        if (whiteList.size() > 0) return true;
        List<String> matchList = Enhance.filter(confuseExtension.getConf().getConfuseResName().getMatch(), s -> Enhance.wildcardStarMatch(s, resRReferrer));
        return matchList.size() <= 0;
    }

    private ResType indexResReferrerType(String resXmlReferrer) {
        for (ResType resType : ResType.values()) {
            String workReferrer = "@" + resType.getRName() + "/";
            if (resXmlReferrer.startsWith(workReferrer)) {
                return resType;
            }
        }
        return null;
    }

    public String getPackageName() {
        return packageName;
    }
}
