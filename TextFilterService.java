package com.kuaishou.old.antispam;

import static com.github.phantomthief.util.MoreFunctions.catching;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.kuaishou.deeplearning.utils.EmojiToWordsUtils.convertByTrie;
import static com.kuaishou.framework.util.PerfUtils.perf;
import static com.kuaishou.kafka.LogTopic.SENSITIVE_WORD_FILTER_LOG;
import static com.kuaishou.sensitive.model.SensitiveMatcherCType.FORBIDDEN_SENSITIVE_WORDS;
import static com.kuaishou.sensitive.model.SensitiveMatcherType.KUAISHOU_APP_SENSITIVE_WORDS;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gifshow.scanner.algos.AlgosHolder;
import com.gifshow.scanner.algos.AlgosMatcher.MatchResult;
import com.kuaishou.admin.text.model.TextScanType;
import com.kuaishou.framework.kafka.KafkaLogProducerHelper;
import com.kuaishou.framework.spring.BeanFactory;
import com.kuaishou.protobuf.antispam.ModeType;
import com.kuaishou.protobuf.antispam.SensitiveWordFilterInfo;
import com.kuaishou.sensitive.SensitiveMatcherHolder;


/**
 * 敏感词服务
 */
public class TextFilterService {

    // TODO 后面弄成自动注入
    private static KafkaLogProducerHelper kafkaLogProducerHelper = BeanFactory.getBean(KafkaLogProducerHelper.class);

    //CHECKSTYLE:OFF
    public static final int WORDFILTER_NEEDLESS_HIT = -1; //不进行过滤
    public static final int WORDFILTER_NOT_HIT = 0; //未命中词表
    public static final int WORDFILTER_REPLACE = 1; //1级词， 只替换文本 ，但数据会送到审核系统由管理员查看
    public static final int WORDFILTER_SEND_AUDIT = 15; //1.5级词， 推送审核
    public static final int WORDFILTER_AUTHOR_FANS_VISIBLE = 2; //2级词，范围屏蔽作品，仅自己和粉丝可见
    public static final int WORDFILTER_AUTHOR_VISIBLE = 25; // 2.5级词，屏蔽作品，仅自己可见
    public static final int WORDFILTER_DELETE = 26; // 2.6级词，删除，目前视频评论在使用
    public static final int WORDFILTER_ACTION_BANG = 3; //3级词，会对用户做出封禁,行为封禁等操作，屏蔽等操作
    public static final int WORDFILTER_USER_BANG = 4; //4级， 直接封禁用户，一般作用于SPAM的行为

    public static final String WORDFILTER_BAN_RESON_PHOTO_CAPTION = "视频标题命中关键词";
    public static final String WORDFILTER_BAN_RESON_USER_TEXT = "个人简介命中关键词";

    //用于专门的探测程序使用，不上报perf log
    public static TextFilterResponse getTextActionLocallyForTest(String input, TextScanType type) {
        return new TextFilterResponseImpl(getMatchResult(input), input, type.getType());
    }

    public static TextFilterResponse getTextActionLocally(String input, TextScanType type) {
        //原来的先直接默认为read了
        return getTextActionForRead(input, type);
    }

    //用于读时敏感词过滤
    public static TextFilterResponse getTextActionForRead(String input, TextScanType type) {
        //禁用词
        TextFilterResponse response = getTextActionForbidden(getForbiddenMatchResult(input), input);
        if (response.getAction() <= WORDFILTER_NOT_HIT) {
            response = new TextFilterResponseImpl(getMatchResult(input), input,
                    type.getType());
        }

        perf("antispam.text.sensitive", type.name(), response.getAction(), "read").logstash();
        return response;
    }

    //用于写时敏感词过滤
    public static TextFilterResponse getTextActionForWrite(String input, TextScanType type) {
        //禁用词
        TextFilterResponse response = getTextActionForbidden(getForbiddenMatchResult(input), input);
        String bizName = FORBIDDEN_SENSITIVE_WORDS.bizName();
        // 记log
        saveSensitiveWordFilterInfo(input, bizName, type,
                response, ModeType.WRITE);
        if (response.getAction() <= WORDFILTER_NOT_HIT) {
            response = new TextFilterResponseImpl(getMatchResult(input), input,
                    type.getType());
            bizName = type.getBizType().bizName();
            // 记log
            saveSensitiveWordFilterInfo(input, bizName, type,
                    response, ModeType.WRITE);
        }
        perf("antispam.text.sensitive", type.name(), response.getAction(), "write").logstash();
        return response;
    }

