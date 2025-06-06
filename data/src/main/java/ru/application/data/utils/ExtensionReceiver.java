package ru.application.data.utils;

import java.io.IOException;
import java.io.InputStream;

public class ExtensionReceiver {
    public static String getExtensionFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {throw new IllegalArgumentException("Input stream cannot be null");}
        byte[] header = new byte[4];
        int bytesRead = inputStream.read(header, 0, 4);
        if (bytesRead >= 4) {
            if ((header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) ||  // PK\x03\x04
                (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x05 && header[3] == 0x06) ||  // PK\x05\x06
                (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x07 && header[3] == 0x08)) {  // PK\x07\x08
                return "docx";
            }
            return "txt";
        } else {
            return "txt";
        }
    }
}
