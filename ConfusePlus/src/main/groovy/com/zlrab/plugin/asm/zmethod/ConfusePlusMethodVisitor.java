package com.zlrab.plugin.asm.zmethod;

import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.java.ZClass;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

/**
 * @author zlrab
 * @date 2020/12/29 15:07
 */
@Deprecated
public class ConfusePlusMethodVisitor extends MethodVisitor {
    private ZClass zClass;
    private Map<String, String> allSignModifyMap;

    public ConfusePlusMethodVisitor(int api, MethodVisitor methodVisitor, ZClass zClass, Map<String, String> allSignModifyMap) {
        super(api, methodVisitor);
        this.zClass = zClass;
        this.allSignModifyMap = allSignModifyMap;
    }

    /**
     * visitMethodInsn : opcode = 182	owner = java/lang/reflect/Method	name = invoke	descriptor = (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;	isInterface = false
     *
     * @param opcode
     * @param owner
     * @param name
     * @param descriptor
     * @param isInterface
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        String newOwner = allSignModifyMap.containsKey(owner) ? allSignModifyMap.get(owner) : owner;
        String newDescriptor = descriptor;
        for (String key : allSignModifyMap.keySet()) {
            String newKey = ConversionTool.signToDescriptor(key);
            if (newDescriptor != null && newDescriptor.contains(newKey)) {
                newDescriptor = newDescriptor.replace(newKey, ConversionTool.signToDescriptor(allSignModifyMap.get(key)));
                break;
            }
        }
        super.visitMethodInsn(opcode, newOwner, name, newDescriptor, isInterface);
    }

    /**
     * 类型强转指向的类型
     * example : String name = (String)  object  type指向的就是java/lang/String
     *
     * @param opcode
     * @param type
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
        String newType = allSignModifyMap.containsKey(type) ? allSignModifyMap.get(type) : type;
        super.visitTypeInsn(opcode, newType);
    }

    /**
     * 方法中访问的类变量
     *
     * @param opcode
     * @param owner
     * @param name
     * @param descriptor
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        String newOwner = allSignModifyMap.containsKey(owner) ? allSignModifyMap.get(owner) : owner;
        String newDescriptor = descriptor;
        for (String key : allSignModifyMap.keySet()) {
            if (newDescriptor != null && newDescriptor.equals(ConversionTool.signToDescriptor(key))) {
                newDescriptor = ConversionTool.signToDescriptor(allSignModifyMap.get(key));
                break;
            }
        }
        super.visitFieldInsn(opcode, newOwner, name, newDescriptor);
    }
    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String) {
            String newValue = (String) value;
            String sign = ConversionTool.classNameToSign(newValue);
            if (allSignModifyMap.containsKey(sign))
                value = ConversionTool.signToClassName(allSignModifyMap.get(sign));
        }
        super.visitLdcInsn(value);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        String newDescriptor = descriptor;
        for (String key : allSignModifyMap.keySet()) {
            if (newDescriptor != null && newDescriptor.equals(ConversionTool.signToDescriptor(key))) {
                newDescriptor = ConversionTool.signToDescriptor(allSignModifyMap.get(key));
                break;
            }
        }
        super.visitLocalVariable(name, newDescriptor, signature, start, end, index);
    }
}
