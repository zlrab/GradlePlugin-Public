package com.zlrab.plugin.asm.zannotation;

import com.zlrab.plugin.java.ZClass;

import org.objectweb.asm.AnnotationVisitor;

/**
 * @author zlrab
 * @date 2020/12/29 15:29
 */
public class ConfusePlusAnnotationVisitor extends AnnotationVisitor {
    private ZClass zClass;

    public ConfusePlusAnnotationVisitor(int api, AnnotationVisitor annotationVisitor, ZClass zClass) {
        super(api, annotationVisitor);
        this.zClass = zClass;
    }

    @Override
    public void visit(String name, Object value) {
//        LogTool.e("class = [ " + zClass.getClassName() + " ]" +
//                "\tConfusePlusAnnotationVisitor visit : name = " + name +
//                "\tvalue = " + value);
        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
//        LogTool.e("class = [ " + zClass.getClassName() + " ]" +
//                "\tConfusePlusAnnotationVisitor visitEnum : name = " + name +
//                "\tdescriptor = " + descriptor +
//                "\tvalue = " + value);
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
//        LogTool.e("class = [ " + zClass.getClassName() + " ]" +
//                "\tConfusePlusAnnotationVisitor visitAnnotation : name = " + name +
//                "\tdescriptor = " + descriptor);
        return super.visitAnnotation(name, descriptor);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
//        LogTool.e("class = [ " + zClass.getClassName() + " ]" +
//                "\tConfusePlusAnnotationVisitor visitArray : name = " + name);
        return super.visitArray(name);
    }

}
