package com.kuaishou.old.antispam.classification;

import java.io.Serializable;

// CHECKSTYLE:OFF
public class TermsPair implements Serializable {

    public String terms1;
    public String terms2;

    @Override
    public String toString() {
        return terms1 + " -> " + terms2;
    }

    @Override
    public boolean equals(Object termsPair) {
        TermsPair tp = (TermsPair) termsPair;
        boolean isEqual = false;
        if (terms1.equals(tp.terms1) && terms2.equals(tp.terms2)) {
            isEqual = true;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        int h;
        //		String pairStr = terms1 + terms2;
        ////		System.out.println("pairStr :: " + pairStr);
        //		char val[] =pairStr.toCharArray();
        //		int num = 0;
        //		for(int i=0; i< val.length; i++){
        //			h = 31*h + val[i];
        //			num = 0 + val[i];
        ////			System.out.println("char :: " + val[i] +  " " + num);
        //		}
        h = terms1.hashCode() + terms2.hashCode();
        return h;
    }
}
// CHECKSTYLE:ON
