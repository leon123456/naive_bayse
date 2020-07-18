package com.kuaishou.old.antispam.classification;

// CHECKSTYLE:OFF
public class SpecialPinyinHander {

    public final static String PORN = "porn";

    public static String[] pornSpecialPinyinList = { "dao", "guo", "huang", "pian" };

    public static boolean inSpecialList(String pinyin, String s_class) {
        if (PORN.equals(s_class)) {
            for (String py : pornSpecialPinyinList) {
                if (pinyin.contains(py)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(inSpecialList("guo2", PORN));
    }

}
// CHECKSTYLE:ON
