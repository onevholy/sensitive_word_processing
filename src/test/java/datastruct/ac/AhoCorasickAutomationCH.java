package datastruct.ac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AhoCorasickAutomationCH {

    /* AC自动机的根结点，根结点不存储任何字符信息 */
    private Node root;

    /* 待查找的目标字符串集合 */
    private List<String> target;

    /* 查找结果，key表示目标字符串，value表示目标字符串在文本中出现的位置列表 */
    private HashMap<String, List<Integer>> result;

    /* AC自动机的结点 */
    private static class Node {
        String str;  // 若该结点是终点，则str保存对应的字符串
        HashMap<Character, Node> table = new HashMap<>();  // 子结点
        Node fail;   // 失败指针

        public boolean isWord() {
            return str != null;
        }
    }

    public AhoCorasickAutomationCH(List<String> target) {
        root = new Node();
        this.target = target;
        buildTrieTree();
        buildACAutomation();
        initializeResult();
    }

    /* 构建Trie树 */
    private void buildTrieTree() {
        for (String s : target) {
            Node curr = root;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (!curr.table.containsKey(ch)) {
                    curr.table.put(ch, new Node());
                }
                curr = curr.table.get(ch);
            }
            curr.str = s;
        }
    }

    /* 构建AC自动机失败指针 */
    private void buildACAutomation() {
        LinkedList<Node> queue = new LinkedList<>();

        // 第一层结点的fail指向root
        for (Node child : root.table.values()) {
            child.fail = root;
            queue.add(child);
        }

        // 广度优先遍历构建失败指针
        while (!queue.isEmpty()) {
            Node parent = queue.poll();

            for (Map.Entry<Character, Node> entry : parent.table.entrySet()) {
                Character ch = entry.getKey();
                Node child = entry.getValue();

                // 关键点：通过父结点的fail指针来找到子结点的fail指针
                Node failTo = parent.fail;
                while (failTo != null) {
                    if (failTo.table.containsKey(ch)) {
                        child.fail = failTo.table.get(ch);
                        break;
                    }
                    failTo = failTo.fail;
                }
                if (failTo == null) {
                    child.fail = root;
                }

                queue.add(child);
            }
        }
    }

    /* 初始化结果容器 */
    private void initializeResult() {
        result = new HashMap<>();
        for (String s : target) {
            result.put(s, new LinkedList<>());
        }
    }

    /* 在文本中查找所有目标字符串 */
    public HashMap<String, List<Integer>> find(String text) {
        Node curr = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // 沿着失败指针链找到匹配的结点
            while (curr != root && !curr.table.containsKey(ch)) {
                curr = curr.fail;
            }

            if (curr.table.containsKey(ch)) {
                curr = curr.table.get(ch);
            } else {
                curr = root;  // 当前字符不在Trie中，重置到根结点
                continue;
            }

            // 检查当前结点及所有失败指针链上的结点是否是终点
            Node temp = curr;
            while (temp != root) {
                if (temp.isWord()) {
                    String word = temp.str;
                    int pos = i - word.length() + 1;
                    result.get(word).add(pos);
                }
                temp = temp.fail;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> targets = new ArrayList<>();
        targets.add("测试");
        targets.add("试试");
        targets.add("AC");

        String text = "这是一个测试字符串，试试看AC自动机能否正常工作。";

        AhoCorasickAutomationCH ac = new AhoCorasickAutomationCH(targets);
        HashMap<String, List<Integer>> result = ac.find(text);

        System.out.println("文本：" + text);
        for (Entry<String, List<Integer>> entry : result.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}