//  AC自动机算法

package datastruct.ac;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class AhoCorasickFile {

    // 节点定义
    private static class Node {
        String word;          // 终点节点保存的完整单词
        Map<Character, Node> children = new HashMap<>();
        Node fail;             // 失败指针

        boolean isWord() {
            return word != null;
        }
    }

    private final Node root = new Node();
    private final Map<String, List<Integer>> results = new HashMap<>();

    // 通过关键词文件构造AC自动机
    public AhoCorasickFile(String keywordFilePath) throws IOException {
        List<String> keywords = readKeywordsFromFile(keywordFilePath);
        buildTrie(keywords);
        buildFailureLinks();
        initializeResults(keywords);
    }

    // 从文件读取关键词（每行一个关键词）
    private static List<String> readKeywordsFromFile(String filePath) {
        List<String> keywords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    keywords.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("读取敏感词文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        return keywords;
    }

    // 构建Trie树
    private void buildTrie(List<String> keywords) {
        for (String word : keywords) {
            Node current = root;
            for (char ch : word.toCharArray()) {
                current = current.children.computeIfAbsent(ch, k -> new Node());
            }
            current.word = word;
        }
    }

    // 构建失败指针（BFS遍历）
    private void buildFailureLinks() {
        Queue<Node> queue = new LinkedList<>();

        // 第一层节点失败指针指向root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node parent = queue.poll();

            for (Map.Entry<Character, Node> entry : parent.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

                // 关键失败指针逻辑
                Node failTo = parent.fail;
                while (failTo != null && !failTo.children.containsKey(ch)) {
                    failTo = failTo.fail;
                }
                child.fail = (failTo != null) ? failTo.children.get(ch) : root;
                if (child.fail == null) child.fail = root;

                queue.add(child);
            }
        }
    }

    // 初始化结果容器
    private void initializeResults(List<String> keywords) {
        for (String word : keywords) {
            results.put(word, new ArrayList<>());
        }
    }

    // 核心查找算法
    public Map<String, List<Integer>> find(String text) {
        // 重置结果集
        for (List<Integer> positions : results.values()) {
            positions.clear();
        }

        Node current = root;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);

            // 沿着失败指针链找到匹配节点
            while (current != root && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            current = current.children.getOrDefault(ch, root);

            // 检查当前节点及所有失败路径上的节点
            Node temp = current;
            while (temp != root) {
                if (temp.isWord()) {
                    int startPos = index - temp.word.length() + 1;
                    results.get(temp.word).add(startPos);
                }
                temp = temp.fail;
            }
        }
        return results;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            // 文件路径配置（关键词文件）
            String keywordsFile = "E:\\Code\\J_project\\sensitive_word_processing\\src\\test\\java\\datastruct\\sensitive_words.txt";

            // 定义测试文本
            String testText = "这是一个包含敏感词的测试文本，敏感词包括：暴力、色情、赌博等不良信息。";

            // 初始化AC自动机
            AhoCorasickFile ac = new AhoCorasickFile(keywordsFile);

            // 执行查找
            Map<String, List<Integer>> result = ac.find(testText);

            System.out.println("=== 匹配结果 ===");
            System.out.println("测试文本：" + testText);

            for (Entry<String, List<Integer>> entry : result.entrySet()) {
                System.out.printf("敏感词 [%-5s] 出现位置：%s%n",
                        entry.getKey(),
                        formatPositions(testText, entry.getKey(), entry.getValue()));
            }

        } catch (IOException e) {
            System.err.println("文件处理错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.printf("耗时：%dms%n", System.currentTimeMillis() - startTime);
    }

    // 格式化输出结果
    private static String formatPositions(String text, String word, List<Integer> positions) {
        if (positions.isEmpty()) return "未找到";

        List<String> outputs = new ArrayList<>();
        for (int pos : positions) {
            int end = pos + word.length();
            String snippet = (end <= text.length()) ?
                    text.substring(pos, end) : text.substring(pos);
            outputs.add(String.format("%d (%s)", pos, snippet));
        }
        return String.join(", ", outputs);
    }

}