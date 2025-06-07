package ru.application.data.datasource;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

import java.io.*;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.xwpf.usermodel.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;

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
        ZipInputStream zipInputStream = new ZipInputStream(is);
        ZipEntry entry;
        int pos = 0;
        boolean firstPara = true;

        try {
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals("content.xml")) {
                    // Парсим content.xml из ODT
                    StringBuilder contentBuilder = new StringBuilder();
                    byte[] contentBuffer = new byte[1024];
                    int contentLen;
                    while ((contentLen = zipInputStream.read(contentBuffer)) > 0) {
                        contentBuilder.append(new String(contentBuffer, 0, contentLen));
                    }

                    // Парсим XML для извлечения текста
                    String xmlContent = contentBuilder.toString();
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        org.w3c.dom.Document xmlDoc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

                        // Получаем все текстовые узлы
                        NodeList textNodes = xmlDoc.getElementsByTagName("text:p"); // Paragraphs in ODT
                        for (int i = 0; i < textNodes.getLength(); i++) {
                            if (!firstPara) {
                                Word newline = new Word();
                                newline.setText("\n");
                                newline.setPosition(pos++);
                                words.add(newline);
                            }
                            firstPara = false;

                            String paraText = textNodes.item(i).getTextContent();
                            for (String wordStr : paraText.split("\\s+")) {
                                if (!wordStr.isEmpty()) {
                                    Word word = new Word();
                                    word.setText(wordStr);
                                    word.setPosition(pos++);
                                    words.add(word);
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new IOException("Failed to parse ODT content", e);
                    }
                }
                zipInputStream.closeEntry();
            }
        } finally {
            zipInputStream.close();
        }

        Document doc = new Document();
        doc.setWords(words);
        return doc;
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
