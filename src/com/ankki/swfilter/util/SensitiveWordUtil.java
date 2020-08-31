package com.ankki.swfilter.util;

import java.util.*;

public class SensitiveWordUtil {
    /**
     * 敏感词树集合
     */
    private static HashMap sensitiveWordMap;

    /**
     * 替换词集合
     */
    private static HashMap<String, String> replaceWordMap;

    /**
     * 初始化敏感词库，构建DFA算法模型
     * @param sensitiveWordSet 敏感词库
     */
    private static synchronized void init(Set<String> sensitiveWordSet) {
        initSensitiveWordMap(sensitiveWordSet);
    }

    /**
     * 初始化敏感词库，构建DFA算法模型
     * @param sensitiveWordSet 敏感词库
     */
    private static void initSensitiveWordMap(Set<String> sensitiveWordSet) {
        // 初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap(sensitiveWordSet.size());
        Map nowMap;
        // 迭代sensitiveWordSet
        for (String key : sensitiveWordSet) {
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                // 转换成char型
                char keyChar = key.charAt(i);
                // 库中获取关键字
                Object wordMap = nowMap.get(keyChar);
                // 如果存在该key，直接赋值，用于下一个循环获取
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    Map<Object, Object> newWordMap = new HashMap<>();
                    // 不是最后一个
                    newWordMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWordMap);
                    nowMap = newWordMap;
                }

