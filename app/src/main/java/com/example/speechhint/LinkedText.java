package com.example.speechhint;

import java.util.ArrayList;
import java.util.List;

public class LinkedText {
    private Node head;
    private Node tail;
    private int size;

    private static class Node {
        String word;
        Node next;
        Node prev;

        Node(String word) {
            this.word = word;
            this.next = null;
            this.prev = null;
        }
    }

    public LinkedText() {
        head = null;
        tail = null;
        size = 0;
    }

    // Add a new word to the end of the list
    public void addWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }

        Node newNode = new Node(word.trim());
        
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

    // Get word by index
    public String getWord(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        if (index < size / 2) {
            // Start from head if index is in first half
            Node current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current.word;
        } else {
            // Start from tail if index is in second half
            Node current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
            return current.word;
        }
    }

    // Search for words that match the filter
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

    // Get the size of the storage
    public int size() {
        return size;
    }

    // Clear all words
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    // Interface for word filtering
    public interface WordFilter {
        boolean matches(String word);
    }

    public static class Filters {
        // Filter for exact match
        public static WordFilter exactMatch(String target) {
            return word -> word.equals(target);
        }

        // Filter for case-insensitive match
        public static WordFilter caseInsensitiveMatch(String target) {
            return word -> word.equalsIgnoreCase(target);
        }

        // Filter for words containing a substring
        public static WordFilter contains(String substring) {
            return word -> word.contains(substring);
        }

        // Filter for words matching a regex pattern
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

        // TODO: Temporary solution
        public String getBufferString() {
            if (current == null) return "";
            
            StringBuilder buffer = new StringBuilder();
            
            // Add words from before buffer
            Node temp = current;
            int count = 0;
            while (temp != null && count < beforeSize) {
                temp = temp.prev;
                if (temp != null) {
                    buffer.insert(0, temp.word + " ");
                }
                count++;
            }
            
            // Add current word
            buffer.append(current.word);
            
            // Add words from after buffer
            temp = current;
            count = 0;
            while (temp != null && count < afterSize) {
                temp = temp.next;
                if (temp != null) {
                    buffer.append(" ").append(temp.word);
                }
                count++;
            }
            
            return buffer.toString();
        }

        /**
         * Searches for a word in the current buffers and moves to it if found.
         * If not found in buffers and searchFullText is true, searches the entire text.
         * returns true if the word was found and position was moved, false if wasn'tK
         */
        public boolean searchAndMoveToWord(String word, WordFilter filter, boolean searchFullText) {
            if (word == null || word.trim().isEmpty() || current == null) {
                return false;
            }

            String searchWord = word.trim();

            // First check if the current word matches
            if (filter.matches(current.word)) {
                return true;
            }

            // Search in before buffer
            Node temp = current;
            int stepsBack = 0;
            while (temp != null && stepsBack < beforeSize) {
                temp = temp.prev;
                if (temp != null && filter.matches(temp.word)) {
                    current = temp;
                    return true;
                }
                stepsBack++;
            }

            // Search in after buffer
            temp = current;
            int stepsForward = 0;
            while (temp != null && stepsForward < afterSize) {
                temp = temp.next;
                if (temp != null && filter.matches(temp.word)) {
                    current = temp;
                    return true;
                }
                stepsForward++;
            }

            // If not found in buffers and searchFullText is true, search the entire text
            if (searchFullText) {
                temp = head;
                while (temp != null) {
                    if (filter.matches(temp.word)) {
                        current = temp;
                        return true;
                    }
                    temp = temp.next;
                }
            }

            return false;
        }
    }
} 