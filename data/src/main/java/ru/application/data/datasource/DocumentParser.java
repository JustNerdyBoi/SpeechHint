package ru.application.data.datasource;

import android.util.Log;

import ru.application.data.utils.ExtensionReceiver;
import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;

import java.io.*;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.xwpf.usermodel.*;
//import org.odftoolkit.simple.TextDocument;
//import org.odftoolkit.simple.text.Paragraph;
//import org.odftoolkit.simple.text.Text;
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

//    public static Document parseOdt(InputStream is) throws IOException {
//        LinkedList<Word> words = new LinkedList<>();
//
//        try {
//            TextDocument document = TextDocument.loadDocument(is);
//            List<Paragraph> paragraphs = document.getParagraphs();
//            boolean firstPara = true;
//            for (Paragraph paragraph : paragraphs) {
//                if (!firstPara) {
//                    words.add(new Word("\n"));
//                }
//                firstPara = false;
//                List<Text> texts = paragraph.getTexts();
//                for (Text text : texts) {
//                    for (String wordStr : text.getStringValue().split("\\s+")) {
//                        if (!wordStr.isEmpty()) {
//                            words.add(new Word(wordStr));
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            throw new IOException("Ошибка при разборе ODT файла", e);
//        }
//        Document doc = new Document();
//        doc.setWords(words);
//        return doc;
//    }

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
