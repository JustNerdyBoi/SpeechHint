package com.example.speechhint.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinkedText implements Serializable {
    private static final long serialVersionUID = 1L;
    private Node head;
    private Node tail;
    private int size;

    public static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        final String word;
        Node next;
        Node prev;

        Node(String word) {
            this.word = word;
        }
    }

    public LinkedText() {
        clear();
    }

    public Node getHead() {
        return head;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public void addWord(String word) {
        if (word == null) {
            return;
        }

        String wordToAdd = processWord(word);
        if (wordToAdd == null) {
            return;
        }

        Node newNode = new Node(wordToAdd);
        addNode(newNode);
    }

    private String processWord(String word) {
        if (word.equals("\n")) {
            return "\n";
        }
        
        String trimmed = word.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        return trimmed;
    }

    private void addNode(Node newNode) {
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public String getWord(int index) {
        Node node = getNodeAt(index);
        if (node == null) {
            return null;
        }
        return node.word;
    }

    public void addWordAt(String word, int position) {
        if (word == null || position < 0 || position > size) {
            return;
        }

        String wordToAdd = processWord(word);
        if (wordToAdd == null) {
            return;
        }

        Node newNode = new Node(wordToAdd);
        insertNodeAt(newNode, position);
    }

    private void insertNodeAt(Node newNode, int position) {
        if (position == 0) {
            insertAtBeginning(newNode);
        } else if (position == size) {
            insertAtEnd(newNode);
        } else {
            insertInMiddle(newNode, position);
        }
        size++;
    }

    private void insertAtBeginning(Node newNode) {
        newNode.next = head;
        if (head != null) {
            head.prev = newNode;
        }
        head = newNode;
        if (tail == null) {
            tail = newNode;
        }
    }

    private void insertAtEnd(Node newNode) {
        newNode.prev = tail;
        tail.next = newNode;
        tail = newNode;
    }

    private void insertInMiddle(Node newNode, int position) {
        Node current = getNodeAt(position);
        newNode.prev = current.prev;
        newNode.next = current;
        current.prev.next = newNode;
        current.prev = newNode;
    }

    public void removeWordAt(int position) {
        if (position < 0 || position >= size) {
            return;
        }

        Node nodeToRemove = getNodeAt(position);
        removeNode(nodeToRemove);
    }

    private void removeNode(Node nodeToRemove) {
        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next;
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }

        size--;
    }

    private Node getNodeAt(int position) {
        if (position < 0 || position >= size) {
            return null;
        }

        return position < size / 2 ? 
            getNodeFromHead(position) : 
            getNodeFromTail(position);
    }

    private Node getNodeFromHead(int position) {
        Node current = head;
        for (int i = 0; i < position; i++) {
            current = current.next;
        }
        return current;
    }

    private Node getNodeFromTail(int position) {
        Node current = tail;
        for (int i = size - 1; i > position; i--) {
            current = current.prev;
        }
        return current;
    }

    public List<Integer> searchWords(WordFilter filter) {
        List<Integer> matches = new ArrayList<>();
        Node current = head;
        int index = 0;

        while (current != null) {
            if (filter.matches(current.word)) {
                matches.add(index);
            }
            current = current.next;
            index++;
        }

        return matches;
    }

    public interface WordFilter {
        boolean matches(String word);
    }

    public static class Filters {
        public static WordFilter exactMatch(String target) {
            return word -> word.equals(target);
        }

        public static WordFilter caseInsensitiveMatch(String target) {
            return word -> word.equalsIgnoreCase(target);
        }

        public static WordFilter contains(String substring) {
            return word -> word.contains(substring);
        }

        public static WordFilter containsCaseInsensitive(String substring) {
            return word -> word.toLowerCase().contains(substring.toLowerCase());
        }

        public static WordFilter regexMatch(String pattern) {
            return word -> word.matches(pattern);
        }
    }

    public class TextManager {
        private Node current;
        private int beforeSize;
        private int afterSize;

        public TextManager(int beforeSize, int afterSize) {
            this.beforeSize = beforeSize;
            this.afterSize = afterSize;
            this.current = head;
        }

        public void moveSteps(int steps) {
            if (steps >= 0) {
                moveForward(steps);
            } else {
                moveBackward(-steps);
            }
        }

        public void moveForward(int steps) {
            if (current == null || steps <= 0) return;
            for (int i = 0; i < steps && current.next != null; i++) {
                current = current.next;
            }
        }

        public void moveBackward(int steps) {
            if (current == null || steps <= 0) return;
            for (int i = 0; i < steps && current.prev != null; i++) {
                current = current.prev;
            }
        }

        public String getCurrentWord() {
            return current != null ? current.word : null;
        }

        public void setBufferSizes(int beforeSize, int afterSize) {
            this.beforeSize = beforeSize;
            this.afterSize = afterSize;
        }

        public void resetToStart() {
            current = head;
        }

        public void resetToEnd() {
            current = tail;
        }

        public int searchAndMoveToWord(String word, WordFilter filter, boolean searchFullText) {
            if (!isValidSearch(word)) {
                return 0;
            }

            if (filter.matches(current.word)) {
                return 0;
            }

            int result = searchInBuffers(filter);
            if (result != 0 || !searchFullText) {
                return result;
            }

            return searchInFullText(filter);
        }

        private boolean isValidSearch(String word) {
            return word != null && !word.trim().isEmpty() && current != null;
        }

        private int searchInBuffers(WordFilter filter) {
            int forwardResult = searchForward(filter);
            if (forwardResult != 0) {
                return forwardResult;
            }

            return searchBackward(filter);
        }

        private int searchForward(WordFilter filter) {
            Node temp = current;
            int steps = 0;
            while (temp != null && steps < afterSize) {
                temp = temp.next;
                if (temp != null && filter.matches(temp.word)) {
                    current = temp;
                    return steps + 1;
                }
                steps++;
            }
            return 0;
        }

        private int searchBackward(WordFilter filter) {
            Node temp = current;
            int steps = 0;
            while (temp != null && steps < beforeSize) {
                temp = temp.prev;
                if (temp != null && filter.matches(temp.word)) {
                    current = temp;
                    return -steps - 1;
                }
                steps++;
            }
            return 0;
        }

        private int searchInFullText(WordFilter filter) {
            Node temp = head;
            while (temp != null) {
                if (filter.matches(temp.word)) {
                    current = temp;
                    return 0;
                }
                temp = temp.next;
            }
            return 0;
        }
    }
} 