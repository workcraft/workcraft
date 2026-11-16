package org.workcraft.plugins.circuit.genlib;

import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.plugins.circuit.jj.substitution.ParseException;
import org.workcraft.plugins.circuit.jj.substitution.SubstitutionParser;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LibraryManager {

    private static final String CHECK_GATE_LIBRARY_MESSAGE =
            "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences.";

    private static Library library;
    private static File libraryFile = null;
    private static long libraryModtime = 0;
    private static String libraryParseError = null;

    private static Map<String, SubstitutionRule> importSubstitutionRules = null;
    private static File importSubstitutionRulesFile = null;
    private static long importSubstitutionRulesModtime = 0;
    private static Boolean importSubstitutionRulesInvert = null;

    private static Map<String, SubstitutionRule> exportSubstitutionRules = null;
    private static File exportSubstitutionRulesFile = null;
    private static long exportSubstitutionRulesModtime = 0;
    private static Boolean exportSubstitutionRulesInvert = null;

    private LibraryManager() {
    }

    public static File getCircuitSettingsEvaluatedLibraryFile() {
        return FileUtils.getEvalPathFile(CircuitSettings.getGateLibrary());
    }

    public static String getLibraryAccessErrorOrNull(File file) {
        if (file == null) {
            return "Gate library is not specified.\n" + CHECK_GATE_LIBRARY_MESSAGE;
        }
        if (!FileUtils.checkFileReadability(file)) {
            return "Cannot read gate library file '" + file.getPath() + "'.\n" + CHECK_GATE_LIBRARY_MESSAGE;
        }
        return null;
    }

    // Error message if the current gate library cannot be used, or null if it is fine
    public static String getLibraryLoadErrorOrNull() {
        String error = getLibraryAccessErrorOrNull(getCircuitSettingsEvaluatedLibraryFile());
        if (error == null) {
            // Make sure the library is (re)loaded before checking for parse error
            getLibrary();
            error = libraryParseError;
        }
        return error;
    }

    // Never returns null: an empty library is returned if the gate library cannot be loaded
    public static Library getLibrary() {
        File file = getCircuitSettingsEvaluatedLibraryFile();
        long modtime = FileUtils.getModtimeOrZero(file);
        // Library file is null if gate library is not specified, so check the library itself
        if ((library == null) || !Objects.equals(libraryFile, file) || (modtime != libraryModtime)) {
            library = loadLibrary(file);
            libraryFile = file;
            libraryModtime = modtime;
        }
        return library;
    }

    private static Library loadLibrary(File file) {
        libraryParseError = null;
        if (file == null) {
            LogUtils.logWarning("Gate library is not specified.");
            return new Library();
        }
        if (FileUtils.checkFileReadability(file, "Gate library access error")) {
            try (InputStream genlibInputStream = new FileInputStream(file)) {
                GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                if (DebugCommonSettings.getParserTracing()) {
                    genlibParser.enable_tracing();
                } else {
                    genlibParser.disable_tracing();
                }
                LogUtils.logInfo("Reading gate library '" + file.getAbsolutePath() + "'");
                return genlibParser.parseGenlib();
            } catch (IOException | org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                libraryParseError = "Could not parse gate library '" + file.getPath() + "'.\n"
                        + e.getMessage() + "\n" + CHECK_GATE_LIBRARY_MESSAGE;

                LogUtils.logWarning(libraryParseError);
            }
        }
        return new Library();
    }

    public static Map<String, SubstitutionRule> getImportSubstitutionRules() {
        File file = FileUtils.getEvalPathFile(CircuitSettings.getImportSubstitutionLibrary());
        long modtime = FileUtils.getModtimeOrZero(file);
        boolean inversion = CircuitSettings.getInvertImportSubstitutionRules();
        if ((importSubstitutionRulesFile == null)
                || !importSubstitutionRulesFile.equals(file)
                || (modtime != importSubstitutionRulesModtime)
                || (inversion != importSubstitutionRulesInvert)) {

            importSubstitutionRules = readSubstitutionRules(file, inversion);
            importSubstitutionRulesFile = file;
            importSubstitutionRulesModtime = modtime;
            importSubstitutionRulesInvert = inversion;
        }
        return importSubstitutionRules;
    }

    public static Map<String, SubstitutionRule> getExportSubstitutionRules() {
        File file = FileUtils.getEvalPathFile(CircuitSettings.getExportSubstitutionLibrary());
        long modtime = FileUtils.getModtimeOrZero(file);
        boolean invert = CircuitSettings.getInvertExportSubstitutionRules();
        if ((exportSubstitutionRulesFile == null)
                || !exportSubstitutionRulesFile.equals(file)
                || (modtime != exportSubstitutionRulesModtime)
                || (invert != exportSubstitutionRulesInvert)) {

            exportSubstitutionRules = readSubstitutionRules(file, invert);
            exportSubstitutionRulesFile = file;
            exportSubstitutionRulesModtime = modtime;
            exportSubstitutionRulesInvert = invert;
        }
        return exportSubstitutionRules;
    }

    private static HashMap<String, SubstitutionRule> readSubstitutionRules(File file, boolean invert) {
        HashMap<String, SubstitutionRule> result = new HashMap<>();
        if ((file != null) && FileUtils.checkFileReadability(file, "Access error for the file of substitutions")) {
            try (InputStream substitutionInputStream = new FileInputStream(file)) {
                SubstitutionParser substitutionParser = new SubstitutionParser(substitutionInputStream, invert);
                if (DebugCommonSettings.getParserTracing()) {
                    substitutionParser.enable_tracing();
                } else {
                    substitutionParser.disable_tracing();
                }
                List<SubstitutionRule> rules = substitutionParser.parseSubstitutionRules();
                for (SubstitutionRule rule : rules) {
                    result.put(rule.oldName, rule);
                }
                LogUtils.logInfo("Renaming gates and pins using "
                        + (invert ? "inverted" : "direct")
                        + " rules in the file of substitutions '"
                        + file.getPath() + "'");

            } catch (IOException | ParseException e) {
                LogUtils.logWarning("Could not parse the file of substitutions '" + file.getPath() + "'\n"
                        + e.getMessage());
            }
        }
        return result;
    }

}
