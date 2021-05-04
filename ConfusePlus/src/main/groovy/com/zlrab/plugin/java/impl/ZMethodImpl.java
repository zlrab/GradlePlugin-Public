package com.zlrab.plugin.java.impl;

import com.zlrab.plugin.java.ZMethod;

/**
 * @author zlrab
 * @date 2020/12/24 18:03
 */
public class ZMethodImpl implements ZMethod {
    /**
     * 访问权限
     * {@link org.objectweb.asm.Opcodes}
     */
    private int access;

    private String name;

    private String descriptor;

    private String signature;

    private String[] exceptions;

    public ZMethodImpl() {
    }

    public ZMethodImpl(int access, String name, String descriptor, String signature, String[] exceptions) {
        setAccess(access);
        setName(name);
        setDescriptor(descriptor);
        setSignature(signature);
        setExceptions(exceptions);
    }

    @Override
    public int getAccess() {
        return access;
    }

    @Override
    public ZMethod setAccess(int access) {
        this.access = access;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZMethod setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public ZMethod setDescriptor(String descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public ZMethod setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    @Override
    public String[] getExceptions() {
        return exceptions;
    }

    @Override
    public ZMethod setExceptions(String[] exceptions) {
        this.exceptions = exceptions;
        return this;
    }
}
