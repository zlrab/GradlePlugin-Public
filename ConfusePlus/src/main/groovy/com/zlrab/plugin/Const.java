package com.zlrab.plugin;

import org.objectweb.asm.Opcodes;

/**
 * @author zlrab
 * @date 2020/12/25 14:00
 */
public class Const {
    /**
     * 指示当前zField需要做加密操作
     */
    public static final String EXTRA_ZFIELD_ENCRYPTION_WORK = "extra_zfield_encryption_work";
    /**
     * 指示当前zField需要赋值赋值
     */
    public static final String EXTRA_ZFILED_ASSIGNMENT_WORK = "extra_zfield_assignment_work";

    public static final String BYTE_ARRAY_SIGN = "[B";
    /**
     * 解密函数的参数签名
     */
    public static final String DECODE_METHOD_PARAMS_SIGN = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";

    public static final String nativeClassName = "com.timing.leoric.NativeLeoric";
    /**
     * string签名
     */
    public static final String STRING_SIGN = "Ljava/lang/String;";
    /**
     * 全局ASM版本
     */
    public static final int ASM_API = Opcodes.ASM9;

    public static String WORK_BUNDLES_DIR_NAME = "workBundles";

    public static String WORK_ROOT_DIR_NAME = "ZLRab";

    public static final String ENCRYPTED_STRING_AND_PROGUARD_PLUS_TASK_NAME = "sdkEncryptedStringAndProguardPlus";

    public static final String INIT_TASK_NAME = "sdkPluginInit";

    public static final String ENCRYPTED_STRING_TASK_NAME = "sdkEncryptedString";

    public static final String PROGUARD_PLUS_TASK_NAME = "sdkProguardPlus";

    public static final String CHECK_ENCRYPTION_STRING_DATA = "ZLRab-check";

    public static final String CUSTOM_VIEW_NAMESPACE = "http://schemas.android.com/apk/res-auto";

    public static final String BYTE_KEY_NAME = "DATA";

    public static final int ASCII_MIN = 32;
    public static final int ASCII_MAX = 126;
}
