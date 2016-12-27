package org.workcraft.formula.sat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ProcessIO {
    public static String runViaStreams(String input, String[] process) {
        Process limboole;
        try {
            writeFile(input, File.createTempFile("stream", "in"));

            limboole = Runtime.getRuntime().exec(process);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(limboole.getOutputStream()));
            writer.write(input);
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(limboole.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }

            String output = result.toString();
            writeFile(output, File.createTempFile("stream", "out"));
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String minisat(String minisatPath, String input) {
        String result;
        try {
            File inputFile = File.createTempFile("minisat", ".in");
            File outputFile = File.createTempFile("minisat", ".out");

            writeFile(input, inputFile);

            Process minisat = Runtime.getRuntime().exec(new String[]{minisatPath, inputFile.getAbsolutePath(), outputFile.getAbsolutePath()});
            minisat.getOutputStream().close();
            while (true) {
                int r = minisat.getInputStream().read();
                if (r == -1) {
                    break;
                }
            }
            minisat.getInputStream().close();
            try {
                minisat.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            result = readFile(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    public static String readFile(File file)
            throws FileNotFoundException, IOException {
        String result;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        reader.close();
        result = sb.toString();
        return result;
    }

    private static void writeFile(String text, File file)
            throws FileNotFoundException, IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        writer.write(text);
        writer.close();
    }

}
