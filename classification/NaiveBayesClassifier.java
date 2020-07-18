package com.kuaishou.old.antispam.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuaishou.mservice.service.MParamsConfigService;
import com.kuaishou.old.mmodel.ParamsConfigModel;
import com.kuaishou.old.split.ChineseSpliter;
import com.kuaishou.old.split.ChineseSpliterSmart;
import com.kuaishou.old.util.StringEscapeUtil;

/**
 * 朴素贝叶斯分类器.
*/
// CHECKSTYLE:OFF
public class NaiveBayesClassifier {

    private static Logger logger = LoggerFactory.getLogger(NaiveBayesClassifier.class);
    public TrainnedModel model;

    protected transient IntermediateData db;

    /** 中文分词. */

    public NaiveBayesClassifier() {
    }

    /**
     * 加载数据模型文件.
     *
     * @param modelFile
     *            模型文件路径
     */
    public final void loadModel(final String modelFile) {
        try {
            File file = new File(modelFile);
            if (file.exists()) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile))) {
                    model = (TrainnedModel) in.readObject();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                logger.error("LoadModel failed. fileNotExist: " + modelFile);
            }
        } catch (Exception e) {
            logger.error("LoadModel exception. file: " + modelFile, e);
        }
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文本
     * @return 分类结果
     */
    public final List<ClassifyResult> getClassifyProbility(final String text) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliter.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类
        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }

        double probility;
        double sum_probility = 0.0;

        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            probility = calcProbability(terms, i);

            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i];
            cr.probility = probility;
            crs.add(cr);

            sum_probility += probility;
        }
        if (sum_probility != 0) {
            for (ClassifyResult cr : crs) {
                cr.probility = cr.probility / sum_probility;
            }
        }

        return crs;
    }

    public final List<ClassifyResult> getClassifyProbility(final String text, String flag) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);
        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }

        double probility;
        double sum_probility = 0.0;

        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            probility = calcProbability(terms, i);

            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i];
            cr.probility = probility;
            crs.add(cr);

            sum_probility += probility;
        }
        if (sum_probility != 0) {
            for (ClassifyResult cr : crs) {
                cr.probility = cr.probility / sum_probility;
            }
        }

        return crs;
    }

    /**
     * 对给定的文本计算概率.
     *
     * @param text
     *            给定的文本
     * @return 结果列表
     */
    public final List<ClassifyResult> getTwoTermsClassifyProbility(final String text) {

        //    	String[] terms = null;
        //        // 中文分词处理(分词后结果可能还包含有停用词）
        //        terms = ChineseSpliter.split(text, " ").split(" ");
        //        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类
        //        Set<String> match_terms = new HashSet<String>();
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				match_terms.add(term);
        //			}
        //		}

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词�?
        String text1 = StringEscapeUtil.filterByRegEx(text);
        terms = ChineseSpliterSmart.split(text1, " ").split(" ");
        for (String string : terms) {
            //        	System.out.println(" before drop stopword" + string);
        }
        //        System.err.println("------------");
        //        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类
        for (String string : terms) {
            //			System.out.println(" after drop stopword " + string);
        }
        if (terms.length < 2) {
            return null;
        }
        for (int i = 0; i < terms.length - 1; i++) {
            terms[i] = terms[i] + terms[i + 1];
        }
        terms[terms.length - 1] = "";

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        String[] v_terms = new String[match_terms.size()];
        int count = 0;
        for (String term : match_terms) {
            v_terms[count] = term;
            count++;
        }

        double probility;
        double sum_probility = 0.0;

        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            probility = calcProbability(v_terms, i);

            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i];
            cr.probility = probility;
            crs.add(cr);

            sum_probility += probility;
        }
        for (ClassifyResult cr : crs) {
            cr.probility = cr.probility / sum_probility;
        }

        return crs;
    }

    public final List<ClassifyResult> getTwoTermsClassifyProbility(final String text, String flag) {

        //    	String[] terms = null;
        //        // 中文分词处理(分词后结果可能还包含有停用词）
        //        terms = ChineseSpliter.split(text, " ").split(" ");
        //        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类
        //        Set<String> match_terms = new HashSet<String>();
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				match_terms.add(term);
        //			}
        //		}

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词�?

        terms = StringEscapeUtil.getTermsbyFilter(text, flag);
        if (terms.length < 2) {
            return null;
        }
        for (int i = 0; i < terms.length - 1; i++) {
            terms[i] = terms[i] + terms[i + 1];
        }
        terms[terms.length - 1] = "";

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        String[] v_terms = new String[match_terms.size()];
        int count = 0;
        for (String term : match_terms) {
            v_terms[count] = term;
            count++;
        }

        double probility;
        double sum_probility = 0.0;

        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            probility = calcProbability(v_terms, i);

            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i];
            cr.probility = probility;
            crs.add(cr);

            sum_probility += probility;
        }
        for (ClassifyResult cr : crs) {
            cr.probility = cr.probility / sum_probility;
        }

        return crs;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文本
     * @return 分类结果
     */
    public final String classify(final String text, double user_level) {
        double flag_comment_naive_bayse_pro_abs_threshold = 0.03;
        double flag_comment_match_vocabulary_count_threshold = 5;
        double flag_comment_naive_bayse_pro_ulevel_param = 0.01;

        List<ParamsConfigModel> config_list = MParamsConfigService.getInstance().loadAll(); // to be modified 0;
        for (ParamsConfigModel paramsConfigModel : config_list) {
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_abs_threshold")) {
                flag_comment_naive_bayse_pro_abs_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_match_vocabulary_count_threshold")) {
                flag_comment_match_vocabulary_count_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_ulevel_param")) {
                flag_comment_naive_bayse_pro_ulevel_param = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
        }

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliter.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类

        //        int count_match_term = 0;
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				count_match_term++;
        //			}
        //		}
        //        if (count_match_term < flag_comment_match_vocabulary_count_threshold) {
        //			return "nospam";
        //		}

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        if (match_terms.size() < flag_comment_match_vocabulary_count_threshold) {
            return "nospam";
        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;
        //        LogUtil.stdout.error("a : " + a);
        //        LogUtil.stdout.error("b : " + b);

        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        double configurate = (int) user_level / 3 * flag_comment_naive_bayse_pro_ulevel_param
                + flag_comment_naive_bayse_pro_abs_threshold;
        //        LogUtil.stdout.error("x : " + x);
        //        LogUtil.stdout.error("configuration : " + configurate);
        if (x < configurate) {
            return "nospam";
        }

        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    public final String classify(final String text, double user_level, String flag) {
        double flag_comment_naive_bayse_pro_abs_threshold = 0.03;
        double flag_comment_match_vocabulary_count_threshold = 5;
        double flag_comment_naive_bayse_pro_ulevel_param = 0.01;

        List<ParamsConfigModel> config_list = MParamsConfigService.getInstance().loadAll(); // to be modified 0;
        for (ParamsConfigModel paramsConfigModel : config_list) {
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_abs_threshold")) {
                flag_comment_naive_bayse_pro_abs_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_match_vocabulary_count_threshold")) {
                flag_comment_match_vocabulary_count_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_ulevel_param")) {
                flag_comment_naive_bayse_pro_ulevel_param = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
        }

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);

        //        int count_match_term = 0;
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				count_match_term++;
        //			}
        //		}
        //        if (count_match_term < flag_comment_match_vocabulary_count_threshold) {
        //			return "nospam";
        //		}

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        if (match_terms.size() < flag_comment_match_vocabulary_count_threshold) {
            return "nospam";
        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;
        //        LogUtil.stdout.error("a : " + a);
        //        LogUtil.stdout.error("b : " + b);

        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        double configurate = (int) user_level / 3 * flag_comment_naive_bayse_pro_ulevel_param
                + flag_comment_naive_bayse_pro_abs_threshold;
        //        LogUtil.stdout.error("x : " + x);
        //        LogUtil.stdout.error("configuration : " + configurate);
        if (x < configurate) {
            return "nospam";
        }

        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文本
     * @return 分类结果
     */
    public final String classify_control_match_terms_count(final String text, double user_level) {
        double flag_comment_naive_bayse_pro_abs_threshold = 0.03;
        double flag_comment_match_vocabulary_count_threshold = 5;
        double flag_comment_naive_bayse_pro_ulevel_param = 0.01;

        List<ParamsConfigModel> config_list = MParamsConfigService.getInstance().loadAll(); // to be modified 0;
        for (ParamsConfigModel paramsConfigModel : config_list) {
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_abs_threshold")) {
                flag_comment_naive_bayse_pro_abs_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_match_vocabulary_count_threshold")) {
                flag_comment_match_vocabulary_count_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_ulevel_param")) {
                flag_comment_naive_bayse_pro_ulevel_param = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
        }

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliter.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类

        int count_match_term = 0;
        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                count_match_term++;
                match_terms.add(term);
            }
        }
        if (match_terms.size() < 10) {
            return "nospam";
        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;
        //        LogUtil.stdout.error("a : " + a);
        //        LogUtil.stdout.error("b : " + b);

        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        double configurate = (int) user_level / 3 * flag_comment_naive_bayse_pro_ulevel_param
                + flag_comment_naive_bayse_pro_abs_threshold;
        //        LogUtil.stdout.error("x : " + x);
        //        LogUtil.stdout.error("configuration : " + configurate);
        if (x < configurate) {
            return "nospam";
        }

        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文本
     * @return 分类结果
     */
    public final String classify(final String text) {
        double flag_comment_naive_bayse_pro_abs_threshold = 0;
        double flag_comment_match_vocabulary_count_threshold = 5;

        List<ParamsConfigModel> config_list = MParamsConfigService.getInstance().loadAll(); // to be modified 0;
        for (ParamsConfigModel paramsConfigModel : config_list) {
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_abs_threshold")) {
                flag_comment_naive_bayse_pro_abs_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_match_vocabulary_count_threshold")) {
                flag_comment_match_vocabulary_count_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
        }

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliter.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类

        //        int count_match_term = 0;
        //        String stringterms = "";
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				count_match_term++;
        //				stringterms += (term + " + ");
        //			}
        //		}
        //        LogUtil.stdout.error("114zxxusertextterms :: " + stringterms + " = " + count_match_term);
        //        if (count_match_term < flag_comment_match_vocabulary_count_threshold) {
        //			return "nospam";
        //		}

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        if (match_terms.size() < flag_comment_match_vocabulary_count_threshold) {
            return "nospam";
        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;

        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        if (x < flag_comment_naive_bayse_pro_abs_threshold) {
            return "nospam";
        }

        return maxElem.classification;
    }


    public final String classify(final String text, String flag) {
        if (text == null || text.length() < 1) return "nospam";
        double flag_comment_naive_bayse_pro_abs_threshold = 0;
        double flag_comment_match_vocabulary_count_threshold = 5;

        List<ParamsConfigModel> config_list = MParamsConfigService.getInstance().loadAll(); // to be modified 0;
        for (ParamsConfigModel paramsConfigModel : config_list) {
            if (paramsConfigModel.getKey().equals("comment_naive_bayse_pro_abs_threshold")) {
                flag_comment_naive_bayse_pro_abs_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
            if (paramsConfigModel.getKey().equals("comment_match_vocabulary_count_threshold")) {
                flag_comment_match_vocabulary_count_threshold = Float
                        .parseFloat(paramsConfigModel.getValue());
            }
        }

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);

        //        int count_match_term = 0;
        //        String stringterms = "";
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				count_match_term++;
        //				stringterms += (term + " + ");
        //			}
        //		}
        //        LogUtil.stdout.error("114zxxusertextterms :: " + stringterms + " = " + count_match_term);
        //        if (count_match_term < flag_comment_match_vocabulary_count_threshold) {
        //			return "nospam";
        //		}

        Set<String> match_terms = new HashSet<>();
        if (terms == null || terms.length < 1) return "nospam";
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        if (match_terms.size() < flag_comment_match_vocabulary_count_threshold) {
            return "nospam";
        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;

        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));
        if (x < flag_comment_naive_bayse_pro_abs_threshold) {
            return "nospam";
        }

        return maxElem.classification;
    }

    public final String actionClassify(final String text, String textFlag, int segFlag) {

        String[] terms = StringEscapeUtil.getTermsbyFilter(text, textFlag, segFlag);

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        return maxElem.classification;
    }

    public final String actionClassify(final String[] texts, String textFlag, int segFlag,
            List<ClassifyResult> classifyResults) {

        String[] terms = texts;
        if (terms == null || terms.length < 1) return "nospam";
        double probility;
        double sum_probility = 0.0;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = actionCalcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);

            cr = new ClassifyResult();
            probility = actionCalcProbability(terms, i);
            sum_probility += probility;
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            classifyResults.add(cr);
        }

        //用非Log形式表示Spam概率
        if (sum_probility != 0) {
            for (ClassifyResult cr : classifyResults) {
                cr.probility = cr.probility / sum_probility;
            }
        }
        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        return maxElem.classification;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文�?
     * @return 分类结果
     */
    public final String classifyTwoTerms(final String text) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词�?
        //        System.out.println(text);
        String text1 = StringEscapeUtil.filterByRegEx(text);
        terms = ChineseSpliterSmart.split(text1, " ").split(" ");
        for (String string : terms) {
            //        	System.out.println(" before drop stopword" + string);
        }
        //        System.err.println("------------");
        //        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类
        for (String string : terms) {
            //			System.out.println(" after drop stopword " + string);
        }
        if (terms.length < 2) {
            return "nospam";
        }
        for (int i = 0; i < terms.length - 1; i++) {
            terms[i] = terms[i] + terms[i + 1];
        }
        terms[terms.length - 1] = "";

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        String[] v_terms = new String[match_terms.size()];
        int count = 0;
        for (String term : match_terms) {
            v_terms[count] = term;
            count++;
        }
        //        System.out.println("termstring :: " + string);
        //        if (match_terms.size() < 3) {
        //        	System.err.println("tokens < 3 :: " + match_terms.toString());
        //			return "nospam";
        //		}
        //        System.out.println("match terms :: " + match_terms.size());
        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(v_terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret <= 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    public final String classifyTwoTerms(final String text, String flag) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词�?
        //        System.out.println(text);
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);

        if (terms.length < 2) {
            return "nospam";
        }
        for (int i = 0; i < terms.length - 1; i++) {
            terms[i] = terms[i] + terms[i + 1];
        }
        terms[terms.length - 1] = "";

        Set<String> match_terms = new HashSet<>();
        for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                match_terms.add(term);
            }
        }
        String[] v_terms = new String[match_terms.size()];
        int count = 0;
        for (String term : match_terms) {
            v_terms[count] = term;
            count++;
        }
        //        System.out.println("termstring :: " + string);
        //        if (match_terms.size() < 3) {
        //        	System.err.println("tokens < 3 :: " + match_terms.toString());
        //			return "nospam";
        //		}
        //        System.out.println("match terms :: " + match_terms.size());
        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(v_terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret <= 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文本
     * @return 分类结果
     */
    public final String classify_ori(final String text) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliter.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
            // 保存分类结果
            ClassifyResult cr = new ClassifyResult();
            cr.classification = model.classifications[i]; // 分类
            cr.probility = probility; // 关键字在分类的条件概率
            //System.out.println(model.classifications[i] + "：" + probility);
            crs.add(cr);
        }

        // 找出最大的元素
        ClassifyResult maxElem = Collections.max(crs, (o1, o2) -> {
            final ClassifyResult m1 = o1;
            final ClassifyResult m2 = o2;
            final double ret = m1.probility - m2.probility;
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        double a = crs.get(0).probility;
        double b = crs.get(1).probility;
        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    public final void train(String intermediateData, String modelFile) {
        // 加载中间数据文件
        loadData(intermediateData);

        System.out.println("tokens : " + db.tokens);
        System.out.println("files : " + db.files);
        System.out.println("vocabulary size : " + db.vocabulary.size());

        model = new TrainnedModel(db.classifications.length);

        model.classifications = db.classifications;
        model.vocabulary = db.vocabulary;
        // 开始训练
        calculatePc();
        calculatePxc();
        //db = null;

        try {
            // 用序列化，将训练得到的结果存放到模型文件中
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFile));
            out.writeObject(model);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final void loadData(String intermediateData) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(intermediateData));
            db = (IntermediateData) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 计算先验概率P(c). */
    protected void calculatePc() {
    }

    /** 计算类条件概率P(x|c). */
    protected void calculatePxc() {
    }

    /**
     * 计算文本属性向量X在类Cj下的后验概率P(Cj|X).
     *
     * @param x
     *            文本属性向量
     * @param cj
     *            给定的类别
     * @return 后验概率
     */
    protected double calcProbability(final String[] x, final int cj) {
        return 0;
    }

    protected double actionCalcProbability(final String[] x, final int cj) {
        return 0;
    }

    /**
     * 计算文本属性向量X在类Cj下的后验概率P(Cj|X).
     *
     * @param x
     *            文本属性向量
     * @param cj
     *            给定的类别
     * @return 后验概率
     */
    protected double calcProd(final String[] x, final int cj) {
        return 0;
    }

    protected double actionCalcProd(final String[] x, final int cj) {
        return 0;
    }

    public final double getCorrectRate(String classifiedDir, String encoding, String model) {
        int total = 0;
        int correct = 0;

        loadModel(model);

        File dir = new File(classifiedDir);

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("训练语料库搜索失败！ [" + classifiedDir + "]");
        }

        String[] classifications = dir.list();
        for (String c : classifications) {
            String[] filesPath = IntermediateData.getFilesPath(classifiedDir, c);
            for (String file : filesPath) {

                //                List<String> text = new ArrayList<String>();
                //                try {
                //                    text = IntermediateData.getTextList(file, encoding);
                //                } catch (IOException e) {
                //                    // TODO Auto-generated catch block
                //                    e.printStackTrace();
                //                }
                //                for (String string : text) {
                //                    total++;
                //                    String classification = classify(string);
                //                    if (classification.equals(c)) { // 计算出的类别，和原始的类别是否相同
                //                        correct++;
                //                    } else {
                //                        System.err.print(classification + " : ");
                //                        System.err.println(string);
                //                    }
                //                }

                File textFile = new File(file);
                try (FileReader fReader = new FileReader(textFile);
                        BufferedReader bReader = new BufferedReader(fReader)) {
                    String str;
                    while ((str = bReader.readLine()) != null) {
                        total++;
                        String classification = classify(str);
                        if (classification.equals(c)) { // 计算出的类别，和原始的类别是否相同
                            correct++;
                        } else {
                            System.err.print(classification + " : ");
                            System.err.println(str);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("correct : " + correct);
        System.out.println("total : " + total);
        return (double) correct / (double) total * 100;
    }

    /** 打印命令行参数的解释信息. */
    private static void usage() {
        // 根据中间数据文件，训练产生模型文件
        System.err.println("usage:\t -t <中间文件> <模型文件>");
        // 对已经分类好的文本库，用某个模型文件来分类，测试正确率
        System.err.println("usage:\t -r <语料库目录> <语料库文本编码> <模型文件>");
        // 用某一个训练好的模型文件，来分类某个文本文件
        System.err.println("usage:\t <模型文件> <文本文件> <文本文件编码>");
    }

    public static void test(NaiveBayesClassifier classifier, String[] args) {
        long startTime = System.currentTimeMillis(); // 获取开始时间
        if (args.length < 3) {
            usage();
            return;
        }

        if (args[0].equals("-t")) { // 训练
            classifier.train(args[1], args[2]);
            System.out.println("训练完毕");
        } else if (args[0].equals("-r")) { // 获取正确率
            double ret = classifier.getCorrectRate(args[1], args[2], args[3]);
            System.out.println("正确率为：" + ret);
        } else { // 分类
            classifier.loadModel(args[0]);

            String text;
            text = args[1]; //IntermediateData.getText(args[1], args[2]);

            String result = classifier.classify(text); // 进行分类

            System.out.println("此属于[" + result + "]");
        }

        long endTime = System.currentTimeMillis(); // 获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
    }
}
// CHECKSTYLE:ON
