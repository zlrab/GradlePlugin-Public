package com.zlrab.plugin.java;

import java.io.File;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/24 18:00
 */
public interface ZClass {
    /**
     * 当前class所有的字段
     *
     * @return
     */
    List<ZField> fieldList();

    /**
     * 当前class所有方法
     *
     * @return
     */
    List<ZMethod> methodList();

    /**
     * 当前class的类名 不包含拓展名 , 比如:Demo , 不是Demo.java或者Demo.class或者Demo.smali
     *
     * @return
     */
    String classFileName();

    /**
     * 当前类相对路径
     * example : com.zlrab.Demo
     *
     * @return
     */
    String getClassName();

    /**
     * 设置类相对路径
     * example : com.zlrab.Demo
     *
     * @return
     */
    ZClass setClassName(String className);

    String getSuperClassName();

    ZClass setSuperClassName(String superClassName);

    /**
     * 获取关联的文件
     *
     * @return
     */
    File getFile();
}
