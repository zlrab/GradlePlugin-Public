package com.zlrab.plugin.asm.zclass;

import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.asm.zmethod.ConfuseMethodVisitor;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.java.ZMethod;
import com.zlrab.plugin.java.impl.ZFiledImpl;
import com.zlrab.plugin.java.impl.ZMethodImpl;
import com.zlrab.plugin.proxy.BaseConfuseStringClassProxy;
import com.zlrab.plugin.proxy.EncryptionStringClassProxy;
import com.zlrab.plugin.work.EncryptionStringClassManager;
import com.zlrab.plugin.work.confuse.ConfuseModifyClassRecord;
import com.zlrab.plugin.work.confuse.ConfuseModifyMethodRecord;
import com.zlrab.plugin.work.confuse.ConfusePlusManager;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;

import org.gradle.internal.impldep.org.mortbay.jetty.deployer.ConfigurationManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zlrab
 * @date 2020/12/31 18:10
 */
public class ConfusePlusVisitor extends ClassVisitor {
    private ZClass zClass;

    private ConfuseModifyClassRecord confuseModifyClassRecord;

    public ConfusePlusVisitor(int api, ClassVisitor classVisitor, ZClass zClass) {
        super(api, classVisitor);
        this.zClass = zClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        zClass.setClassName(name)
                .setSuperClassName(superName);
        confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(name);

        String newName = confuseModifyClassRecord != null ? confuseModifyClassRecord.getNewSign() : name;
        ConfuseModifyClassRecord superClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(superName);
        String newSuperName = superClassRecord != null ? superClassRecord.getNewSign() : superName;
        super.visit(version, access, newName, signature, newSuperName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String newName = name;
        String newOuterName = outerName;
        String newInnerName = innerName;
        if (name != null && name.length() > 0) {
            ConfuseModifyClassRecord nameClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(name);
            newName = nameClassRecord != null ? nameClassRecord.getNewSign() : name;
        }

        if (newOuterName != null && newOuterName.length() > 0) {
            ConfuseModifyClassRecord outerNameClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(outerName);
            newOuterName = outerNameClassRecord != null ? outerNameClassRecord.getNewSign() : outerName;
        }

        if (newInnerName != null && newInnerName.length() > 0) {
            ConfuseModifyClassRecord innerNameClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(innerName);
            newInnerName = innerNameClassRecord != null ? innerNameClassRecord.getNewSign() : innerName;
        }

        super.visitInnerClass(newName, newOuterName, newInnerName, access);
    }

    /**
     * 字段初始化规则
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |                |   普通类成员   |  static类成员  |                       static final类成员                                   |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |                                                | 基本类型&String直接赋值    |  object类型直接赋值& 基本类型&String调用函数赋值    |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |     class      |    <init>     |    <clinit>   |        字段上直接赋值      |                 <clinit>                       |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * | abstract class |    <init>     |    <clinit>   |        字段上直接赋值      |                 <clinit>                       |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |     enum       |    <init>     |    <clinit>   |        字段上直接赋值      |                 <clinit>                       |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |   interface    |            无                 |        字段上直接赋值      |                  <clinit>                      |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     * |   @interface   |            无                 |        字段上直接赋值      |                  <clinit>                      |
     * |————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————|
     *
     * @param access
     * @param name
     * @param descriptor
     * @param signature
     * @param value
     * @return
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        ZField zFiled = new ZFiledImpl(access, name, descriptor, signature, value);
        zClass.fieldList().add(zFiled);
        if (value instanceof String) {//字段上有值且类型为string,说明这是字段被static final修饰，后续需要在<clinit>插入加密操作
            zFiled.putExtra(Const.EXTRA_ZFIELD_ENCRYPTION_WORK, true);
            value = null;
        }

        BaseConfuseStringClassProxy encryptionStringClassProxy = EncryptionStringClassManager.getInstance().needModifyByteArrayKey(zClass);
        if (encryptionStringClassProxy != null
                && name != null
                && name.equals(encryptionStringClassProxy.getByteArrayKeyName())) {
            zFiled.putExtra(Const.EXTRA_ZFILED_ASSIGNMENT_WORK, true);
        }

        ConfuseModifyClassRecord fieldTypeRecord = ConfusePlusManager.getInstance().accordingToShortDescReadRecord(descriptor);

        String filedNewDescriptor = fieldTypeRecord != null ? ConversionTool.signToDescriptor(fieldTypeRecord.getNewSign()) : descriptor;
        return super.visitField(access, name, filedNewDescriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        ZMethod zMethod = new ZMethodImpl(access, name, descriptor, signature, exceptions);
        zClass.methodList().add(zMethod);
        String newName = name;
        //确定是否要删除这个方法
        if (confuseModifyClassRecord != null) {
            ConfuseModifyMethodRecord confuseModifyMethodRecord = ConfusePlusManager.getInstance().determineMethodOperationMode(confuseModifyClassRecord.getOldSign(), access, name, descriptor);
            if (confuseModifyMethodRecord != null) {
                if (confuseModifyMethodRecord.isRemove()) {
                    return null;
                }
                newName = confuseModifyMethodRecord.getNewName();
            }
        }

        final AtomicReference<String> newDescriptorRef = new AtomicReference<>(descriptor);
        Collection<ConfuseModifyClassRecord> descRecords = ConfusePlusManager.getInstance().accordingToLongDescReadRecord(newDescriptorRef.get());
        if (descRecords != null && descRecords.size() > 0) {
            Enhance.forEach(descRecords, confuseModifyClassRecord ->
                    newDescriptorRef.set(
                            newDescriptorRef.get().replace(
                                    ConversionTool.signToDescriptor(confuseModifyClassRecord.getOldSign()),
                                    ConversionTool.signToDescriptor(confuseModifyClassRecord.getNewSign())
                            )
                    ));
        }
        final AtomicReference<String> newSignatureRef = new AtomicReference<>(signature);
        Collection<ConfuseModifyClassRecord> signatureRecords = ConfusePlusManager.getInstance().accordingToLongSignatureReadRecord(newSignatureRef.get());
        if (signatureRecords != null && signatureRecords.size() > 0) {
            Enhance.forEach(signatureRecords, confuseModifyClassRecord -> newSignatureRef.set(
                    newSignatureRef.get().replace(
                            ConversionTool.signToSignature(confuseModifyClassRecord.getOldSign()),
                            ConversionTool.signToSignature(confuseModifyClassRecord.getNewSign())
                    )
            ));
        }

        MethodVisitor methodVisitor = cv.visitMethod(access, newName, newDescriptorRef.get(), newSignatureRef.get(), exceptions);
        return new ConfuseMethodVisitor(api, methodVisitor, zClass, "<clinit>".equals(name));
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(ConfusePlusManager.getInstance().autoRemoveClassSource() ? "" : source, debug);
    }
}
