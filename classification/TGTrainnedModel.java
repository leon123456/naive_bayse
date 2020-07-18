package com.kuaishou.old.antispam.classification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

// CHECKSTYLE:OFF
public class TGTrainnedModel implements Serializable {

    /** �����. */
    public String[] classifications;
    /** ���Ͽ��������ֹ��ĵ���. */
    HashSet<String> vocabulary; // ��BernoulliNB�ڷ���ʱcalcProd()Ҫ�õ�
    /*文本第一个词的集合*/
    HashSet<String> headVocabulary;

    HashSet<TermsPair> termsVocabulary;
    /** �����������. */
    private double[] pC;
    /** ���Ե����������ʣ�String �ĸ�ʽΪ ������#�����. */
    private HashMap[] pXC;

    private HashMap[] pXXC;

    private HashMap[] phXC;

    public int allTokens;

    public int allNgTokens;

    public TGTrainnedModel(int n) {
        pC = new double[n];
        pXC = new HashMap[n];
        pXXC = new HashMap[n];
        phXC = new HashMap[n];
        for (int i = 0; i < n; i++) {
            pXC[i] = new HashMap<String, Double>();
            pXXC[i] = new HashMap<TermsPair, Double>();
            phXC[i] = new HashMap<String, Double>();
        }
    }

    /**
     * �������.
     *
     * @param c
     *            �����ķ���
     * @return ���������µ��������
     */
    public final double getPc(final int c) {
        return pC[c];
    }

    /**
     * �����������.
     *
     * @param c
     *            �����ķ���
     * @return ���������µ��������
     */
    public final void setPc(final int c, double p) {
        pC[c] = p;
    }

    /**
     * �������������.
     *
     * @param x
     *            �������ı�����
     * @param c
     *            �����ķ���
     * @return �����Ե�����������
     */
    public final double getPxc(final String x, final int c) {
        Double ret = 0.0;
        HashMap<String, Double> p = pXC[c];
        Double f = p.get(x);

        if (f != null) {
            ret = f;
        }
        return ret;
    }

    /**
     * ��������������.
     *
     * @param x
     *            �������ı�����
     * @param c
     *            �����ķ���
     * @return �����Ե�����������
     */
    public final void setPxc(final String x, final int c, double p) {
        pXC[c].put(x, p);
    }

    public final double getPxxc(final TermsPair tp, final int c) {
        Double ret = 0.0;
        HashMap<TermsPair, Double> p = pXXC[c];
        Double f = p.get(tp);
        if (f != null) {
            ret = f;
        }

        return ret;
    }

    public final void setPxxc(final TermsPair tp, int c, double p) {
        pXXC[c].put(tp, p);
    }

    public final double getPhxc(final String x, int c) {
        Double ret = 0.0;
        HashMap<String, Double> p = phXC[c];
        Double hp = p.get(x);
        if (hp != null) {
            ret = hp;
        }

        return ret;
    }

    public final void setPhxc(final String x, int c, double p) {
        phXC[c].put(x, p);
    }
}
// CHECKSTYLE:ON