    public static List<MatchResult> getMatchResult(String input) {
        List<MatchResult> results = catching(() -> getMatchResultNew(input));
        return firstNonNull(results, emptyList());
    }

    public static List<MatchResult> getMatchResultNew(String input) {
        return SensitiveMatcherHolder.of(KUAISHOU_APP_SENSITIVE_WORDS).get() //
                .scanWithBoundary(input) //
                .stream() //
                .map(it -> new MatchResult(it.getPosition(), it.getLength(), 0, it.getCategory(),
                        it.getMatchedText(), it.getKeyword())) //
                .collect(toList());
    }

    // 后面会在getTextActionForRead、getTextActionForWrite强制加禁用词检测
    private static TextFilterResponse getTextActionForbidden (List<MatchResult> results, String input) {
        TextFilterResponse response = new TextFilterResponseImpl(results, input);
        return response;
    }

    // isSensitive强制加禁用词检测
    private static List<MatchResult> getMatchResultForbidden(String input) {
        List<MatchResult> results = catching(() -> getForbiddenMatchResult(input));
        return firstNonNull(results, emptyList());
    }

    private static List<MatchResult> getForbiddenMatchResult(String input) {
        return SensitiveMatcherHolder.of(FORBIDDEN_SENSITIVE_WORDS).get() //
                .scanWithBoundary(input) //
                .stream() //
                .map(it -> new MatchResult(it.getPosition(), it.getLength(), 0, it.getCategory(),
                        it.getMatchedText(), it.getKeyword())) //
                .collect(toList());
    }

    public static boolean isSafeContent(String text, TextScanType type) {
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        TextFilterResponse response = getTextActionLocally(convertByTrie(text), type);
        return response.getAction() <= WORDFILTER_NOT_HIT;
    }

    //0.根据TextScanType取的敏感词库，不和其他一样用的KUAISHOU_APP_SENSITIVE_WORDS
    //1.用的scanWithBoundary，用之前跟蒯炜确认好
    //2.这里不需要根据MatchResult做进一步处理
    public static boolean isSensitive(String word, TextScanType type) {
        if (StringUtils.isEmpty(word)) {
            return false;
        }
        // 禁用词
        List<MatchResult> resultListForbidden = getMatchResultForbidden(word);
        boolean hit = !resultListForbidden.isEmpty();
        if (!hit) {
            hit = !SensitiveMatcherHolder.of(type.getBizType()).get().scanWithBoundary(word)
                    .isEmpty();
        }
        perf("antispam.text.sensitive.hit", type.name(), hit).logstash();
        return hit;
    }

    // 获取原始的 json 结果
    //text 增加表情等字符转化
    @SuppressWarnings("unchecked")
    public static JSONArray textScan(String text) {
        JSONArray rst = new JSONArray();
        AlgosHolder.getInstance().get().scan(text).forEach(matchResult -> {
            JSONObject object = new JSONObject();
            object.put("len", matchResult.getLen());
            object.put("pos", matchResult.getPos());
            object.put("matched_text", matchResult.getMatchedText());
            object.put("m_line", matchResult.getMline());
            object.put("category", matchResult.getCategory());
            object.put("keyword", matchResult.getKeyword());
            rst.add(object);
        });
        return rst;
    }

    private static void saveSensitiveWordFilterInfo(String input,
                                                    String bizName,
                                                    TextScanType type,
                                                    TextFilterResponse response,
                                                    ModeType modeType) {
        SensitiveWordFilterInfo info = genSensitiveWordFilterInfo(input, bizName, type, response, modeType);
        kafkaLogProducerHelper.send(SENSITIVE_WORD_FILTER_LOG, info);
    }

    private static SensitiveWordFilterInfo genSensitiveWordFilterInfo(String input,
                                                                      String bizName,
                                                                      TextScanType type,
                                                                      TextFilterResponse response,
                                                                      ModeType modeType) {
        List<String> keywords = new ArrayList();
        if (response.getAction() > WORDFILTER_NOT_HIT) {
            keywords = response.getMatchText().stream().map(MatchText::getWord).collect(toList());
        }

        SensitiveWordFilterInfo info = SensitiveWordFilterInfo.newBuilder()
                .setInputText(input)
                .setBizName(bizName)
                .setTextType(type.getValue())
                .setMatcherType(type.getType())
                .setAction(response.getAction())
                .setReplaceText(response.getReplaceString())
                .addAllKeyWord(keywords)
                .setMode(modeType)
                .setTimestamp(currentTimeMillis())
                .build();
        return info;
    }
}