package ru.application.data.datasource;

import android.util.Log;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

import java.io.*;
import java.util.LinkedList;
import org.apache.poi.xwpf.usermodel.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DocumentParser {
    public static Document parse(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        byte[] data = baos.toByteArray();

        String extension = ExtensionReceiver.getExtensionFromInputStream(new ByteArrayInputStream(data));
        Log.i("DocumentParser", "Detected " + extension + " filetype. Starting pacing");

        switch (extension) {
            case "txt":
                return parseTxt(new ByteArrayInputStream(data));
            case "docx":
                return parseDocx(new ByteArrayInputStream(data));
            case "odt":
//                return parseOdt(new ByteArrayInputStream(data));
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    public static Document parseOdt(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        ZipInputStream zipIn = new ZipInputStream(is);
        ZipEntry entry;

        try {
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().equals("content.xml")) {
                    // Читаем content.xml
                    String content = readStream(zipIn);
                    // Упрощенная обработка XML (удаляем все теги)
                    String text = content.replaceAll("<[^>]+>", " ");
                    text = text.replaceAll("\\s+", " ").trim();

                    for (String wordStr : text.split("\\s+")) {
                        if (!wordStr.isEmpty()) {
                            words.add(new Word(wordStr));
                        }
                    }
                    break;
                }
            }
        } finally {
            zipIn.close();
        }

        Document doc = new Document();
        doc.setWords(words);
        return doc;
    }

    private static String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    // For TXT: treat each line as a paragraph, and insert \n as a separate Word after each line
    public static Document parseTxt(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (!firstLine) {
                // Add \n as a Word at the end of the previous paragraph (line)
                Word newline = new Word("\n");
                words.add(newline);
            }
            firstLine = false;

            for (String wordStr : line.split("\\s+")) {
                if (!wordStr.isEmpty()) {
                    Word word = new Word(wordStr);
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
    public static Document parseDocx(InputStream is) throws IOException { // TODO: sometimes skips '\n' (compare to loading txt)
        LinkedList<Word> words = new LinkedList<>();
        XWPFDocument docx = new XWPFDocument(is);
        boolean firstPara = true;
        for (XWPFParagraph para : docx.getParagraphs()) {
            if (!firstPara) {
                // Add \n as a Word at the end of the previous paragraph
                Word newline = new Word("\n");
                words.add(newline);
            }
            firstPara = false;

            String text = para.getText();
            for (String wordStr : text.split("\\s+")) {
                if (!wordStr.isEmpty()) {
                    Word word = new Word(wordStr);
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
