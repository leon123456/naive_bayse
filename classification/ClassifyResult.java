package com.kuaishou.old.antispam.classification;

/**
 * 分类结果.
 */
// CHECKSTYLE:OFF
public class ClassifyResult {

    /** 分类的概率. */
    public double probility;
    /** 类别名. */
    public String classification;

    /** 构造函数. */
    public ClassifyResult() {
        probility = 0;
        classification = null;
    }
}
// CHECKSTYLE:ON
