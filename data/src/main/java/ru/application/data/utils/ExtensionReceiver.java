package ru.application.data.utils;

import java.io.IOException;
import java.io.InputStream;

public class ExtensionReceiver {
    private static final int HEADER_SIZE = 256;

    public static String getExtensionFromInputStream(InputStream inputStream) throws IOException {  // TODO: FIX THIS F*CKING THING
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        byte[] header = new byte[HEADER_SIZE];
        int bytesRead = inputStream.read(header, 0, HEADER_SIZE);

        if (bytesRead == -1) {
            return "txt"; // Empty file, default to txt
        }

        if (isHtmlSignature(header, bytesRead)) {
            return "html";
        }

        if (isZipSignature(header)) {
            if (isOdtFile(header, bytesRead)) {
                return "odt";
            }
            return "docx";
        }


        return "txt";
    }

    private static boolean isZipSignature(byte[] header) {
        return (header[0] == 0x50 && header[1] == 0x4B &&
               ((header[2] == 0x03 && header[3] == 0x04) ||
               (header[2] == 0x01 && header[3] == 0x02) ||
               (header[2] == 0x05 && header[3] == 0x06) ||
               (header[2] == 0x07 && header[3] == 0x08)));
    }

    private static boolean isOdtFile(byte[] header, int bytesRead) {
        String headerStr = new String(header, 0, bytesRead);
        return headerStr.contains("vnd.oasis.opendocument.text");
    }

     private static boolean isHtmlSignature(byte[] header, int bytesRead) {
        String headerStr = new String(header, 0, bytesRead);
        headerStr = headerStr.toLowerCase(); // Convert to lowercase for case-insensitive matching
        return headerStr.startsWith("<!doctype html") || headerStr.startsWith("<html");
    }
}