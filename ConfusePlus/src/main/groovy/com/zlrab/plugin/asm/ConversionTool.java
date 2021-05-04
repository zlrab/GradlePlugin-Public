package com.zlrab.plugin.asm;

import java.io.File;

/**
 * sign : com/zlrab/demo Suitable for asm to indicate the format of the class path
 * className : com.zlrab.demo
 * descriptor : Lcom/zlrab/demo;
 *
 * @author zlrab
 * @date 2020/12/29 17:22
 */
public class ConversionTool {
    /**
     * @param sign com/zlrab/demo
     * @return Lcom/zlrab/demo;
     */
    public static String signToDescriptor(String sign) {
        return "L" + sign + ";";
    }

    /**
     * @param sign com/zlrab/demo
     * @return Lcom/zlrab/demo;
     */
    public static String signToSignature(String sign) {
        return signToDescriptor(sign);
    }

    public static String descriptorToSign(String descriptor) {
        return descriptor.substring(0, descriptor.length() - 1).substring(1);
    }

    /**
     * @param className com.zlrab.demo
     * @return com/zlrab/demo
     */
    public static String classNameToSign(String className) {
        return className.replace('.', '/');
    }

    /**
     * @param sign com/zlrab/demo
     * @return com.zlrab.demo
     */
    public static String signToClassName(String sign) {
        return sign.replace('/', '.');
    }

    public static String signToFileSys(String sign) {
        return sign.replace("/", File.separator);
    }

    public static String stringName(String name) {
        return "\"" + name + "\"";
    }
}
