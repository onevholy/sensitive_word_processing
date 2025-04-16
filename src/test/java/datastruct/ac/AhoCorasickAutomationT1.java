package datastruct.ac;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class AhoCorasickAutomationT1 {

    // ... 原有代码保持不变 ...

    // 新增方法：从文件读取敏感词列表
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

    public static void main(String[] args) {
        // 从文件读取敏感词
        String keywordFilePath = "E:\\Code\\J_project\\sensitive_word_processing\\src\\test\\java\\datastruct\\sensitive_words.txt"; // 文件路径按需修改
        List<String> target = readKeywordsFromFile(keywordFilePath);

        // 检查是否成功读取到敏感词
        if (target.isEmpty()) {
            System.out.println("未读取到敏感词，请检查文件路径和内容。");
            return;
        }

        String text = "asasasasassasas"; // 待匹配的文本

        AhoCorasickAutomationEN aca = new AhoCorasickAutomationEN(target);
        HashMap<String, List<Integer>> result = aca.find(text);

        System.out.println("文本内容: " + text);
        System.out.println("匹配结果:");
        for (Entry<String, List<Integer>> entry : result.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}