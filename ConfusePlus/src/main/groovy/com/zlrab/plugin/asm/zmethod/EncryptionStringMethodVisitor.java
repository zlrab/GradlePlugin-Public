package com.zlrab.plugin.asm.zmethod;

import com.zlrab.plugin.Const;
import com.zlrab.plugin.java.ZClass;
import com.zlrab.plugin.java.ZField;
import com.zlrab.plugin.proxy.BaseConfuseStringClassProxy;
import com.zlrab.plugin.proxy.EncryptionStringClassProxy;
import com.zlrab.plugin.work.EncryptionStringClassManager;
import com.zlrab.plugin.work.confuse.ConfusePlusManager;
import com.zlrab.tool.Enhance;
import com.zlrab.tool.LogTool;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/25 14:18
 */
@Deprecated
public class EncryptionStringMethodVisitor extends MethodVisitor {

    private MethodVisitor methodVisitor;

    private ZClass zClass;

    private boolean isInitMethod;

    public EncryptionStringMethodVisitor(int api, MethodVisitor methodVisitor, ZClass zClass, boolean isInitMethod) {
        super(api, methodVisitor);
        this.methodVisitor = methodVisitor;
        this.zClass = zClass;
        this.isInitMethod = isInitMethod;
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String && ConfusePlusManager.getInstance().autoEncryptedString()) {
            String data = (String) value;
            BaseConfuseStringClassProxy encryptionProxy = EncryptionStringClassManager.getInstance().getRandomProxy();
            if (encryptionProxy != null) {
                //加密原始串
                String encode = encryptionProxy.encode(data);
                mv.visitLdcInsn(encode);
                mv.visitLdcInsn(encryptionProxy.getKey());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        encryptionProxy.getConfuseDecodeClassName().replace(".", "/"),
                        encryptionProxy.getConfuseDecodeMethodName(),
                        Const.DECODE_METHOD_PARAMS_SIGN, false);
                return;
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
                        if (encryptionProxy != null) {
                            //加密原始串
                            String encode = encryptionProxy.encode((String) zField.getValue());
                            LogTool.w("encode = " + encode + "\tdecode = " + zField.getValue() + "\t key = " + encryptionProxy.getKey());
                            mv.visitLdcInsn(encode);
                            mv.visitLdcInsn(encryptionProxy.getKey());
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, encryptionProxy.getConfuseDecodeClassName().replace(".", "/"),
                                    encryptionProxy.getConfuseDecodeMethodName(), Const.DECODE_METHOD_PARAMS_SIGN, false);
                            mv.visitFieldInsn(Opcodes.PUTSTATIC, zClass.getClassName(), zField.getName(), Const.STRING_SIGN);
                        }
                    });
                }
            }
        }
        super.visitInsn(opcode);
    }
}
