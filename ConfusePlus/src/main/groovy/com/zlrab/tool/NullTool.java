package com.zlrab.tool;


/**
 * @author zlrab
 * @date 2020/11/23 17:46
 */
public class NullTool {
    public static <T> T ifNullThrowException(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}
