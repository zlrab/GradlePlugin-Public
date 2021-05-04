package com.zlrab.plugin.java;

/**
 * @author zlrab
 * @date 2020/12/24 18:00
 */
public interface ZField {
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
    ZField setAccess(int access);

    String getName();

    ZField setName(String name);

    String getDescriptor();

    ZField setDescriptor(String descriptor);

    String getSignature();

    ZField setSignature(String signature);

    Object getValue();

    ZField setValue(Object value);

    boolean hasExtra(String key);

    Object getExtra(String key);

    ZField putExtra(String key, Object tag);

    boolean getBooleanExtra(String key);
}
