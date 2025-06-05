package ru.application.data.datasource;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

import java.io.*;
import java.util.LinkedList;
import org.apache.poi.xwpf.usermodel.*;

public class DocumentParser {

    public static Document parse(InputStream is, String extension) throws IOException {
        switch (extension) {
            case "txt":
                return parseTxt(is);
            case "docx":
                return parseDocx(is);
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    // For TXT: treat each line as a paragraph, and insert \n as a separate Word after each line
    public static Document parseTxt(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        int pos = 0;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (!firstLine) {
                // Add \n as a Word at the end of the previous paragraph (line)
                Word newline = new Word();
                newline.setText("\n");
                newline.setPosition(pos++);
                words.add(newline);
            }
            firstLine = false;

            for (String wordStr : line.split("\\s+")) {
                if (!wordStr.isEmpty()) {
                    Word word = new Word();
                    word.setText(wordStr);
                    word.setPosition(pos++);
                    words.add(word);
                }
            }
        }
        reader.close();
        Document doc = new Document();
        doc.setWords(words);
        return doc;
    }

    // For DOCX: treat each paragraph, insert \n as a Word after each paragraph (even if empty)
    public static Document parseDocx(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        XWPFDocument docx = new XWPFDocument(is);
        int pos = 0;
        boolean firstPara = true;
        for (XWPFParagraph para : docx.getParagraphs()) {
            if (!firstPara) {
                // Add \n as a Word at the end of the previous paragraph
                Word newline = new Word();
                newline.setText("\n");
                newline.setPosition(pos++);
                words.add(newline);
            }
            firstPara = false;

            String text = para.getText();
            for (String wordStr : text.split("\\s+")) {
                if (!wordStr.isEmpty()) {
                    Word word = new Word();
                    word.setText(wordStr);
                    word.setPosition(pos++);
                    words.add(word);
                }
            }
        }
        docx.close();
        Document doc = new Document();
        doc.setWords(words);
        return doc;
    }
}
