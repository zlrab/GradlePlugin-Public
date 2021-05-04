package com.zlrab.plugin.asm.zmethod;

import com.zlrab.core.ResType;
import com.zlrab.plugin.Const;
import com.zlrab.plugin.asm.ConversionTool;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.proxy.BaseConfuseStringClassProxy;
import com.zlrab.plugin.proxy.EncryptionStringClassProxy;
import com.zlrab.plugin.work.EncryptionStringClassManager;
import com.zlrab.plugin.work.confuse.ConfuseModifyClassRecord;
import com.zlrab.plugin.work.confuse.ConfuseModifyMethodRecord;
import com.zlrab.plugin.work.confuse.ConfusePlusManager;
import com.zlrab.plugin.work.confuse.ResConfusePlusManager;
import com.zlrab.plugin.work.confuse.ResModifyRecord;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zlrab
 * @date 2021/1/4 20:09
 */
public class ConfuseMethodVisitor extends MethodVisitor implements Opcodes {
    private ZClass zClass;
    private boolean isInitMethod;

    private boolean debugGetClassNameTest = false;

    public ConfuseMethodVisitor(int api, MethodVisitor methodVisitor, ZClass zClass) {
        this(api, methodVisitor, zClass, false);
    }

    public ConfuseMethodVisitor(int api, MethodVisitor methodVisitor, ZClass zClass, boolean isInitMethod) {
        super(api, methodVisitor);
        this.zClass = zClass;
        this.isInitMethod = isInitMethod;
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String && ConfusePlusManager.getInstance().autoEncryptedString()) {
            String data = (String) value;
            //处理反射调用className
            String classSign = ConversionTool.classNameToSign(data);
            ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(classSign);
            if (confuseModifyClassRecord != null) {
                data = ConversionTool.signToClassName(confuseModifyClassRecord.getNewSign());
            }
            //开始加密字符串
            BaseConfuseStringClassProxy encryptionProxy = EncryptionStringClassManager.getInstance().getRandomProxy();
            if (encryptionProxy!=null){
                String encode = encryptionProxy.encode(data);
                mv.visitLdcInsn(encode);
                mv.visitLdcInsn(encryptionProxy.getKey());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        encryptionProxy.getConfuseDecodeClassName().replace(".", "/"),
                        encryptionProxy.getConfuseDecodeMethodName(),
                        Const.DECODE_METHOD_PARAMS_SIGN, false);
                return;
            }
        } else if (value instanceof Type) {
            Type type = (Type) value;
            ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(ConversionTool.descriptorToSign(type.getDescriptor()));
            if (confuseModifyClassRecord != null) {
                value = Type.getType(ConversionTool.signToDescriptor(confuseModifyClassRecord.getNewSign()));
            }
        }
        super.visitLdcInsn(value);
    }

    @Override
    public void visitInsn(int opcode) {
        if (ConfusePlusManager.getInstance().autoEncryptedString()) {
            List<ZField>
                    filter = Enhance.filter(zClass.fieldList(), zField -> zField.getBooleanExtra(Const.EXTRA_ZFIELD_ENCRYPTION_WORK));
            if (filter.size() > 0 && isInitMethod) {
                if (opcode == Opcodes.RETURN) {
                    Enhance.forEach(filter, zField -> {
                        BaseConfuseStringClassProxy encryptionProxy = EncryptionStringClassManager.getInstance().getRandomProxy();
                        if (encryptionProxy!=null){
                            //加密原始串
                            String encode = encryptionProxy.encode((String) zField.getValue());
                            LogTool.w("encode = " + encode + "\tdecode = " + zField.getValue() + "\t key = " + encryptionProxy.getKey());
                            mv.visitLdcInsn(encode);
                            mv.visitLdcInsn(encryptionProxy.getKey());
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, encryptionProxy.getConfuseDecodeClassName().replace(".", "/"),
                                    encryptionProxy.getConfuseDecodeMethodName(), Const.DECODE_METHOD_PARAMS_SIGN, false);
                            ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(zClass.getClassName());
                            mv.visitFieldInsn(Opcodes.PUTSTATIC, confuseModifyClassRecord==null?zClass.getClassName():confuseModifyClassRecord.getNewSign(), zField.getName(), Const.STRING_SIGN);
                        }
                    });
                }
            }
        }
        BaseConfuseStringClassProxy encryptionStringClassProxy = EncryptionStringClassManager.getInstance().needModifyByteArrayKey(zClass);
        if (encryptionStringClassProxy != null && opcode == Opcodes.ACONST_NULL) {
            ZField byElement = Enhance.findByElement(zClass.fieldList(), zField -> zField.hasExtra(Const.EXTRA_ZFILED_ASSIGNMENT_WORK)
                    && (boolean) zField.getExtra(Const.EXTRA_ZFILED_ASSIGNMENT_WORK));
            if (byElement != null) {
                byte[] randomByteArrayKey = encryptionStringClassProxy.getRandomByteArrayKey();
                int length = randomByteArrayKey.length;
                if (length > 5) {
                    mv.visitIntInsn(BIPUSH, length);
                    mv.visitIntInsn(NEWARRAY, T_BYTE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_0);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[0]);
                    mv.visitInsn(BASTORE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_1);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[1]);
                    mv.visitInsn(BASTORE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_2);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[2]);
                    mv.visitInsn(BASTORE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_3);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[3]);
                    mv.visitInsn(BASTORE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_4);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[4]);
                    mv.visitInsn(BASTORE);

                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_5);
                    mv.visitIntInsn(BIPUSH, randomByteArrayKey[5]);
                    mv.visitInsn(BASTORE);

                    for (int index = 6; index < length; index++) {
                        mv.visitInsn(DUP);
                        mv.visitIntInsn(BIPUSH, index);
                        mv.visitIntInsn(BIPUSH, randomByteArrayKey[index]);
                        mv.visitInsn(BASTORE);
                    }
                    return;
                }
            }

        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        String newDescriptor = descriptor;
        ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToShortDescReadRecord(descriptor);
        if (confuseModifyClassRecord != null) {
            newDescriptor = ConversionTool.signToDescriptor(confuseModifyClassRecord.getNewSign());
        }
        super.visitLocalVariable(name, newDescriptor, signature, start, end, index);
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
        ConfuseModifyClassRecord ownerRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(owner);
        String newOwner = ownerRecord != null ? ownerRecord.getNewSign() : owner;
        String newDescriptor = descriptor;
        ConfuseModifyClassRecord descRecord = ConfusePlusManager.getInstance().accordingToShortDescReadRecord(descriptor);
        if (descRecord != null) {
            newDescriptor = ConversionTool.signToDescriptor(descRecord.getNewSign());
        }
        String newName = name;
        ResType resType = ResType.positionResTypeAccordingToClassSign(owner);
        if (resType != null) {
            ResModifyRecord position = ResConfusePlusManager.getInstance().position(resType, name);
            if (position != null) {
                newName = position.getNewName();
            }
        }
        super.visitFieldInsn(opcode, newOwner, newName, newDescriptor);
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
        ConfuseModifyClassRecord confuseModifyClassRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(type);
        String newType = confuseModifyClassRecord != null ? confuseModifyClassRecord.getNewSign() : type;
        super.visitTypeInsn(opcode, newType);
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
        String newMethodName = name;
        ConfuseModifyMethodRecord confuseModifyMethodRecord = ConfusePlusManager.getInstance().determineMethodOperationModeIgnoreAccess(owner, name, descriptor);
        if (confuseModifyMethodRecord != null) {
            newMethodName = confuseModifyMethodRecord.getNewName();
        }
        ConfuseModifyClassRecord ownerRecord = ConfusePlusManager.getInstance().accordingToSignReadRecord(owner);
        String newOwner = ownerRecord != null ? ownerRecord.getNewSign() : owner;
        final AtomicReference<String> newDescriptorRef = new AtomicReference<>(descriptor);
        Collection<ConfuseModifyClassRecord> descRecords = ConfusePlusManager.getInstance().accordingToLongDescReadRecord(descriptor);
        if (descRecords != null && descRecords.size() > 0) {
            Enhance.forEach(descRecords, confuseModifyClassRecord ->
                    newDescriptorRef.set(
                            newDescriptorRef.get().replace(
                                    ConversionTool.signToDescriptor(confuseModifyClassRecord.getOldSign()),
                                    ConversionTool.signToDescriptor(confuseModifyClassRecord.getNewSign())
                            )
                    ));
        }
        super.visitMethodInsn(opcode, newOwner, newMethodName, newDescriptorRef.get(), isInterface);
    }

}
