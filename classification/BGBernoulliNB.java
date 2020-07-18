package com.kuaishou.old.antispam.classification;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// CHECKSTYLE:OFF
public class BGBernoulliNB extends NaiveBayesClassifier1 {

    public static final int LAPLACE = 1;
    public static final int NOLAPLACE = 2;
    public int cal_pxc_type = LAPLACE;

    @Override
    protected void calculatePc() {
        for (int i = 0; i < db.classifications.length; i++) {
            model.setPc(i, (double) db.filesOfC[i] / (double) db.files);
        }
    }

    protected void calculatePxc_ok() {
        for (int i = 0; i < db.classifications.length; i++) {
            HashMap<String, Integer> source = db.filesOfXC[i];
            // HashMap<String, Double> target = model.pXC[i];

            for (String t : db.vocabulary) {
                // 本类别下不包含单词t
                Integer value = source.getOrDefault(t, 0);
                int x = db.filesOfC[i];
                int z = db.tokensOfC[i];
                if (db.tokensOfXC[i].containsKey(t)) {
                    int y = (Integer) db.tokensOfXC[i].get(t);
                }

                double a = (double) value / (double) (db.filesOfC[i] + 2);
                model.setPxc(t, i, (double) value / (double) (db.tokensOfC[i] + 1));
                //model.setPxc(t, i, (double)(value+1)/(double)(db.tokensOfC[i]+2));
            }
        }
    }

    @Override
    protected void calculatePxc() {
        System.out.println("cal_pxc_type : " + cal_pxc_type);
        for (int i = 0; i < db.classifications.length; i++) {
            HashMap<String, Integer> source = db.filesOfXC[i];
            // HashMap<String, Double> target = model.pXC[i];

            for (String t : db.vocabulary) {
                //// 本类别下不包含单词t
                Integer value = source.getOrDefault(t, 0);

                int x = db.filesOfC[i];
                int z = db.tokensOfC[i];

                if (db.tokensOfXC[i].containsKey(t)) {
                    int y = (Integer) db.tokensOfXC[i].get(t);
                }

                double a = (double) value / (double) (db.filesOfC[i] + 2);
                if (cal_pxc_type == LAPLACE) {
                    //					System.out.println(t + " : " + i + " : " + (double)(value+1)/(double)(db.tokensOfC[i]+2+model.vocabulary.size()));
                    model.setPxc(t, i, (double) (value + 1) / (double) (db.tokensOfC[i] + 2));
                    //model.setPxc(t, i, (double)(value+1)/(double)(db.tokensOfC[i]+2 + model.vocabulary.size()));
                } else {
                    model.setPxc(t, i, (double) value / (double) (db.tokensOfC[i] + 1));
                }
            }
        }
    }

    @Override
    protected void calculatePxx() {
        for (int i = 0; i < db.classifications.length; i++) {
            HashMap<String, Integer> head = db.headOfXC[i];
            HashMap<TermsPair, Integer> source = db.filesOfXXC[i];

            for (String headStr : db.headVocabulary) {
                Integer value = head.get(headStr);
                double lambda = 1.0;
                if (value == null) {
                    value = 0;
                }

                model.setPhxc(headStr, i,
                        (value + lambda) / (db.filesOfC[i] + db.headVocabulary.size() * lambda));
            }

            for (TermsPair tp : db.termsVocabulary) {
                Integer value1 = source.get(tp);
                double lambda = 1.0;

                if (value1 == null) {
                    value1 = 0;
                }

                Integer value2 = (Integer) db.tokensOfXC[i].get(tp.terms1);
                if (value2 == null) {
                    value2 = 0;
                }
                //System.out.println("db.tokensOfXC[i].get(tp.terms1) :: " + db.tokensOfXC[i].get(tp.terms1));
                model.setPxxc(tp, i,
                        (value1 + lambda) / (value2 + db.termsVocabulary.size() * lambda));
            }

        }
    }

