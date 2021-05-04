package com.zlrab.plugin.asm.zclass;


import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.zmethod.EncryptionStringMethodVisitor;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.java.ZMethod;
import com.zlrab.plugin.java.impl.ZFiledImpl;
import com.zlrab.plugin.java.impl.ZMethodImpl;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;


/**
 * @author zlrab
 * @date 2020/12/24 18:10
 */
@Deprecated
public class EncryptedStringClassVisitor extends ClassVisitor {
    private ZClass zClass;

    public EncryptedStringClassVisitor(int api, ClassWriter classWriter, ZClass zClass) {
        super(api, classWriter);
        this.zClass = zClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        zClass.setClassName(name)
                .setSuperClassName(superName);
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
            return super.visitField(access, name, descriptor, signature, null);
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        ZMethod zMethod = new ZMethodImpl(access, name, descriptor, signature, exceptions);
        zClass.methodList().add(zMethod);
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        return new EncryptionStringMethodVisitor(api, methodVisitor, zClass, "<clinit>".equals(name));
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
