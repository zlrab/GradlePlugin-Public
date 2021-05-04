package com.zlrab.plugin.java;


/**
 * @author zlrab
 * @date 2020/12/24 18:00
 */
public interface ZMethod {
    /**
     * 获取访问权限
     *
     * @return
     */
    int getAccess();

    /**
     * 设置访问权限
     *
     * @return
     */
    ZMethod setAccess(int access);

    String getName();

    ZMethod setName(String name);

    String getDescriptor();

    ZMethod setDescriptor(String descriptor);

    String getSignature();

    ZMethod setSignature(String signature);

    String[] getExceptions();

    ZMethod setExceptions(String[] exceptions);
}
