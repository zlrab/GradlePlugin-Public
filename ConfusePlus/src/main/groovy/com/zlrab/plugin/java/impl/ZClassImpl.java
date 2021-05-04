package com.zlrab.plugin.java.impl;

import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.java.ZMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/24 18:02
 */
public class ZClassImpl implements ZClass {
    private File classFile;
    private List<ZField> zFieldList = new ArrayList<>();
    private List<ZMethod> zMethodList = new ArrayList<>();
    private String className;
    private String superClassName;

    public ZClassImpl(File classFile) {
        this.classFile = classFile;
    }

    @Override
    public List<ZField> fieldList() {
        return zFieldList;
    }

    @Override
    public List<ZMethod> methodList() {
        return zMethodList;
    }

    @Override
    public String classFileName() {
        return null;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public ZClass setClassName(String className) {
        this.className = className.replace(".", "/");
        return this;
    }

    @Override
    public String getSuperClassName() {
        return superClassName;
    }

    @Override
    public ZClass setSuperClassName(String superClassName) {
        this.superClassName = superClassName.replace(".", "/");
        return this;
    }

    @Override
    public File getFile() {
        return classFile;
    }

    @Override
    public String toString() {
        return "ZClassImpl{" +
                "classFile=" + classFile +
                ", className='" + className + '\'' +
                ", superClassName='" + superClassName + '\'' +
                '}';
    }
}