    @SuppressWarnings("unchecked")
    protected void displayPxc() {
        for (int i = 0; i < db.classifications.length; i++) {
            HashMap<String, Integer> source = db.filesOfXC[i];
            HashMap map_Data = new HashMap();
            int count = 0;
            System.out.println(source);
            Iterator iterator = source.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                String key = entry.getKey();
                Integer value = entry.getValue();
                //System.out.println(key + " = " + value);
                count++;
                if (count > 4000) {
                    break;
                }
                map_Data.put(key, value);
            }

            List<Map.Entry<String, Integer>> list_Data = new ArrayList<>(map_Data.entrySet());
            list_Data.sort((o1, o2) -> {
                if (o2.getValue() != null && o1.getValue() != null
                        && o2.getValue().compareTo(o1.getValue()) > 0) {
                    return 1;
                } else {
                    return -1;
                }

            });
            System.out.println(list_Data);

            // HashMap<String, Double> target = model.pXC[i];

        }
    }

    protected void calculatePxcOLD() {
        for (int i = 0; i < db.classifications.length; i++) {
            HashMap<String, Integer> source = db.filesOfXC[i];
            // HashMap<String, Double> target = model.pXC[i];

            for (String t : db.vocabulary) {
                // 本类别下不包含单词t
                Integer value = source.getOrDefault(t, 0);
                int x = db.filesOfC[i];
                double a = (double) (value + 1) / (double) (db.filesOfC[i] + 2);
                model.setPxc(t, i, (double) (value + 1) / (double) (db.filesOfC[i] + 2));
            }
        }
    }

    protected double calcProdProbability(final String[] x, final int cj) {
        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        double a = 0, b = 0;
        for (String word : words) {
            //    		System.out.println(word);
            double i = model.getPxc(word, cj);
            //    		System.out.println("p(x|" + cj + ") : " + i);
            ret += Math.log(model.getPxc(word, cj));

            int xx = 0;
            int yy = 0;
            if (db.filesOfXC[0].containsKey(word)) {
                xx = (Integer) db.filesOfXC[0].get(word);
            }
            if (db.filesOfXC[1].containsKey(word)) {
                yy = (Integer) db.filesOfXC[1].get(word);
            }
            //    		System.out.println("db.vocabulary.size() = " +db.tokens);
            double j = (float) (xx + yy) / db.tokens;
            //    		System.out.println("p(x|0) + p(x|1) = " + j);
            ret -= Math.log(j);
        }

        // 再乘以先验概率
        double xx = model.getPc(cj);
        //    	System.out.println("p(" + cj + ") : " + xx);
        ret += Math.log(xx);

        ret = Math.exp(ret);
        //        System.out.println("p(c|x) = " + ret);
        return ret;
    }

    @Override
    protected double calcProd(final String[] x, final int cj) {
        boolean printlog = false;
        if (printlog) {
            System.out.println("\n+++++++++++++++++++++++\n");
        }

        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        if (printlog) {
            System.out.println(model.classifications[cj]);
        }
        for (int i = 0; i < x.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = x[i];
            tp.terms2 = x[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                ret += Math.log(model.getPxxc(tp, cj));
                if (printlog) {
                    System.out.println(x[i] + " -> " + x[i + 1] + " :: " + model.getPxxc(tp, cj));
                    System.out.println("ret :: " + ret);
                }
            }

            if (i == 0 && model.headVocabulary.contains(x[0])) {
                ret += Math.log(model.getPhxc(x[0], cj));
            }

        }

        //    	for (String word : words) {
        //    		if (printlog) System.out.println("origin word : " +word);
        //    		if (model.vocabulary.contains(word)) {
        //    			if (printlog) {
        //    				System.out.println("p(" + word + "|" + model.classifications[cj] + ") : " + model.getPxc(word, cj));
        ////    				System.out.println(word + " : : " + Math.log(model.getPxc(word, cj)));
        //    			}
        //        		ret += Math.log(model.getPxc(word, cj));
        //
        //        		if (printlog) System.out.println("ret : " + ret);
        //			}
        //		}

        if (printlog) {
            System.out.println("before plus class p : " + ret);
        }
        // 再乘以先验概率
        //    	double xx = model.getPc(cj);
        //    	if (printlog) System.out.println("xx : " + xx);
        //    	ret += Math.log(xx);
        //    	if (printlog) System.out.println("math.log(xx) = " + Math.log(xx));
        if (printlog) {
            System.out.println(ret);
        }
        return ret;
    }

    @Override
    protected double calcProbability(final String[] x, final int cj) {
        double ret = 1000000.0;
        HashSet<String> words = new HashSet<>();

        for (int i = 0; i < x.length - 1; i++) {
            TermsPair tp = new TermsPair();
            tp.terms1 = x[i];
            tp.terms2 = x[i + 1];
            if (model.termsVocabulary.contains(tp)) {
                ret *= model.getPxxc(tp, cj);
            }
        }
        //乘以第一个词出现的概率
        if (x.length > 1) {
            if (model.headVocabulary.contains(x[0])) {
                ret *= model.getPhxc(x[0], cj);
            }
        }
        if (ret == 1000000.0) {
            ret *= model.getPc(cj);
        }

        //    	LogUtil.stdout.error("xx : " + xx);
        //    	LogUtil.stdout.error(ret);
        return ret;
    }

    protected double calcProdOLD(final String[] x, final int cj) {
        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        double a = 0, b = 0;
        for (String t : model.vocabulary) {
            if (words.contains(t)) {
                System.out.println("exist : t : " + t + " \t\tp = " + model.getPxc(t, cj));
                ret += Math.log(model.getPxc(t, cj));
            } else {
                System.out.println("not exist : t : " + t + " \t\tp = " + model.getPxc(t, cj));
                ret += Math.log(1 - model.getPxc(t, cj));
            }
        }

        // 再乘以先验概率
        double xx = model.getPc(cj);
        System.out.println("xx : " + xx);
        ret += Math.log(xx);

        return ret;
    }

    protected double calcProdOri(final String[] x, final int cj) {
        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        for (String t : model.vocabulary) {
            if (words.contains(t)) {
                ret += Math.log(model.getPxc(t, cj));
            } else {
                ret += Math.log(1 - model.getPxc(t, cj));
            }
        }

        // 再乘以先验概率
        ret += Math.log(model.getPc(cj));
        return ret;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TGIntermediateData tdm = new TGIntermediateData();
        try {
            tdm.generate("E:/work/usertextPorn/newdataclean/usertext_porn2014121902bg", "utf-8",
                    "E:/classifier/NGramNaiveBayesClassifier/src/text/classification/data/db");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BGBernoulliNB nb = new BGBernoulliNB();
        nb.cal_pxc_type = BGBernoulliNB.LAPLACE;
        String[] argv = new String[4];

        argv[0] = "-t";
        argv[1] = "E:/classifier/NGramNaiveBayesClassifier/src/text/classification/data/db";
        argv[2] = "E:/classifier/NGramNaiveBayesClassifier/src/text/classification/data/model";
        NaiveBayesClassifier1.test(nb, argv);

        System.out.println("\n\n\n\n--------------------\n\n");

        argv[0] = "E:/classifier/NGramNaiveBayesClassifier/src/text/classification/data/model";
        argv[1] = "QQ:1252790351 我是一个好色的小女孩，经常管不住自己，喜欢与哥n哥玩礻果聊，看哥哥的下面。我不漏脸儿，和我视频过的哥哥都说驾n驭不了我。哥哥你会流鼻血么?和我视频不许截图哦记得.现在有空个n要玩加我QQ:1252790351";
        argv[1] = filterByRegEx(argv[1]);

        NaiveBayesClassifier1.test(nb, argv);

        //		argv[0] = "-r";
        //		argv[1] = "E:/NaiveBayesClassifier/src/com/yanjiuyanjiu/text/classification/data/testporn";
        //		argv[2] = "utf-8";
        //		argv[3] = "E:/classifier/NGramNaiveBayesClassifier/src/text/classification/data/model";
        //		BGBernoulliNB.test(nb, argv);
    }
}
// CHECKSTYLE:ON
