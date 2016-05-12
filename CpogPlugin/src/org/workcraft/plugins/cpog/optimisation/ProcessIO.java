/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.plugins.cpog.optimisation;

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
