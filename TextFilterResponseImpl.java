package com.kuaishou.old.antispam;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.gifshow.scanner.algos.AlgosMatcher;
import com.kuaishou.antispam.service.LiteralUtil;
import com.kuaishou.old.util.StringUtil;

/**
 * 使用时注意action为0，-1时，replaceString和原content也可能不同，一定不能直接用replaceString
 */
public class TextFilterResponseImpl implements TextFilterResponse {

    private static final int FACTOR = 10;
    private int action;
    private List<MatchText> matchText;
    private String replaceString;
    @Override
    public int getAction() {
        return action;
    }
    @Override
    public void setAction(int action) {
        this.action = action;
    }
    @Override
    public List<MatchText> getMatchText() {
        return matchText;
    }
    @Override
    public void setMatchText(List<MatchText> matchText) {
        this.matchText = matchText;
    }
    @Override
    public String getReplaceString() {
        return replaceString;
    }
    @Override
    public void setReplaceString(String replaceString) {
        this.replaceString = replaceString;
    }

    private static boolean isHigherAction(int action, int originAction) {
        action = action < FACTOR ? action * FACTOR : action;
        originAction = originAction < FACTOR ? originAction * FACTOR : originAction;
        return action > originAction;
    }

    TextFilterResponseImpl(List<AlgosMatcher.MatchResult> result, String input, int textType) {
        action = 0;
        matchText = new ArrayList<>();
        char[] chars = input.toCharArray();

        char wordForReplace = LiteralUtil.hasChinese(input) ? '萌' : '*';
        for (AlgosMatcher.MatchResult match : result) {
            int pos = match.getPos();
            int len = match.getLen();
            String categoryAndAction = match.getCategory();
            String[] fields = categoryAndAction.split(" ");
            if (fields.length > textType + 1) {
                String category = fields[0];
                int textAction = StringUtil.parseToInt(fields[textType + 1]);
                if (textAction >= 1) {
                    if (isHigherAction(textAction, action)) {
                        action = textAction;
                    }
                    MatchText tempMatchText = new MatchText();
                    tempMatchText.setPos(pos);
                    tempMatchText.setLen(len);
                    tempMatchText.setText(match.getMatchedText());
                    tempMatchText.setWord(match.getKeyword());
                    tempMatchText.setCategory(category);
                    matchText.add(tempMatchText);
                    if (pos >= 0 && len > 0) {
                        for (int i = pos; i < pos + len; i++) {
                            chars[i] = wordForReplace;
                        }

                    }
                }
            }
        }
        replaceString = new String(chars);
    }

    // 禁用词以及危险词的解析, action暂时返回最严格的
    TextFilterResponseImpl(List<AlgosMatcher.MatchResult> result, String input) {
        action = 0;
        char[] chars = input.toCharArray();
        matchText = new ArrayList<>();
        char wordForReplace = LiteralUtil.hasChinese(input) ? '萌' : '*';
        for (AlgosMatcher.MatchResult match : result) {
            action = TextFilterService.WORDFILTER_AUTHOR_VISIBLE;
            int pos = match.getPos();
            int len = match.getLen();
            String categoryAndAction = match.getCategory();
            String[] fields = categoryAndAction.split(" ");
            String category = fields[0];
            MatchText tempMatchText = new MatchText();
            tempMatchText.setPos(pos);
            tempMatchText.setLen(len);
            tempMatchText.setText(match.getMatchedText());
            tempMatchText.setWord(match.getKeyword());
            tempMatchText.setCategory(category);
            matchText.add(tempMatchText);
            if (pos >= 0 && len > 0) {
                for (int i = pos; i < pos + len; i++) {
                    chars[i] = wordForReplace;
                }

            }
        }
        replaceString = new String(chars);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Nullable
    public String getKeyword() {
        if (!isEmpty(matchText)) {
            return matchText.get(0).getWord();
        }
        return null;
    }
}