package com.zlrab.plugin.proxy;

import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.work.confuse.ConfuseModifyClassRecord;
import com.zlrab.plugin.work.confuse.ConfuseModifyMethodRecord;
import com.zlrab.plugin.work.confuse.ConfusePlusManager;
import com.zlrab.tool.Base64;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfuseNativeStringProxy implements BaseConfuseStringClassProxy {
    private Map<String, String> modifyMap = new HashMap<>();
    private ConfuseModifyClassRecord confuseModifyClassRecord;
    private String key;
    private String decodeClassName;
    private String decodeMethodName;
    private String confuseDecodeClassName;
    private String confuseDecodeMethodName;
    private String byteArrayName;
    private byte[] so_name_bytes;

    public ConfuseNativeStringProxy(String decodeClassName, String decodeMethodName, String byteArrayName, byte[] bytesData) {
        this(decodeClassName, decodeMethodName, byteArrayName, bytesData, RandomTool.randomName());
    }

    public ConfuseNativeStringProxy(String decodeClassName, String decodeMethodName, String byteArrayName, byte[] bytesData, String key) {
        this.key = key == null || key.length() <= 0 ? RandomTool.randomName() : key;
        this.decodeClassName = decodeClassName;
        this.decodeMethodName = decodeMethodName;
        this.byteArrayName = byteArrayName;
        this.so_name_bytes = bytesData;
        confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(ConversionTool.classNameToSign(decodeClassName));

        confuseDecodeClassName = confuseModifyClassRecord == null ? decodeClassName : ConversionTool.signToClassName(confuseModifyClassRecord.getNewSign());

        ConfuseModifyMethodRecord methodRecord = ConfusePlusManager.getInstance().determineMethodOperationModeIgnoreAccess(ConversionTool.classNameToSign(decodeClassName), decodeMethodName, Const.DECODE_METHOD_PARAMS_SIGN);
        confuseDecodeMethodName = confuseModifyClassRecord == null ? decodeMethodName : methodRecord.getNewName();

        Collection<ConfuseModifyClassRecord> confuseModifyClassRecords = ConfusePlusManager.getInstance().readAllRecord();
        if (confuseModifyClassRecords != null && confuseModifyClassRecords.size() > 0) {
            confuseModifyClassRecords.forEach(classRecord -> {
                modifyMap.put(classRecord.getOldSign(), classRecord.getNewSign());
                modifyMap.put(classRecord.getOldSign().replace("/", "."), classRecord.getNewSign().replace("/", "."));
                List<ConfuseModifyMethodRecord> methodRecordList = classRecord.getMethodRecordList();
                methodRecordList.forEach(confuseModifyMethodRecord -> modifyMap.put(confuseModifyMethodRecord.getOldName(), confuseModifyMethodRecord.getNewName()));
                LogTool.d(classRecord.toString());
            });
        }

    }

    @Override
    public boolean check() {
        return confuseModifyClassRecord != null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public BaseConfuseStringClassProxy setKey(String key) {
        this.key = key == null || key.length() <= 0 ? this.key : key;
        return this;
    }

    @Override
    public String getDecodeOldClassName() {
        return decodeClassName;
    }

    @Override
    public String getDecodeOldMethodName() {
        return decodeMethodName;
    }

    @Override
    public String getConfuseDecodeClassName() {
        return confuseDecodeClassName;
    }

    @Override
    public String getConfuseDecodeMethodName() {
        return confuseDecodeMethodName;
    }

    @Override
    public boolean needModifyByteArrayKey() {
        return true;
    }

    @Override
    public byte[] getRandomByteArrayKey() {
        return so_name_bytes;
    }

    @Override
    public String getByteArrayKeyName() {
        return byteArrayName;
    }

    @Override
    public String encode(String data) {
        return encode(data, key);
    }

    @Override
    public String encode(String data, String key) {
        String newData;
        newData = new String(Base64.encode(data.getBytes(), Base64.NO_WRAP));
        return newData;
    }

    @Override
    public String decode(String data) {
        return decode(data, key);
    }

    @Override
    public String decode(String data, String key) {
        String newData;
        newData = new String(Base64.decode(data, Base64.NO_WRAP), StandardCharsets.UTF_8);
        return newData;
    }

    private static byte[] xor(byte[] data, String key) {
        int len = data.length;
        int lenKey = key.length();
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key.charAt(j));
            i++;
            j++;
        }
        return data;
    }

    public Map<String, String> getModifyMap() {
        return modifyMap;
    }

    @Override
    public String getNativeDecodeMethodName() {
        return "aaaaa";
    }
}
