package com.zlrab.tool;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zlrab
 * @date 2020/12/28 17:23
 */
public class RandomTool {
    private static Random random = new Random();

    private static List<String> stringList = new ArrayList<>();

    public static String[] randomPackageClassNames(int count) {
        String[] strings = new String[count];
        for (int index = 0; index < count; index++) {
            strings[index] = randomPackageClassName();
        }
        return strings;
    }

    /**
     * 随机生成类包名+类名
     *
     * @return example : com.zlrab.demo.Main
     */
    public static String randomPackageClassName() {
        return randomPackageClassName(randomRangeNumber(4, 6));
    }

    /**
     * 指定小段个数随机生成类包名+类名
     *
     * @param count 以.为分割的小段个数
     * @return example : com.zlrab.demo.Main
     */
    public static String randomPackageClassName(int count) {
        return randomPackageClassName(count, 5, 8);
    }

    /**
     * 指定小段个数随机生成类包名+类名
     *
     * @param count             以.为分割的小段个数
     * @param shortSectionStart 小段最小长度(包含)
     * @param shortSectionEnd   小段的最大长度(不包含)
     * @return example : com.zlrab.demo.Main
     */
    public static String randomPackageClassName(int count, int shortSectionStart, int shortSectionEnd) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            stringBuilder.append(randomName(shortSectionStart, shortSectionEnd)).append('.');
        }
        String s = stringBuilder.toString();
        s = s.substring(0, s.length() - 1);
        if (stringList.contains(s))
            return randomPackageClassName(count, shortSectionStart, shortSectionEnd);
        stringList.add(s);
        return s;
    }

    public static String randomName(int start, int end) {
        int length = randomRangeNumber(start, end);
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String randomName() {
        return randomName(5, 8);
    }

    /**
     * 随机生成数字
     *
     * @param start 随机起始数（包含）
     * @param end   随机最大数（不包含）
     * @return [ start , end )
     */
    public static int randomRangeNumber(int start, int end) {
        return random.nextInt(end) + start;
    }

    public static int randomRangeNumber(int end) {
        return randomRangeNumber(0, end);
    }
}
