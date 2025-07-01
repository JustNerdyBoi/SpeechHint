package ru.application.data.utils;

import android.util.Log;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import ru.application.domain.entity.Document;
import ru.application.domain.entity.Word;


/**
 * <p>
 * The {@code DocumentParser} class provides functionality to parse input streams into a
 * {@link Document} object, extracting words and preserving line breaks.
 * </p>
 * <p>
 * Supported formats:
 * <ul>
 *     <li>Plain text (.txt)</li>
 *     <li>Microsoft Word (.docx)</li>
 *     <li>OpenDocument Text (.odt)</li>
 * </ul>
 * </p>
 */
public class DocumentParser {
    private static final String WORD_SEPARATOR_REGEX = "\\s+";
    private static final String NEW_LINE_REGEX = "\n";
    private static final String NEW_LINE_WORD = "\n";
    private static final Map<String, Function<byte[], ArrayList<Word>>> PARSERS = new HashMap<>();

    static {
        PARSERS.put("txt", exceptionHandler(data -> parseTxt(new ByteArrayInputStream(data))));
        PARSERS.put("docx", exceptionHandler(data -> parseDocx(new ByteArrayInputStream(data))));
        PARSERS.put("odt", exceptionHandler(data -> parseOdt(new ByteArrayInputStream(data))));
    }

    private static Function<byte[], ArrayList<Word>> exceptionHandler(ThrowingFunction<byte[], ArrayList<Word>> parser) {
        return data -> {
            try {
                return parser.apply(data);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing file", e);
            }
        };
    }

    @FunctionalInterface
    private interface ThrowingFunction<InputArgumentType, ResultType> {
        ResultType apply(InputArgumentType inputArgument) throws Exception;
    }


    public static Document parse(InputStream is) throws Exception {
        byte[] data;
        try (is) {
            data = is.readAllBytes();
        }

        String extension = ExtensionReceiver.getExtensionFromInputStream(new ByteArrayInputStream(data));
        Log.i("DocumentParser", "Detected " + extension + " filetype. Starting parsing");

        Function<byte[], ArrayList<Word>> parser = PARSERS.get(extension);

        if (parser == null) {
            throw new IllegalArgumentException("Unsupported file format: " + extension);
        }

        ArrayList<Word> words = parser.apply(data);
        String language = LanguageDetector.detectLanguage(LanguageDetector.generateSample(words));

        return new Document(words, language);
    }

    private static ArrayList<Word> parseDocx(InputStream inputStream) throws IOException {
        ArrayList<Word> words = new ArrayList<>();
        XWPFDocument document = new XWPFDocument(inputStream);

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String[] lines = paragraph.getText().split(NEW_LINE_REGEX); // Find line breaks within a paragraph
            for (String line : lines) {
                String[] textWords = line.split(WORD_SEPARATOR_REGEX);
                for (String textWord : textWords) {
                    if (!textWord.trim().isEmpty()) {
                        words.add(new Word(textWord));
                    }
                }
                words.add(new Word(NEW_LINE_WORD));
            }
        }

        document.close();
        return words;
    }

    private static ArrayList<Word> parseOdt(InputStream inputStream) throws Exception {
        ZipInputStream zis = new ZipInputStream(inputStream);
        org.w3c.dom.Document doc = null;

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if ("content.xml".equals(entry.getName())) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder builder = dbf.newDocumentBuilder();
                doc = builder.parse(zis);
                break;
            }
        }
        if (doc == null) {
            zis.close();
            throw new IllegalArgumentException("content.xml not found in ODT");
        }

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("text".equals(prefix)) {
                    return "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
                }
                return XMLConstants.NULL_NS_URI;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                if ("urn:oasis:names:tc:opendocument:xmlns:text:1.0".equals(namespaceURI)) {
                    return "text";
                }
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        });

        NodeList paragraphs = (NodeList) xpath.evaluate("//text:p", doc, XPathConstants.NODESET);

        ArrayList<Word> words = new ArrayList<>();
        for (int i = 0; i < paragraphs.getLength(); i++) {
            Node paragraph = paragraphs.item(i);
            NodeList lines = paragraph.getChildNodes();
            for (int j = 0; j < lines.getLength(); j++) {
                Node line = lines.item(j);
                String lineText = line.getTextContent();
                for (String word : lineText.split(WORD_SEPARATOR_REGEX)) {
                    if (!word.isEmpty()) words.add(new Word(word));
                }
                words.add(new Word(NEW_LINE_WORD));
            }
        }

        zis.close();

        return words;
    }

    private static ArrayList<Word> parseTxt(InputStream inputStream) throws IOException {
        ArrayList<Word> words = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] textWords = line.split(WORD_SEPARATOR_REGEX);
            for (String textWord : textWords) {
                if (!textWord.trim().isEmpty()) {
                    words.add(new Word(textWord));
                }
            }
            words.add(new Word("\n"));
        }

        reader.close();
        return words;
    }
}
