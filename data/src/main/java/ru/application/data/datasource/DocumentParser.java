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
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.nio.charset.StandardCharsets;

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
                return parseOdt(new ByteArrayInputStream(data));
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    public static Document parseOdt(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        ZipInputStream zipIn = new ZipInputStream(is);
        ZipEntry entry = null;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();

        try {
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().equals("content.xml")) {
                    int len;
                    while ((len = zipIn.read(buffer)) != -1) {
                        xmlOut.write(buffer, 0, len);
                    }
                    break;
                }
            }
        } finally {
            zipIn.close();
        }

        if (xmlOut.size() == 0) {
            throw new IOException("content.xml not found in ODT");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // ODT использует пространства имён
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream xmlStream = new ByteArrayInputStream(xmlOut.toByteArray());
            org.w3c.dom.Document xmlDoc = builder.parse(xmlStream);

            NodeList paragraphs = xmlDoc.getElementsByTagNameNS("*", "p");
            for (int i = 0; i < paragraphs.getLength(); i++) {
                Node p = paragraphs.item(i);
                extractTextWithLineBreaks(p, words);

                // добавляем перенос строки после параграфа
                words.add(new Word("\n"));
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse content.xml", e);
        }

        Document doc = new Document();
        doc.setWords(words);
        return doc;
    }

    private static void extractTextWithLineBreaks(Node node, LinkedList<Word> words) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String[] splitWords = child.getTextContent().split("\\s+");
                for (String wordStr : splitWords) {
                    if (!wordStr.isEmpty()) {
                        words.add(new Word(wordStr));
                    }
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                if (el.getLocalName().equals("line-break")) {
                    words.add(new Word("\n"));
                } else {
                    extractTextWithLineBreaks(child, words); // рекурсия
                }
            }
        }
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
    public static Document parseDocx(InputStream is) throws IOException {
        LinkedList<Word> words = new LinkedList<>();
        XWPFDocument docx = new XWPFDocument(is);
        boolean firstPara = true;

        for (XWPFParagraph para : docx.getParagraphs()) {
            if (!firstPara) {
                words.add(new Word("\n"));
            }
            firstPara = false;

            for (XWPFRun run : para.getRuns()) {
                String runText = run.getText(0);
                if (runText == null) continue;

                int breaks = run.getCTR().getBrList().size();
                for (int i = 0; i < breaks; i++) {
                    words.add(new Word("\n"));
                }

                // Разбиваем по \n внутри run
                String[] parts = runText.split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (!part.isEmpty()) {
                        for (String wordStr : part.split("\\s+")) {
                            if (!wordStr.isEmpty()) {
                                words.add(new Word(wordStr));
                            }
                        }
                    }
                    if (i < parts.length - 1) {
                        words.add(new Word("\n"));
                    }
                }
            }
        }

        docx.close();

        Document doc = new Document();
        doc.setWords(words);
        return doc;
    }
}
