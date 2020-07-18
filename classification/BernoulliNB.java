package com.kuaishou.old.antispam.classification;

import java.util.HashMap;
import java.util.HashSet;

// CHECKSTYLE:OFF
public class BernoulliNB extends NaiveBayesClassifier {

    public static final int LAPLACE = 1;
    public static final int NOLAPLACE = 2;
    public int cal_pxc_type = LAPLACE;

    @Override
    protected void calculatePc() {
        for (int i = 0; i < db.classifications.length; i++) {
            model.setPc(i, (double) db.filesOfC[i] / (double) db.files);
        }
    }

    @Override
    protected void calculatePxc() {
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
                if (cal_pxc_type == LAPLACE) {
                    //					model.setPxc(t, i, (double)(value+1)/(double)(db.tokensOfC[i]+2));
                    model.setPxc(t, i, (double) (value + 1)
                            / (double) (db.tokensOfC[i] + 2 + model.vocabulary.size()));
                } else {
                    model.setPxc(t, i, (double) value / (double) (db.tokensOfC[i] + 1));
                }
            }
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
            System.out.println(word);
            double i = model.getPxc(word, cj);
            System.out.println("p(x|" + cj + ") : " + i);
            ret += Math.log(model.getPxc(word, cj));

            int xx = 0;
            int yy = 0;
            if (db.filesOfXC[0].containsKey(word)) {
                xx = (Integer) db.filesOfXC[0].get(word);
            }
            if (db.filesOfXC[1].containsKey(word)) {
                yy = (Integer) db.filesOfXC[1].get(word);
            }
            System.out.println("db.vocabulary.size() = " + db.tokens);
            double j = (float) (xx + yy) / db.tokens;
            System.out.println("p(x|0) + p(x|1) = " + j);
            ret -= Math.log(j);
        }

        // 再乘以先验概率
        double xx = model.getPc(cj);
        System.out.println("p(" + cj + ") : " + xx);
        ret += Math.log(xx);

        ret = Math.exp(ret);
        System.out.println("p(c|x) = " + ret);
        return ret;
    }

    @Override
    protected double calcProd(final String[] x, final int cj) {
        boolean printlog = false;
        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        for (String word : words) {

            if (printlog) {
                System.out.println("origin word : " + word);
            }
            if (model.vocabulary.contains(word)) {
                if (printlog) {
                    System.out.println("p(" + word + "|" + model.classifications[cj] + ") : "
                            + model.getPxc(word, cj));
                }
                ret += Math.log(model.getPxc(word, cj));
                if (printlog) {
                    System.out.println("ret : " + ret);
                }
            }
        }

        if (printlog) {
            System.out.println("before plus class p : " + ret);
        }
        // 再乘以先验概率
        double xx = model.getPc(cj);
        if (printlog) {
            System.out.println("xx : " + xx);
        }
        ret += Math.log(xx);
        if (printlog) {
            System.out.println(ret);
        }
        return ret;
    }

    @Override
    protected double actionCalcProd(final String[] x, final int cj) {
        boolean printlog = false;
        double ret = 0.0;
        for (String word : x) {

            if (printlog) {
                System.out.println("origin word : " + word);
            }
            if (model.vocabulary.contains(word)) {
                if (printlog) {
                    System.out.println("p(" + word + "|" + model.classifications[cj] + ") : "
                            + model.getPxc(word, cj));
                }
                ret += Math.log(model.getPxc(word, cj));
                if (printlog) {
                    System.out.println("ret : " + ret);
                }
            }
        }

        if (printlog) {
            System.out.println("before plus class p : " + ret);
        }
        // 再乘以先验概率
        double xx = model.getPc(cj);
        if (printlog) {
            System.out.println("xx : " + xx);
        }
        ret += Math.log(xx);
        if (printlog) {
            System.out.println(ret);
        }
        return ret;
    }

    @Override
    protected double calcProbability(final String[] x, final int cj) {
        boolean printlog = false;
        double ret = 1000000.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        for (String word : words) {
            if (model.vocabulary.contains(word)) {
                ret *= model.getPxc(word, cj);
                if (printlog) {
                    System.out.println(word + " :: " + model.getPxc(word, cj));
                    //        		 System.out.println(word + " :: " + model.getPxc(word, cj));
                }
            }
        }

        // 再乘以先验概率
        double xx = model.getPc(cj);
        if (printlog) {
            System.out.println("xx : " + xx);
        }
        ret *= xx;
        if (printlog) {
            System.out.println("ret :: " + ret);
        }
        if (printlog) {
            System.out.println("log(ret) :: " + Math.log(ret / 1000000.0));
        }
        return ret;
    }

    @Override
    protected double actionCalcProbability(final String[] x, final int cj) {
        boolean printlog = false;
        double ret = 1000000.0;

        for (String word : x) {
            if (model.vocabulary.contains(word)) {
                ret *= model.getPxc(word, cj);
                if (printlog) {
                    System.out.println(word + " :: " + model.getPxc(word, cj));
                    //               System.out.println(word + " :: " + model.getPxc(word, cj));
                }
            }
        }

        // 再乘以先验概率
        double xx = model.getPc(cj);
        if (printlog) {
            System.out.println("xx : " + xx);
        }
        ret *= xx;
        if (printlog) {
            System.out.println("ret :: " + ret);
        }
        if (printlog) {
            System.out.println("log(ret) :: " + Math.log(ret / 1000000.0));
        }
        return ret;
    }

    protected double calcProdOld(final String[] x, final int cj) {
        double ret = 0.0;
        HashSet<String> words = new HashSet<>();

        for (String s : x) {
            words.add(s);
        }

        double a = 0, b = 0;
        for (String t : model.vocabulary) {
            if (words.contains(t)) {
                System.out.println("t : " + t + " \t\tp = " + model.getPxc(t, cj));
                ret += Math.log(model.getPxc(t, cj));
            } else {
                //ret += Math.log(1- model.getPxc(t, cj));
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
//        		IntermediateData tdm = new IntermediateData();
//            	try {
//        			tdm.generate("D:/work/AD/caption/trainData",
//        					"utf-8",
//        					"D:/work/AD/caption/db");
//        		} catch (FileNotFoundException e) {
//        			e.printStackTrace();
//        		}

        BernoulliNB nb = new BernoulliNB();

        String[] argv = new String[4];
//        argv[0] = "-t";
//        argv[1] = "D:/work/AD/caption/db";
//        argv[2] = "D:/work/AD/caption/model";
//        NaiveBayesClassifier.test(nb, argv);

        System.out.println("\n\n\n\n--------------------\n\n");

        		argv[0] = "D:/work/AD/caption/model";
        		argv[1]="阿雅专售：jing仿三星W2015 W2014 大器3 三星S6 iphone6 iphone6plus 威图系列 等精仿1:1高端手机 支持鲁大师检测真机 QQ:719939999 微信:a719939999 （支持货到付款 保修一年 ）另无限招dai理";
//        		int userId = 81782243;
//        		List<UserActionService.Action> actions = UserActionService.getUserAction(userId);
//        		String[] texts = UserActionFeatureGenerate.actionsToSegment(actions, 3);
//        		List<ClassifyResult> crs = new ArrayList<>();
//        		String result = nb.actionClassify(texts, FilterType.ACTION_STRING_SHINGLING, 3, crs);
//        		System.out.println("class: " + result);
        		test(nb, argv);
//        List<ClassifyResult> cls = nb.getClassifyProbility(argv[1]);
//        for(ClassifyResult cr : cls){
//            System.out.println(cr.classification + ":" + cr.probility);
//        }

//        		argv[0] = "-r";
//        		argv[1] = "D:/work/spotBot/actionSequence/testData";
//        		argv[2] = "utf-8";
//        		argv[3] = "D:/work/spotBot/actionSequence/model";
//        		BernoulliNB.test(nb, argv);
//        String text = "姐姐寂寞求深射";
//        System.out.println(nb.classify(text, FilterType.NAME_SHINGLING));
    }
}
// CHECKSTYLE:ON
