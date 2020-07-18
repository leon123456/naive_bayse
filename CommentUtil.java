package com.kuaishou.old.antispam;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;
import com.kuaishou.old.util.AtUtils;
import com.kuaishou.old.util.EmojiCodeUtil;
import com.kuaishou.old.util.StringEscapeUtil;

/**
 *
 */
// CHECKSTYLE:OFF
public class CommentUtil {

    public static float getLcsSimilarity(String fs1, String fs2) {
        String ret1 = AtUtils.toPlainTextAt(fs1);
        String origin1 = StringEscapeUtil.filterByRegEx(ret1.replace(" ", ""));

        String ret2 = AtUtils.toPlainTextAt(fs2);
        String origin2 = StringEscapeUtil.filterByRegEx(ret2.replace(" ", ""));

        String common = getLcs(fs1, fs2);

        return (float) (common.length() * 1.0) / (Math.max(origin1.length(), origin2.length()) + 1);
    }

    public static float getLcsSimilarityWithAt(String fs1, String fs2) {

        String origin1 = StringEscapeUtil.filterByRegEx(fs1.replace(" ", ""));
        String origin2 = StringEscapeUtil.filterByRegEx(fs2.replace(" ", ""));
        String common = getLcs(origin1, origin2);

        return (float) (common.length() * 1.0) / (Math.max(origin1.length(), origin2.length()) + 1);
    }

    private static final Set<String> INVALID_WORDS = Sets.newHashSet("专业", "减肥");
    private static final int MIN_COMMENT_LENGTH = 1; //公共字符串小于等于这个值的时候不做处理
    private static final int NO_EXTEND_COMMENT_LENGTH = 5; //被检测的字符长度小于这个值得时候不做+1优化

    public static float getSimilarityForShortText(String detectStr, String badStr) {

        String origin1 = StringEscapeUtil.filterByRegEx(detectStr.replace(" ", ""));
        String origin2 = StringEscapeUtil.filterByRegEx(badStr.replace(" ", ""));

        String common = getLcs(origin1, origin2);
        if (StringUtils.length(common) <= MIN_COMMENT_LENGTH || INVALID_WORDS.contains(common)) {
            return 0;
        }
        if (StringUtils.length(badStr) < NO_EXTEND_COMMENT_LENGTH) {
            return (float) (common.length() * 1.0) / Math.max(origin1.length(), origin2.length());
        } else {
            return (float) (common.length() * 1.0)
                    / (Math.max(origin1.length(), origin2.length()) + 1);
        }

    }

    public static float getLcsSimilarityNoEmojiStopword(String fs1, String fs2) {
        String ret1 = AtUtils.toPlainTextAt(fs1);
        String origin1 = StringEscapeUtil.filterByRegEx(ret1.replace(" ", ""));
        origin1 = EmojiCodeUtil.deleteEmoji(origin1);

        String ret2 = AtUtils.toPlainTextAt(fs2);
        String origin2 = StringEscapeUtil.filterByRegEx(ret2.replace(" ", ""));
        origin2 = EmojiCodeUtil.deleteEmoji(origin2);

        String common = getLcs(origin1, origin2);
        return (float) (common.length() * 1.0) / (Math.max(origin1.length(), origin2.length()) + 1);
    }

    public static String getLcs(String fs1, String fs2) {
        String s1 = AtUtils.toPlainTextAt(fs1);
        String s2 = AtUtils.toPlainTextAt(fs2);

        int M = s1.length();
        int N = s2.length();

        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
        int[][] opt = new int[M + 1][N + 1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = M - 1; i >= 0; i--) {
            for (int j = N - 1; j >= 0; j--) {
                if (s1.charAt(i) == s2.charAt(j)) {
                    opt[i][j] = opt[i + 1][j + 1] + 1;
                } else {
                    opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
                }
            }
        }

        String result = "";
        // recover LCS itself and print it to standard output
        int i = 0, j = 0;
        while (i < M && j < N) {
            if (s1.charAt(i) == s2.charAt(j)) {
                result = result.concat("" + s1.charAt(i));
                i++;
                j++;
            } else if (opt[i + 1][j] >= opt[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

}
// CHECKSTYLE:ON
