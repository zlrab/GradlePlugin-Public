package com.zlrab.plugin.work.confuse;

import org.dom4j.Attribute;
import org.dom4j.QName;

/**
 * @author zlrab
 * @date 2021/1/12 16:03
 */
public class AttributeRecord {
    public String newValue;
    public QName qName;

    public AttributeRecord(String newValue, QName qName) {
        this.newValue = newValue;
        this.qName = qName;
    }

    public AttributeRecord(QName qName) {
        this.qName = qName;
    }

}
