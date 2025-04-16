package datastruct.dfa;
/*
 *项目名: sensitive_word_processing
 *文件名: DFA
 *创建者: YANGTIAN
 *创建时间:2025/4/16 11:33
 *描述: 运用DFA算法进行敏感词匹配

 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DeterministicFiniteAutomaton {

    // DFA节点定义
    private static class DfaNode {
        Map<Character, DfaNode> children = new HashMap<>();
        boolean isEnd;
    }

    private final DfaNode root = new DfaNode(); // DFA根节点
    private final Set<String> sensitiveWords = new HashSet<>(); // 敏感词库

    // 通过文件初始化DFA
    public DeterministicFiniteAutomaton(String filePath) {
        buildDFA(readKeywordsFromFile(filePath));
    }

    // 从文件读取关键词
    private List<String> readKeywordsFromFile(String filePath) {
        List<String> keywords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    keywords.add(trimmed);
                    sensitiveWords.add(trimmed);
                }
            }
        } catch (IOException e) {
            System.err.println("读取敏感词文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        return keywords;
    }

    // 构建DFA树
    private void buildDFA(List<String> keywords) {
        for (String word : keywords) {
            DfaNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                current = current.children.computeIfAbsent(ch, k -> new DfaNode());
            }
            current.isEnd = true;
        }
    }

    // 执行过滤（返回所有匹配的敏感词及其位置）
    public Map<String, List<Integer>> filter(String text) {
        Map<String, List<Integer>> result = new HashMap<>();
        for (String word : sensitiveWords) {
            result.put(word, new ArrayList<>());
        }

        for (int i = 0; i < text.length(); i++) {
            DfaNode current = root;
            int j = i;
            while (j < text.length()) {
                current = current.children.get(text.charAt(j));
                if (current == null) break;

                if (current.isEnd) {
                    String foundWord = text.substring(i, j + 1);
                    if (sensitiveWords.contains(foundWord)) {
                        result.get(foundWord).add(i);
                        // 找到后立即跳出，避免包含更短的词
                        break;
                    }
                }
                j++;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String keywordsFile = "E:\\Code\\J_project\\sensitive_word_processing\\src\\test\\java\\datastruct\\sensitive_words.txt";

        // 初始化过滤器
        DeterministicFiniteAutomaton filter = new DeterministicFiniteAutomaton(keywordsFile);

        // 测试文本
        String text = "新疆骚乱，苹果发布会停止了吗，饭菜涨价了吗";

        // 执行过滤
        Map<String, List<Integer>> result = filter.filter(text);

        // 显示结果
        System.out.println("=== 敏感词检测结果 ===");
        System.out.println("原始文本：" + text);

        for (Map.Entry<String, List<Integer>> entry : result.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.printf("敏感词 [%-4s] 出现位置：%s%n",
                        entry.getKey(),
                        formatPositions(text, entry.getKey(), entry.getValue()));
            }
        }

        System.out.printf("耗时：%dms%n", System.currentTimeMillis() - start);
    }

    // 格式化位置信息
    private static String formatPositions(String text, String word, List<Integer> positions) {
        List<String> outputs = new ArrayList<>();
        for (int pos : positions) {
            outputs.add(pos + ":" + text.substring(pos, pos + word.length()));
        }
        return String.join(", ", outputs);
    }
}