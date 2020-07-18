package com.kuaishou.old.antispam.classification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kuaishou.mservice.service.MParamsConfigService;
import com.kuaishou.old.mmodel.ParamsConfigModel;
import com.kuaishou.old.split.ChineseSpliter;
import com.kuaishou.old.split.ChineseSpliterSmart;
import com.kuaishou.old.util.StringEscapeUtil;

/**
 * 朴素贝叶斯分类器.
 */
// CHECKSTYLE:OFF
public class NaiveBayesClassifier1 {

    public TGTrainnedModel model;

    protected transient TGIntermediateData db;

    public NaiveBayesClassifier1() {
    }

    /**
     * 加载数据模型文件.
     *
     * @param modelFile
     *            模型文件路径
     */
    public final void loadModel(final String modelFile) {
        try (FileInputStream fis = new FileInputStream(modelFile);
                ObjectInputStream in = new ObjectInputStream(fis)) {
            model = (TGTrainnedModel) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected double calcProbability(final String[] x, final int cj) {
        return 0;
    }

    public final List<ClassifyResult> getClassifyProbility(final String text) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = ChineseSpliterSmart.split(text, " ").split(" ");
        terms = ChineseSpliter.dropStopWords(terms); // 去掉停用词，以免影响分类

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
        for (ClassifyResult cr : crs) {
            cr.probility = cr.probility / sum_probility;
        }

        return crs;
    }

    public final List<ClassifyResult> getClassifyProbility(final String text, String flag) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词）
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);

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
        for (ClassifyResult cr : crs) {
            cr.probility = cr.probility / sum_probility;
        }

