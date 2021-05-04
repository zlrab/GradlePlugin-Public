package com.zlrab.plugin.proxy;

import com.zlrab.plugin.Const;
import com.zlrab.plugin.Reflect;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.work.confuse.ConfuseModifyClassRecord;
import com.zlrab.plugin.work.confuse.ConfuseModifyMethodRecord;
import com.zlrab.plugin.work.confuse.ConfusePlusManager;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author zlrab
 * @date 2020/12/30 16:40
 */
public class EncryptionStringClassProxy implements BaseConfuseStringClassProxy {
    private Class<?> encodeClass;
    private String encodeMethodName;
    private Class<?> decodeClass;
    private String decodeMethodName;
    private String key;
    private String byteArrayKeyName;
    private boolean needModifyByteArrayKey = false;
    private byte[] randomByteArrayKey;
    /**
     * com.zlrab.demo
     */
    private String confuseDecodeClassName;

    private String confuseDecodeMethodName;

    public EncryptionStringClassProxy(Class<?> encodeClass, String encodeMethodName, Class<?> decodeClass, String decodeMethodName) {
        this(encodeClass, encodeMethodName, decodeClass, decodeMethodName, null, null);
    }

    public EncryptionStringClassProxy(Class<?> encodeClass, String encodeMethodName, Class<?> decodeClass, String decodeMethodName, String key, String byteArrayKeyName) {
        this.encodeClass = encodeClass;
        this.encodeMethodName = encodeMethodName;
        this.decodeClass = decodeClass;
        this.decodeMethodName = decodeMethodName;
        this.key = key == null || key.length() == 0 ? RandomTool.randomName() : key;
        this.byteArrayKeyName = byteArrayKeyName;
        processByteArrayKey();

        ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(ConversionTool.classNameToSign(decodeClass.getName()));
        confuseDecodeClassName = confuseModifyClassRecord != null ? ConversionTool.signToClassName(confuseModifyClassRecord.getNewSign()) : decodeClass.getName();

        ConfuseModifyMethodRecord confuseModifyMethodRecord = ConfusePlusManager.getInstance().determineMethodOperationModeIgnoreAccess(ConversionTool.classNameToSign(decodeClass.getName()), decodeMethodName, Const.DECODE_METHOD_PARAMS_SIGN);
        confuseDecodeMethodName = confuseModifyMethodRecord != null ? confuseModifyMethodRecord.getNewName() : decodeMethodName;

        LogTool.e(toString());
    }

    private void processByteArrayKey() {
        if (byteArrayKeyName == null || byteArrayKeyName.length() == 0) return;
        try {
            randomByteArrayKey = RandomTool.randomName(20, 40).getBytes();
            Field encodeClassByteArrayKeyField = Enhance.findByElement(encodeClass.getDeclaredFields(), field -> field.getName().equals(byteArrayKeyName) && field.getType().equals(byte[].class) && Modifier.isStatic(field.getModifiers()));
            Field decodeClassByteArrayKeyField = Enhance.findByElement(decodeClass.getDeclaredFields(), field -> field.getName().equals(byteArrayKeyName) && field.getType().equals(byte[].class) && Modifier.isStatic(field.getModifiers()));
            if (encodeClassByteArrayKeyField == null || decodeClassByteArrayKeyField == null)
                return;
            encodeClassByteArrayKeyField.setAccessible(true);
            decodeClassByteArrayKeyField.setAccessible(true);
            encodeClassByteArrayKeyField.set(null, randomByteArrayKey);
            decodeClassByteArrayKeyField.set(null, randomByteArrayKey);
            needModifyByteArrayKey = true;
            LogTool.e("DEBUG processByteArrayKey : " + toString());
        } catch (Throwable e) {
            LogTool.e("modify byte[] key error", e);
        }
    }

    public String getByteArrayKeyName() {
        return byteArrayKeyName;
    }

