package datastruct.ac;/*
 *项目名: sensitive_word_process
 *文件名: AC
 *创建者: YANGTIAN
 *创建时间:2025/4/16 13:42
 *描述: 实现AC自动机

 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class AhoCorasickAutomation {

    private static class Node {
        String word;
        Map<Character, Node> children = new HashMap<>();
        Node fail;

        boolean isWord() {
            return word != null;
        }
    }

    private final Node root = new Node();

    public AhoCorasickAutomation(String keywordFilePath) throws IOException {
        List<String> keywords = readKeywordsFromFile(keywordFilePath);
        buildTrie(keywords);
        buildFailureLinks();
    }

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

    private void buildTrie(List<String> keywords) {
        for (String word : keywords) {
            Node current = root;
            for (char ch : word.toCharArray()) {
                current = current.children.computeIfAbsent(ch, k -> new Node());
            }
            current.word = word;
        }
    }

    private void buildFailureLinks() {
        Queue<Node> queue = new LinkedList<>();
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node parent = queue.poll();
            for (Map.Entry<Character, Node> entry : parent.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();

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

    public Map<String, List<Integer>> find(String text) {
        Map<String, List<Integer>> result = new HashMap<>();

        Node current = root;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);

            while (current != root && !current.children.containsKey(ch)) {
                current = current.fail;
            }
            current = current.children.getOrDefault(ch, root);

            Node temp = current;
            while (temp != root) {
                if (temp.isWord()) {
                    String word = temp.word;
                    int startPos = index - word.length() + 1;

                    // 动态添加结果条目
                    if (!result.containsKey(word)) {
                        result.put(word, new ArrayList<>());
                    }
                    result.get(word).add(startPos);
                }
                temp = temp.fail;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            String keywordsFile = "E:\\Code\\J_project\\sensitive_word_processing\\src\\test\\java\\datastruct\\sensitive_words.txt";
            String testText = "新疆骚乱，苹果发布会停止了吗，饭菜涨价了吗";

            AhoCorasickAutomation ac = new AhoCorasickAutomation(keywordsFile);
            Map<String, List<Integer>> result = ac.find(testText);

            System.out.println("=== AC自动机匹配结果 ===");
            System.out.println("测试文本：" + testText);

            for (Entry<String, List<Integer>> entry : result.entrySet()) {
                System.out.printf("敏感词 [%-4s] 出现位置：%s%n",
                        entry.getKey(),
                        formatPositions(testText, entry.getKey(), entry.getValue()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("耗时：%dms%n", System.currentTimeMillis() - startTime);
    }

    private static String formatPositions(String text, String word, List<Integer> positions) {
        List<String> outputs = new ArrayList<>();
        for (int pos : positions) {
            outputs.add(pos + ":" + text.substring(pos, pos + word.length()));
        }
        return String.join(", ", outputs);
    }
}