                if (i == key.length() - 1) {
                    // 最后一个
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 判断文字是否包含敏感字符
     * @param txt       文字
     * @return 若包含返回true，否则返回false
     */
    private static boolean contains(String txt) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = checkSensitiveWord(txt, i); //判断是否包含敏感字符
            if (matchFlag > 0) {    // 大于0存在，返回true
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：
     * @param txt 文字
     * @param beginIndex 开始索引
     * @return 如果存在，则返回敏感词字符的长度，不存在返回0
     */
    private static int checkSensitiveWord(String txt, int beginIndex) {
        // 敏感词结束标识位
        boolean flag = false;
        // 匹配标识数默认为0
        int matchFlag = 0;
        char word;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            //获取指定key
            nowMap = (Map) nowMap.get(word);
            if (nowMap != null) {// 存在，则判断是否为最后一个
                // 找到相应key，匹配标识+1
                matchFlag++;
                // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                if ("1".equals(nowMap.get("isEnd"))) {
                    // 结束标志位为true
                    flag = true;
                }
            } else {// 不存在，直接返回
                break;
            }
        }
        if (!flag) {
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * 获取文字中的敏感词
     *
     * @param txt       文字
     * @return 敏感词列表
     */
    private static Set<String> getSensitiveWord(String txt) {
        Set<String> sensitiveWordList = new HashSet<>();

        for (int i = 0; i < txt.length(); i++) {
            // 判断是否包含敏感字符
            int length = checkSensitiveWord(txt, i);
            if (length > 0) {// 存在,加入list中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1;// 减1的原因，是因为for会自增
            }
        }

        return sensitiveWordList;
    }

    /**
     * 替换敏感字字符
     *
     * @param txt        文本
     * @return 替换后的字符串
     */
    private static String replaceSensitiveWord(String txt) {
        String resultTxt = txt;
        //获取所有的敏感词
        Set<String> set = getSensitiveWord(txt);
        for (String word : set) {
            String replaceStr = getReplaceStr(word);
            resultTxt = resultTxt.replaceAll(word, replaceStr);
        }
        return resultTxt;
    }

    /**
     * 获取替换字符串
     *
     * @param word 原字符串
     * @return 替换字符串
     */
    private static String getReplaceStr(String word) {
        return replaceWordMap.get(word);
    }

    public static void main(String[] args) {
        replaceWordMap = new HashMap<>();
        replaceWordMap.put("习近平", "习主席");
        replaceWordMap.put("越南", "**");
        replaceWordMap.put("人民网", "媒体");
        replaceWordMap.put("新华社", "媒体");
        replaceWordMap.put("共产党", "执政党");
        replaceWordMap.put("北京", "首都");
        replaceWordMap.put("刘鹤", "财经领导");
        replaceWordMap.put("杨洁篪", "外交领导");
        replaceWordMap.put("冏", "*");
        replaceWordMap.put("词语", "**");
        // 初始化敏感词树
        init(replaceWordMap.keySet());

        System.out.println("敏感词的数量：" + replaceWordMap.size());
        String txt = "冏人民网越南岘港11月10日电 （记者杜尚泽、刘刚）10日，中共中央总书记、国家主席习近平抵达越南岘港，出席亚太经合组织第二十五次领导人非正式会议并对越南社会主义共和国进行国事访问。\n" +
                "\n" +
                "　　当地时间下午1时许，习近平乘坐的专机抵达岘港国际机场。习近平步出舱门，受到越南政府部长等高级官员热情迎接。越南青年向习近平献上鲜花。礼兵分列红地毯两侧。\n" +
                "\n" +
                "　　习近平在机场发表书面讲话，代表中国共产党、中国政府、中国人民，向兄弟的越南共产党、越南政府、越南人民致以诚挚问候和良好祝愿。习近平指出，中越是山水相连的友好邻邦，政治制度相同，发展道路相近，前途命运相关。建交67年来，中越关系内涵不断丰富，双方合作水平不断提升。进入新的历史时期，两国全面战略合作伙伴关系持续深化，给两国人民带来了切实利益，为地区和平、稳定、繁荣作出了重要贡献。两党两国领导人达成的共识正在逐步得到落实，中越战略合作正迈向新的广度和深度。中方高度重视同越南的关系。我期待同阮富仲总书记、陈大光主席等越南党和国家领导同志以及越南各界人士广泛接触，就中越两党两国关系及共同关心的国际和地区问题深入交换意见，巩固传统友谊，规划未来发展，推动中越全面战略合作伙伴关系迈上新台阶。\n" +
                "\n" +
                "　　丁薛祥、刘鹤、杨洁篪等陪同人员同机抵达。\n" +
                "\n" +
                "　　先期抵达出席亚太经合组织第二十五次领导人非正式会议的香港特别行政区行政长官林郑月娥在机场迎候。中国驻越南大使洪小勇也到机场迎接。\n" +
                "\n" +
                "　　在结束出席亚太经合组织第二十五次领导人非正式会议并对越南进行国事访问后，习近平还将对老挝进行国事访问。\n" +
                "\n" +
                "　　新华社北京11月10日电  中共中央总书记、国家主席习近平10日上午乘专机离开北京，应越南社会主义共和国主席陈大光邀请，赴越南岘港出席亚太经合组织第二十五次领导人非正式会议；应越南共产党中央委员会总书记阮富仲、越南社会主义共和国主席陈大光，老挝人民革命党中央委员会总书记、老挝人民民主共和国主席本扬邀请，对越南、老挝进行国事访问。\n" +
                "\n" +
                "　　陪同习近平出访的有：中共中央政治局委员、中央书记处书记、中央办公厅主任丁薛祥，中共中央政治局委员、中央财经领导小组办公室主任刘鹤，中共中央政治局委员、国务委员杨洁篪等。";
        System.out.println("待检测语句字数：" + txt.length());

        long start = System.currentTimeMillis();
        Set<String> sensitiveWord = getSensitiveWord(txt);
        long end = System.currentTimeMillis();
        System.out.println("语句中包含敏感词的个数为：" + sensitiveWord.size() + "。包含：" + sensitiveWord);
        System.out.println("检测耗时：" + (end - start));
        long begin = System.currentTimeMillis();
        String result = replaceSensitiveWord(txt);
        long finish = System.currentTimeMillis();
        System.out.println("替换后的语句为：" + result);
        System.out.println("替换耗时：" + (finish - begin));
    }
}
