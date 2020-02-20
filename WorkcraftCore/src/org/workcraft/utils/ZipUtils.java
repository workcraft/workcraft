package org.workcraft.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static InputStream getUncompressedEntry(String name, InputStream zippedData) throws IOException {
        ZipInputStream zis = new ZipInputStream(zippedData, StandardCharsets.UTF_8);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().equals(name)) {
                return zis;
            }
            zis.closeEntry();
        }
        zis.close();
        return null;
    }


    public static String unzipInputStream(ZipInputStream zis) {
        String result = "";
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                StringBuilder isb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                String line = "=== " + ze.getName() + " ===";
                while (line != null) {
                    isb.append(line);
                    isb.append('\n');
                    line = br.readLine();
                }
                result += isb.toString();
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