        return crs;
    }

    /**
     * 对给定的文本进行分类.
     *
     * @param text
     *            给定的文�?
     * @return 分类结果
     */
    public final String classify(final String text) {

        String[] terms;
        // 中文分词处理(分词后结果可能还包含有停用词�?
        //        System.out.println(text);
        terms = ChineseSpliterSmart.split(text, " ").split(" ");
        for (String string : terms) {
            // System.out.println(" before drop stopword" + string);
        }
        // System.err.println("------------");
        terms = ChineseSpliterSmart.dropStopWords(terms); // 去掉停用词，以免影响分类
        for (String string : terms) {
            // System.out.println(" after drop stopword " + string);
        }

        int count_match_term = 0;
        String string = "";
        Set<TermsPair> match_termpairs = new HashSet<>();
        /*for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                count_match_term++;
                match_terms.add(term);
                string += (term + "+");
            }
        }*/

        for (int i = 0; i < terms.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = terms[i];
            tp.terms2 = terms[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                match_termpairs.add(tp);
            }

        }
        //        System.out.println("matchterms :: " + match_termpairs.size());
        if (match_termpairs.size() < 3) {
            // System.err.println("matchterms < 5 :: " +  match_termpairs.toString());
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
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
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

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

    public final String classifyForReviewTextName(final String text) {

        String[] terms;
        terms = ChineseSpliterSmart.nGramShingle(text);
        int count_match_term = 0;
        String string = "";
        Set<TermsPair> match_termpairs = new HashSet<>();

        for (int i = 0; i < terms.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = terms[i];
            tp.terms2 = terms[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                match_termpairs.add(tp);
            }

        }

        double probility;
        List<ClassifyResult> crs = new ArrayList<>(); // 分类结果
        for (int i = 0; i < model.classifications.length; i++) {
            // 计算给定的文本属性向量terms在给定的分类Ci中的分类条件概率
            probility = calcProd(terms, i);
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
            if (ret < 0) {
                return -1;
            } else {
                return 1;
            }
        });

        return maxElem.classification;
    }

    public final String classify(final String text, String flag) {
        if (text == null || text.length() < 1) return "nospam";
        String[] terms;
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);

        int count_match_term = 0;
        String string = "";
        Set<TermsPair> match_termpairs = new HashSet<>();
        /*for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                count_match_term++;
                match_terms.add(term);
                string += (term + "+");
            }
        }*/

        for (int i = 0; i < terms.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = terms[i];
            tp.terms2 = terms[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                match_termpairs.add(tp);
            }

        }
        //        System.out.println("matchterms :: " + match_termpairs.size());
        if (match_termpairs.size() < 3) {
            //        	System.err.println("matchterms < 5 :: " +  match_termpairs.toString());
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
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
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

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        if (Double.isInfinite(a) && Double.isInfinite(b)) {
            return "nospam";
        }

        return maxElem.classification;
    }

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
        // 中文分词处理(分词后结果可能还包含有停用词�?
        //        System.out.println(text);
        terms = ChineseSpliterSmart.split(text, " ").split(" ");
        for (String string : terms) {
            //        	System.out.println(" before drop stopword" + string);
        }
        //        System.err.println("------------");
        terms = ChineseSpliterSmart.dropStopWords(terms); // 去掉停用词，以免影响分类
        for (String string : terms) {
            // System.out.println(" after drop stopword " + string);
        }

        int count_match_term = 0;
        String string = "";
        Set<TermsPair> match_termpairs = new HashSet<>();
        /*for (String term : terms) {
            if (model.vocabulary.contains(term)) {
                count_match_term++;
                match_terms.add(term);
                string += (term + "+");
            }
        }*/

        for (int i = 0; i < terms.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = terms[i];
            tp.terms2 = terms[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                match_termpairs.add(tp);
            }

        }
        //        System.out.println("matchterms :: " + match_termpairs.size());
        if (match_termpairs.size() < 3) {
            // System.err.println("matchterms < 5 :: " +  match_termpairs.toString());
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
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
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

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        double configurate = (int) user_level / 3 * flag_comment_naive_bayse_pro_ulevel_param
                + flag_comment_naive_bayse_pro_abs_threshold;
        //      LogUtil.stdout.error("x : " + x);
        //      LogUtil.stdout.error("configuration : " + configurate);
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
        // 中文分词处理(分词后结果可能还包含有停用词�?
        terms = StringEscapeUtil.getTermsbyFilter(text, flag);
        //        System.out.println(text);
        int count_match_term = 0;
        String string = "";
        Set<TermsPair> match_termpairs = new HashSet<>();
        //        for (String term : terms) {
        //			if (model.vocabulary.contains(term)) {
        //				count_match_term++;
        //				match_terms.add(term);
        //				string += (term + "+");
        //			}
        //		}

        for (int i = 0; i < terms.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = terms[i];
            tp.terms2 = terms[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                match_termpairs.add(tp);
            }

        }
        //        System.out.println("matchterms :: " + match_termpairs.size());
        if (match_termpairs.size() < 3) {
            //        	System.err.println("matchterms < 5 :: " +  match_termpairs.toString());
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
            cr.probility = probility; // 关键字在分类的条件概�?
            //System.out.println(model.classifications[i] + "�?" + probility);
            crs.add(cr);
        }

        // 找出�?大的元素
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

        //        System.out.println("a : " + a);
        //        System.out.println("b : " + b);
        double x = Math.abs(Math.abs(Math.abs(a) - Math.abs(b)) / Math.max(a, b));

        double configurate = (int) user_level / 3 * flag_comment_naive_bayse_pro_ulevel_param
                + flag_comment_naive_bayse_pro_abs_threshold;
        //      LogUtil.stdout.error("x : " + x);
        //      LogUtil.stdout.error("configuration : " + configurate);
        if (x < configurate) {
            return "nospam";
        }

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

        model = new TGTrainnedModel(db.classifications.length);

        model.classifications = db.classifications;
        model.vocabulary = db.vocabulary;
        model.headVocabulary = db.headVocabulary;
        model.termsVocabulary = db.termsVocabulary;
        // �?始训�?
        calculatePc();
        calculatePxc();
        calculatePxx();
        //    	model.allTokens = db.tokens;
        //    	model.allNgTokens = db.ngtokens;
        //db = null;

        try {
            // 用序列化，将训练得到的结果存放到模型文件�?
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
            db = (TGIntermediateData) in.readObject();
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

    protected void calculatePxx() {
    }

    /**
     * 计算文本属�?�向量X在类Cj下的后验概率P(Cj|X).
     *
     * @param x
     *            文本属�?�向�?
     * @param cj
     *            给定的类�?
     * @return 后验概率
     */
    protected double calcProd(final String[] x, final int cj) {
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

                List<String> text = new ArrayList<>();
                try {
                    text = IntermediateData.getTextList(file, encoding);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for (String string : text) {
                    total++;
                    string = filterByRegEx(string);
                    String classification = classify(string);
                    if (classification.equals(c)) { // 计算出的类别，和原始的类别是否相同
                        //                    	System.out.println("right : " + string);
                        correct++;
                    } else {
                        System.err.print("Error : " + classification + " : ");
                        System.err.println(string + "\n");
                    }
                }
            }
        }
        System.out.println("correct : " + correct);
        System.out.println("total : " + total);
        return (double) correct / (double) total * 100;
    }

    /** 打印命令行参数的解释信息. */
    private static void usage() {
        // 根据中间数据文件，训练产生模型文�?
        System.err.println("usage:\t -t <中间文件> <模型文件>");
        // 对已经分类好的文本库，用某个模型文件来分类，测试正确�?
        System.err.println("usage:\t -r <语料库目�?> <语料库文本编�?> <模型文件>");
        // 用某�?个训练好的模型文件，来分类某个文本文�?
        System.err.println("usage:\t <模型文件> <文本文件> <文本文件编码>");
    }

    public static void test(NaiveBayesClassifier1 classifier, String[] args) {
        long startTime = System.currentTimeMillis(); // 获取�?始时�?
        if (args.length < 3) {
            usage();
            return;
        }

        if (args[0].equals("-t")) { // 训练
            classifier.train(args[1], args[2]);
            System.out.println("训练完毕");
        } else if (args[0].equals("-r")) { // 获取正确�?
            double ret = classifier.getCorrectRate(args[1], args[2], args[3]);
            System.out.println("正确率为 : " + ret);
        } else { // 分类
            classifier.loadModel(args[0]);

            String text;
            text = args[1]; //IntermediateData.getText(args[1], args[2]);

            System.out.println(text);
            String result = classifier.classify(text); // 进行分类

            System.out.println("此属于[" + result + "]");
        }

        long endTime = System.currentTimeMillis(); // 获取结束时间
        System.out.println("程序运行时间 : " + (endTime - startTime) + "ms");
    }

    public static String filterByRegEx(String str) {
        // 只允许字母和数字
        // String   regEx  =  "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
// CHECKSTYLE:ON
