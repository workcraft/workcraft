package org.workcraft.plugins.circuit.genlib;

import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class LibraryManager {

    private static Library library;
    private static String libraryFileName = "";

    private LibraryManager() {
    }

    public static Library getLibrary() {
        String fileName = CircuitSettings.getGateLibrary();
        if (needsReload(fileName)) {
            loadLibrary(fileName);
        }
        return library;
    }

    private static boolean needsReload(String fileName) {
        return !libraryFileName.equals(fileName);
    }

    private static Library loadLibrary(String fileName) {
        if (fileName.isEmpty()) {
            LogUtils.logWarning("Gate library is not specified.");
            library = new Library();
            libraryFileName = fileName;
        } else {
            File file = new File(fileName);
            if (FileUtils.checkAvailability(file, "Gate library access error", false)) {
                try {
                    InputStream genlibInputStream = new FileInputStream(file);
                    GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                    if (DebugCommonSettings.getParserTracing()) {
                        genlibParser.enable_tracing();
                    } else {
                        genlibParser.disable_tracing();
                    }
                    LogUtils.logInfo("Reading gate library '" + file.getAbsolutePath() + "'.");
                    library = genlibParser.parseGenlib();
                    libraryFileName = fileName;
                } catch (FileNotFoundException e) {
                } catch (org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                    LogUtils.logWarning("Could not parse the gate library '" + fileName + "'.");
                }
            }
        }
        return library;
    }

}
