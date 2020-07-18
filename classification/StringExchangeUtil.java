package com.kuaishou.old.antispam.classification;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kuaishou.old.util.EmojiCodeUtil;
import com.kuaishou.old.util.StringEscapeUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

// CHECKSTYLE:OFF
public class StringExchangeUtil {

    public static String strToPinyin(final String text, final String flag) {
        String str = text;
        str = StringEscapeUtil.filterByRegEx(str);
        str = EmojiCodeUtil.deleteEmoji(str);
        System.out.println(str);
        StringBuilder sb = new StringBuilder();

        ArrayList<String> segList = new ArrayList<>();
        Matcher m = Pattern.compile("([a-zA-Z0-9]+|[\u4e00-\u9fa5]+)").matcher(str);
        while (m.find()) {
            segList.add(m.group());
        }

        for (String seg : segList) {
            if (isAllChinese(seg)) {
                char[] words = seg.toCharArray();
                for (char word : words) {
                    sb.append(getPinyin(word, flag)).append(" ");
                }
            } else {
                sb.append(seg).append(" ");
            }
        }

        return sb.toString();
    }

    public static boolean isAllChinese(final String text) {
        Pattern p = Pattern.compile("[^\u4e00-\u9fa5]");
        Matcher m = p.matcher(text);
        return !m.find();
    }

    public static String getPinyin(final char ch_word, final String flag) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        String[] pinyinArray = null;
        try {
            pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch_word, defaultFormat);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        String r_str = "";
        if (pinyinArray != null) {
            if ("porn".equals(flag)) {
                for (String pinyin : pinyinArray) {
                    if (SpecialPinyinHander.inSpecialList(pinyin, SpecialPinyinHander.PORN)) {
                        r_str = getEnglish(pinyin);
                    }
                }
                if (r_str.length() == 0) {
                    r_str = getEnglish(pinyinArray[0]);
                }
            } else {
                r_str = getEnglish(pinyinArray[0]);
            }
        }
        return r_str;
    }

    public static String getEnglish(final String text) {
        return Pattern.compile("[^a-zA-Z]").matcher(text).replaceAll("");
    }

    public static void main(String[] args) {
        String str = "天天化妆的骚女是为了勾引男的 以后就算结婚了也不会收敛 ";
        System.out.println(strToPinyin(str, "porn"));
        //		System.out.println(changeChinese2Pintyi(str));
    }
}
// CHECKSTYLE:ON