    public boolean needModifyByteArrayKey() {
        return needModifyByteArrayKey;
    }

    @Override
    public byte[] getRandomByteArrayKey() {
        return randomByteArrayKey;
    }

    /**
     * @return
     */
    @Override
    public boolean check() {
        try {
            Method encodeMethod = encodeClass.getDeclaredMethod(encodeMethodName, String.class, String.class);
        } catch (Throwable e) {
            e.printStackTrace();
            LogTool.e("check error , From [ " + encodeClass + " ] Search encryption function [ public static java.lang.String " + encodeMethodName + "(java.lang.String,java.lang.String) ] An error has occurred," +
                    " this encryption class will be discarded!!!!");
            return false;
        }
        try {
            decodeClass.getDeclaredMethod(decodeMethodName, String.class, String.class);
        } catch (Throwable e) {
            e.printStackTrace();
            LogTool.e("check error , From [ " + decodeClass + " ] Search decryption function [ public static java.lang.String " + decodeMethodName + "(java.lang.String,java.lang.String) ] An error has occurred," +
                    " this decryption class will be discarded!!!!");
            return false;
        }
        try {
            String encode = encode(Const.CHECK_ENCRYPTION_STRING_DATA);
            String decode = decode(encode);
            if (!Const.CHECK_ENCRYPTION_STRING_DATA.equals(decode)) {
                LogTool.e("check error , The decrypted value is inconsistent with the original value,[ " + Const.CHECK_ENCRYPTION_STRING_DATA + " ≠ " + decode + "] , " +
                        "Please check the encryption and decryption functions: encodeClass = [" + encodeClass + "]\tencodeMethodName = [" + encodeMethodName + "]\tdecodeClass = [" + decodeClass + "]\tdecodeMethodName = [" + decodeMethodName + "] , " +
                        " this decryption class will be discarded!!!!");
                return false;
            }
        } catch (Throwable e) {
            LogTool.e("check error , An exception occurred while testing the encryption and decryption functions, " +
                    "Please check the encryption and decryption functions: encodeClass = [" + encodeClass + "]\tencodeMethodName = [" + encodeMethodName + "]\tdecodeClass = [" + decodeClass + "]\tdecodeMethodName = [" + decodeMethodName + "] , " +
                    " this decryption class will be discarded!!!!", e);
            return false;
        }
        return true;
    }

    @Override
    public String encode(String data) {
        return Reflect.on(encodeClass).call(encodeMethodName, data, key).get();
    }

    @Override
    public String encode(String data, String key) {
        return data;
    }

    @Override
    public String decode(String data) {
        return Reflect.on(decodeClass).call(decodeMethodName, data, key).get();
    }

    @Override
    public String decode(String data, String key) {
        return data;
    }

    public Class<?> getEncodeClass() {
        return encodeClass;
    }

    public String getEncodeMethodName() {
        return encodeMethodName;
    }

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
        return decodeClass.getName();
    }

    @Override
    public String getDecodeOldMethodName() {
        return encodeClass.getName();
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
    public String getNativeDecodeMethodName() {
        return null;
    }

    @Override
    public String toString() {
        return "EncryptionStringClassProxy{" +
                "encodeClass=" + encodeClass.getName() +
                ", encodeMethodName='" + encodeMethodName + '\'' +
                ", decodeClass=" + decodeClass.getName() +
                ", decodeMethodName='" + decodeMethodName + '\'' +
                ", key='" + key + '\'' +
                ", byteArrayKeyName='" + byteArrayKeyName + '\'' +
                ", needModifyByteArrayKey=" + needModifyByteArrayKey +
                ", randomByteArrayKey=" + Arrays.toString(randomByteArrayKey) +
                ", confuseDecodeClassName='" + confuseDecodeClassName + '\'' +
                ", confuseDecodeMethodName='" + confuseDecodeMethodName + '\'' +
                '}';
    }
}
