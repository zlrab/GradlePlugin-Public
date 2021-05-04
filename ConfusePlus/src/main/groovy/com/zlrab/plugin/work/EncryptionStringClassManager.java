package com.zlrab.plugin.work;


import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.extension.ConfuseExtension;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.proxy.BaseConfuseStringClassProxy;
import com.zlrab.plugin.proxy.ConfuseNativeStringProxy;
import com.zlrab.plugin.proxy.EncryptionStringClassProxy;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import org.gradle.api.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/30 16:06
 */
public class EncryptionStringClassManager {
    private static EncryptionStringClassManager sEncryptionStringClassManager;

    private List<BaseConfuseStringClassProxy> encryptionStringClassOldProxyList = new ArrayList<>();

    private ConfuseExtension confuseExtension;

    public static void dyLoadEncryptionPluginJar(ConfuseExtension confuseExtension) {
        if (sEncryptionStringClassManager == null) {
            sEncryptionStringClassManager = new EncryptionStringClassManager(confuseExtension);
        } else {
            LogTool.w("EncryptionStringClassManager.dyLoadEncryptionPluginJar(String,com.zlrab.plugin.extensionConfuseExtension)Only allow to call once, do not call repeatedly");
        }
    }

    public static EncryptionStringClassManager getInstance() {
        if (sEncryptionStringClassManager == null)
            throw new RuntimeException("Please call dyLoadEncryptionPluginJar() first to initialize this class");
        return sEncryptionStringClassManager;
    }

    public @Nullable
    BaseConfuseStringClassProxy getRandomProxy() {
        if (confuseExtension == null || !confuseExtension.isAutoEncryptionString()) return null;
        return encryptionStringClassOldProxyList.get(RandomTool.randomRangeNumber(encryptionStringClassOldProxyList.size()));
    }

    public @Nullable
    ConfuseNativeStringProxy getNativeProxy() {
        if (confuseExtension == null || !confuseExtension.isAutoEncryptionString()) return null;
        return (ConfuseNativeStringProxy) Enhance.findByElement(encryptionStringClassOldProxyList, baseConfuseStringClassProxy -> baseConfuseStringClassProxy.getDecodeOldClassName().equals(Const.nativeClassName));
    }

    private EncryptionStringClassManager(ConfuseExtension confuseExtension) {
        if (confuseExtension == null) return;
        this.confuseExtension = confuseExtension;
        boolean encryptionStatus = confuseExtension.isAutoEncryptionString();
        if (!encryptionStatus) return;
        ConfuseNativeStringProxy confuseNativeStringProxy = new ConfuseNativeStringProxy(Const.nativeClassName, "callNativeDecode","DATA",confuseExtension.getSoName().getBytes());
        boolean confuseNativeStringProxyCheck = confuseNativeStringProxy.check();
        if (confuseNativeStringProxyCheck) {
            encryptionStringClassOldProxyList.add(confuseNativeStringProxy);
        }
        if (confuseExtension.getEncryptedStringToolJarPath() == null) return;
        File jarFile = new File(confuseExtension.getEncryptedStringToolJarPath());
        if (!jarFile.exists() || !jarFile.canRead()) {//gradle配置的jar路径不存在或无法读取，尝试读取json中配置的jar路径
            LogTool.e("The jar path configured by gradle does not exist or cannot be read, " +
                    "please check {encryptedStringToolJarPath =" + confuseExtension.getEncryptedStringToolJarPath() + " } Is it correct?" +
                    " Will try to read the jar path configured in ConfuseConf.json again");
            jarFile = new File(confuseExtension.getConf().getEncryptedString().getEncryptedStringToolJarPath());
        }
        if (confuseExtension.getConf().getEncryptedString().getEncryptedStringToolJarPath() == null)
            return;
        if (!jarFile.exists() || !jarFile.canRead()) {//如果json中配置的jar路径也不存在或无法读取
            LogTool.e("The jar path configured in ConfuseConf.json does not exist or cannot be read, " +
                    "please check encryptedStringToolJarPath = " + confuseExtension.getConf().getEncryptedString().getEncryptedStringToolJarPath() + " , Disable encrypted string!!!!");
        }
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
            List<ConfuseExtension.Conf.EncryptedStringEntity.EncryptionToolClassEntity> encryptionToolClassList = confuseExtension.getConf().getEncryptedString().getEncryptionToolClass();
            for (ConfuseExtension.Conf.EncryptedStringEntity.EncryptionToolClassEntity encryptionToolClassEntity : encryptionToolClassList) {
                try {
                    Class<?> encodeClass = urlClassLoader.loadClass(encryptionToolClassEntity.getEncodeClassName());
                    Class<?> decodeClass = urlClassLoader.loadClass(encryptionToolClassEntity.getDecodeClassName());
                    EncryptionStringClassProxy proxy = new EncryptionStringClassProxy(
                            encodeClass,
                            encryptionToolClassEntity.getEncodeMethodName(),
                            decodeClass, encryptionToolClassEntity.getDecodeMethodName(),
                            encryptionToolClassEntity.getKey(),
                            encryptionToolClassEntity.getByteArrayKeyName());
                    boolean check = proxy.check();
                    if (check) {
                        encryptionStringClassOldProxyList.add(proxy);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (encryptionStringClassOldProxyList.size() == 0) {
                confuseExtension.setAutoEncryptionString(false);
                LogTool.e("All encryption and decryption functions configured in ConfuseConf.json fail to verify, disable encrypted string!!!!");
            }
            LogTool.d("encryptionStringClassOldProxyList size = " + encryptionStringClassOldProxyList.size());
        } catch (MalformedURLException e) {
            LogTool.e("Unable to load the encryption and decryption jar, please check !!!!", e);
        } finally {
            LogTool.d("encryptionStringClassOldProxyList size = " + encryptionStringClassOldProxyList.size());
        }
    }

    public BaseConfuseStringClassProxy needModifyByteArrayKey(ZClass zClass) {
        String className = ConversionTool.signToClassName(zClass.getClassName());
        BaseConfuseStringClassProxy byElement = Enhance.findByElement(encryptionStringClassOldProxyList, encryptionStringClassProxy -> encryptionStringClassProxy.getDecodeOldClassName().equals(className));
        if (byElement != null && byElement.needModifyByteArrayKey() && byElement.getRandomByteArrayKey() != null)
            return byElement;
        return null;
    }
}
