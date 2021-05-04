package com.zlrab.tool;


import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author zlrab
 * @date 2020/11/28 18:27
 */
public class Enhance {
    public static <T> void forEach(Iterable<T> tIterable, Consumer<? super T> action) {
        NullTool.ifNullThrowException(tIterable, "");
        for (T t : tIterable) {
            action.accept(t);
        }
    }

    public static <T> T findByElement(Iterable<T> tIterable, Find<T> find) {
        NullTool.ifNullThrowException(tIterable, "");
        for (T t : tIterable) {
            boolean accept = find.accept(t);
            if (accept) return t;
        }
        return null;
    }

    public static <T> T findByElement(T[] tIterable, Find<T> find) {
        NullTool.ifNullThrowException(tIterable, "");
        for (T t : tIterable) {
            boolean accept = find.accept(t);
            if (accept) return t;
        }
        return null;
    }

    public static <T> void forEach(T[] data, Consumer<? super T> action) {
        NullTool.ifNullThrowException(data, "");
        for (T t : data) {
            action.accept(t);
        }
    }

    public static <K, V> void forEach(Map<K, V> map, MapConsumer<K, V> consumer) {
        NullTool.ifNullThrowException(map, "");
        forEach(map.entrySet(), consumer::accept);
    }

    public static <T> List<T> filter(T[] data, Filter<? super T> filter) {
        NullTool.ifNullThrowException(data, "");
        List<T> tempDataList = new ArrayList<>();
        forEach(data, tempDataList::add);
        filter(tempDataList, filter);
        return tempDataList;
    }

    public static <T> List<T> filter(Collection<T> tIterable, Filter<? super T> filter) {
        NullTool.ifNullThrowException(tIterable, "");
        List<T> tempDataList = new ArrayList<>();
        forEach(tIterable, tempDataList::add);
        Iterator<T> iterator = tempDataList.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (!filter.accept(next)) iterator.remove();
        }
        return tempDataList;
    }

    public static <K, V> Map<K, V> mapFilter(Map<K, V> map, MapFilter<K, V> mapFilter) {
        NullTool.ifNullThrowException(map, "");
        Map<K, V> tempDataMap = new HashMap<>();
        forEach(map, entry -> tempDataMap.put(entry.getKey(), entry.getValue()));

        Iterator<K> iterator = tempDataMap.keySet().iterator();
        while (iterator.hasNext()) {
            K next = iterator.next();
            if (!mapFilter.accept(next, tempDataMap.get(next))) {
                iterator.remove();
                tempDataMap.remove(next);
            }
        }
        return tempDataMap;
    }

    /**
     * 根据指定的规则匹配字符串
     *
     * @param pattern
     * @param content
     * @return
     */
    public static boolean wildcardStarMatch(String pattern, String content) {
        int strLength = content.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0, patternLength = pattern.length(); patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                while (strIndex < strLength) {
                    if (wildcardStarMatch(pattern.substring(patternIndex + 1), content.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else {
                if ((strIndex >= strLength) || (ch != content.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }

    public static void recursiveAllAttr(Element rootElement, AttrConsumer attrConsumer) {
        recursiveAllAttr(rootElement, rootElement, attrConsumer);
    }

    private static void recursiveAllAttr(Element rootElement, Element currentElement, AttrConsumer attrConsumer) {
        currentElement.attributeIterator().forEachRemaining(attribute -> attrConsumer.accept(rootElement, currentElement, attribute));
        currentElement.elementIterator().forEachRemaining(element -> recursiveAllAttr(rootElement, element, attrConsumer));
    }

    /**
     * 递归获取element的Attribute迭代器 用于通过{@link Iterator#remove()}函数支持删除操作
     *
     * @param rootElement
     * @param attrIteratorConsumer
     */
    public static void recursiveAllAttrIterator(Element rootElement, AttrIteratorConsumer attrIteratorConsumer) {
        recursiveAllAttrIterator(rootElement, rootElement, attrIteratorConsumer);
    }

    private static void recursiveAllAttrIterator(Element rootElement, Element currentElement, AttrIteratorConsumer attrIteratorConsumer) {
        Iterator<Attribute> attributeIterator = currentElement.attributeIterator();
        attrIteratorConsumer.accept(rootElement, currentElement, attributeIterator);
        currentElement.elementIterator().forEachRemaining(element -> recursiveAllAttrIterator(rootElement, element, attrIteratorConsumer));
    }

    public interface AttrConsumer {
        void accept(Element rootElement, Element currentElement, Attribute attribute);
    }

    public interface AttrIteratorConsumer {
        void accept(Element rootElement, Element currentElement, Iterator<Attribute> attributeIterator);
    }

    public interface Consumer<T> {
        void accept(T t);
    }

    public interface MapConsumer<K, V> {
        void accept(Map.Entry<K, V> entry);
    }

    public interface Filter<T> {
        boolean accept(T t);
    }

    public interface MapFilter<K, V> {
        boolean accept(K key, V value);
    }

    public interface Find<T> {
        boolean accept(T t);
    }
}
