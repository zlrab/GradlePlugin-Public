package com.zlrab.plugin.java.impl;

import com.zlrab.plugin.java.ZField;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zlrab
 * @date 2020/12/24 18:02
 */
public class ZFiledImpl implements ZField {
    /**
     * 访问权限
     * {@link org.objectweb.asm.Opcodes}
     */
    private int access;

    private String name;

    private String descriptor;

    private String signature;

    private Object value;

    private Map<String, Object> extraMap = new HashMap<>();

    public ZFiledImpl() {
    }

    public ZFiledImpl(int access, String name, String descriptor, String signature, Object value) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.value = value;
    }

    @Override
    public int getAccess() {
        return access;
    }

    @Override
    public ZField setAccess(int access) {
        this.access = access;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZField setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public ZField setDescriptor(String descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public ZField setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public ZField setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean hasExtra(String key) {
        return extraMap.containsKey(key);
    }

    @Override
    public Object getExtra(String key) {
        return extraMap.get(key);
    }

    @Override
    public ZField putExtra(String key, Object tag) {
        extraMap.put(key, tag);
        return this;
    }

    @Override
    public boolean getBooleanExtra(String key) {
        Object o = extraMap.get(key);
        if (o instanceof Boolean) return (boolean) o;
        return false;
    }
}
