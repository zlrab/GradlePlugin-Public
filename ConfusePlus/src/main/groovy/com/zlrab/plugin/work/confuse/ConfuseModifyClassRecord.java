package com.zlrab.plugin.work.confuse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/30 21:12
 */
public class ConfuseModifyClassRecord {
    /**
     * example : com/zlrab/Demo
     */
    private String oldSign;
    /**
     * example : com/new/Proxy
     */
    private String newSign;
    /**
     * example :
     */
    private List<ConfuseModifyMethodRecord> methodRecordList = new ArrayList<>();

    private String extra;

    public ConfuseModifyClassRecord(String oldSign, String newSign) {
        this.oldSign = oldSign;
        this.newSign = newSign;
    }

    public ConfuseModifyClassRecord(String oldSign, String newSign, String extra) {
        this.oldSign = oldSign;
        this.newSign = newSign;
        this.extra = extra;
    }

    public ConfuseModifyClassRecord addMethodRecord(ConfuseModifyMethodRecord confuseModifyMethodRecord) {
        methodRecordList.add(confuseModifyMethodRecord);
        return this;
    }

    public String getOldSign() {
        return oldSign;
    }

    public String getNewSign() {
        return newSign;
    }

    public List<ConfuseModifyMethodRecord> getMethodRecordList() {
        return methodRecordList;
    }

    public String getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return "ConfuseModifyClassRecord{" +
                "oldSign='" + oldSign + '\'' +
                ", newSign='" + newSign + '\'' +
                ", methodRecordList=" + Arrays.toString(methodRecordList.toArray()) +
                ", extra='" + extra + '\'' +
                '}';
    }
}
