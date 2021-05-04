package com.zlrab.plugin.proxy;

public interface BaseConfuseStringClassProxy {
    String encode(String data);

    String encode(String data, String key);

    String decode(String data);

    String decode(String data, String key);

    boolean check();

    String getKey();

    BaseConfuseStringClassProxy setKey(String key);

    String getDecodeOldClassName();

    String getDecodeOldMethodName();

    String getConfuseDecodeClassName();

    String getConfuseDecodeMethodName();

    boolean needModifyByteArrayKey();

    byte[] getRandomByteArrayKey();

    String getByteArrayKeyName();

    String getNativeDecodeMethodName();
}
