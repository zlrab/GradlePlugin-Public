package com.zlrab.plugin.asm.zclass;

import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.asm.zannotation.ConfusePlusAnnotationVisitor;
import com.zlrab.plugin.asm.zmethod.ConfusePlusMethodVisitor;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.java.ZMethod;
import com.zlrab.plugin.java.impl.ZFiledImpl;
import com.zlrab.plugin.java.impl.ZMethodImpl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

/**
 * @author zlrab
 * @date 2020/12/28 21:26
 */
@Deprecated
public class ConfusePlusClassVisitor extends ClassVisitor {
    private ZClass zClass;
    private Map<String, String> allSignModifyMap;
    private boolean removeSource;

    public ConfusePlusClassVisitor(int api, ClassVisitor classVisitor, ZClass zClass, Map<String, String> allSignModifyMap, boolean removeSource) {
        super(api, classVisitor);
        this.zClass = zClass;
        this.allSignModifyMap = allSignModifyMap;
        this.removeSource = removeSource;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        zClass.setClassName(name)
                .setSuperClassName(superName);
        String newName = allSignModifyMap.containsKey(name) ? allSignModifyMap.get(name) : name;
        String newSuperName = allSignModifyMap.containsKey(superName) ? allSignModifyMap.get(superName) : superName;
        super.visit(version, access, newName, signature, newSuperName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        ZField zFiled = new ZFiledImpl(access, name, descriptor, signature, value);
        zClass.fieldList().add(zFiled);
        String newDescriptor = allSignModifyMap.containsKey(ConversionTool.signToDescriptor(descriptor)) ? ConversionTool.signToDescriptor(descriptor) : descriptor;
        return super.visitField(access, name, newDescriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        ZMethod zMethod = new ZMethodImpl(access, name, descriptor, signature, exceptions);
        zClass.methodList().add(zMethod);
        String newDescriptor = descriptor;
        String newSignature = signature;
        for (String key : allSignModifyMap.keySet()) {
            if (newDescriptor != null && descriptor.contains(ConversionTool.signToDescriptor(key))) {
                newDescriptor = newDescriptor.replace(ConversionTool.signToDescriptor(key), ConversionTool.signToDescriptor(allSignModifyMap.get(key)));
            }
            if (signature != null && signature.contains(ConversionTool.signToSignature(key))) {
                newSignature = newSignature.replace(ConversionTool.signToSignature(key), ConversionTool.signToSignature(allSignModifyMap.get(key)));
            }
        }
        MethodVisitor methodVisitor = cv.visitMethod(access, name, newDescriptor, newSignature, exceptions);
        return new ConfusePlusMethodVisitor(api, methodVisitor, zClass, allSignModifyMap);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(removeSource ? "" : source, debug);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String newDescriptor = descriptor;
        for (String key : allSignModifyMap.keySet()) {
            if (descriptor != null && descriptor.equals(ConversionTool.signToDescriptor(key))) {
                newDescriptor = ConversionTool.signToDescriptor(allSignModifyMap.get(key));
                break;
            }
        }
        AnnotationVisitor annotationVisitor = cv.visitAnnotation(newDescriptor, visible);
        return new ConfusePlusAnnotationVisitor(api, annotationVisitor, zClass);
    }

}
