package ru.application.data.utils;

import java.io.IOException;
import java.io.InputStream;

public class ExtensionReceiver {
    private static final int HEADER_SIZE = 64;
    
    public static String getExtensionFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        byte[] header = new byte[HEADER_SIZE];
        int bytesRead = inputStream.read(header, 0, HEADER_SIZE);

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
               (header[2] == 0x07 && header[3] == 0x08));
    }
    
    private static boolean isOdtFile(byte[] header, int bytesRead) {
        String headerStr = new String(header, 0, bytesRead);
        return headerStr.contains("vnd.oasis.opendocument.text");
    }
}
