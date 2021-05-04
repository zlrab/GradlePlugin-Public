package com.zlrab.core;

import com.zlrab.plugin.work.confuse.ResConfusePlusManager;

/**
 * @author zlrab
 * @date 2021/1/8 16:25
 */
public enum ResType {
    anim("anim"),
    animator("animator"),
    color("color"),
    drawable("drawable"),
    font("font"),
    layout("layout"),
    menu("menu"),
    mipmap("mipmap"),
    navigation("navigation"),
    raw("raw"),
    transition("transition"),
    xml("xml"),
    string("string"),
    styleable("styleable"),
    style("style"),
    bool("bool"),
    integer("integer"),
    dimen("dimen"),
    id("id");

    ResType(String rName) {
        this.rName = rName;
    }

    private String rName;


    public String getRName() {
        return rName;
    }

    /**
     * @param name app_name
     * @return R.string.app_name
     */
    public String buildResReferrer(String name) {
        return "R." + rName + "." + name;
    }

    /**
     * @param name app_name
     * @return @string/app_name
     */
    public String buildXmlReferrer(String name) {
        return "@" + rName + "/" + name;
    }

    /**
     * @param xmlReferrer @string/app_name
     * @return app_name
     */
    public String cropNameFromXmlReferrer(String xmlReferrer) {
        return xmlReferrer.substring(("@" + rName + "/").length());
    }

    public static ResType positionResType(String rName) {
        ResType[] values = ResType.values();
        for (ResType resType : values) {
            if (resType.getRName().equals(rName)) {
                return resType;
            }
        }
        return null;
    }

    /**
     * owner = lib/R$styleable	name = TestView
     *
     * @param fieldSign lib/R$styleable
     * @return {@link ResType}
     */
    public static ResType positionResTypeAccordingToClassSign(String fieldSign) {
        String baseSign = ResConfusePlusManager.getInstance().getPackageName() + "/R$";
        ResType[] values = ResType.values();
        for (ResType resType : values) {
            if ((baseSign + resType.getRName()).equals(fieldSign)) return resType;
        }
        return null;
    }
}
