import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Trie {

    public static class Node {
        HashMap<Integer, Node> children = new HashMap<>();
        String text;
//        Node[] children = new Node[26];
        public boolean isWord;
        Node() {
            isWord = false;
            text = "";
            for (int i = 0; i < 26; i++) {
                children.put(i, null);
            }
        }
    }

    static Node root;

    static void insert(String word) {
        int index;

        Node current = root;

        for(int i = 0; i < word.length(); i++) {
            // Note the word.charAt(i) - 'a' makes life easier by shifting to 0-26 index
            index = word.charAt(i) - 'a';
            if(current.children.get(index) == null) {
                current.children.put(index, new Node());
            }
            current = current.children.get(index);
//            if(current.children[index] == null) {
//                current.children[index] = new Node();
//            }
//            current = current.children[index];
        }
        current.isWord = true;
//        current.isWord = true;
    }

    static boolean inTrie(String query) {
        int index;
        Node current = root;

        for (int i = 0; i < query.length(); i++) {
            index = query.charAt(i) - 'a';
            if (current.children.get(index) == null) {
                return false;
            }
            current = current.children.get(index);
        }
        return true;
    }

    public static void main(String args[]) {
        // Input keys (use only 'a' through 'z' and lower case)
        String keys[] = {"the", "a", "there", "answer", "any",
                "by", "bye", "theirin"};

        String output[] = {"Not present in trie", "Present in trie"};


        root = new Node();

        // Construct trie
        int i;
        for (i = 0; i < keys.length ; i++)
            insert(keys[i]);

        // Search for different keys
        if(inTrie("the") == true)
            System.out.println("the --- " + output[1]);
        else System.out.println("the --- " + output[0]);

        if(inTrie("these") == true)
            System.out.println("these --- " + output[1]);
        else System.out.println("these --- " + output[0]);

        if(inTrie("their") == true)
            System.out.println("their --- " + output[1]);
        else System.out.println("their --- " + output[0]);

        if(inTrie("thaw") == true)
            System.out.println("thaw --- " + output[1]);
        else System.out.println("thaw --- " + output[0]);

    }

}
