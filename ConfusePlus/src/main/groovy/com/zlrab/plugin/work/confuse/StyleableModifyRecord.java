package com.zlrab.plugin.work.confuse;

import com.zlrab.core.ResType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zlrab
 * @date 2021/1/11 20:09
 */
public class StyleableModifyRecord extends ResModifyRecord {
    private List<StyleableEnum> styleableEnumList = new ArrayList<>();

    public StyleableModifyRecord(ResType resType, String oldName, String newName, File resFile) {
        super(resType, oldName, newName, resFile, false);
    }

    public StyleableModifyRecord addStyleableEnum(StyleableEnum styleableEnum) {
        this.styleableEnumList.add(styleableEnum);
        return this;
    }

    public List<StyleableEnum> getStyleableEnumList() {
        return styleableEnumList;
    }

    public static class StyleableEnum {
        private String oldName;
        private String newName;
        private String parentAttrName;

        public StyleableEnum(String oldName, String newName, String parentAttrName) {
            this.oldName = oldName;
            this.newName = newName;
            this.parentAttrName = parentAttrName;
        }

        public String getParentAttrName() {
            return parentAttrName;
        }

        public String getOldName() {
            return oldName;
        }

        public String getNewName() {
            return newName;
        }

        @Override
        public String toString() {
            return "StyleableEnum{" +
                    "oldName='" + oldName + '\'' +
                    ", newName='" + newName + '\'' +
                    ", parentAttrName='" + parentAttrName + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "StyleableModifyRecord{" +
                "styleableEnumList=" + Arrays.toString(styleableEnumList.toArray()) +
                "super=" + super.toString() +
                '}';
    }
}
