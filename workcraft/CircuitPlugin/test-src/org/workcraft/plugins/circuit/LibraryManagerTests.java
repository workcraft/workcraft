package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.workcraft.Framework;
import org.workcraft.formula.FreeVariable;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.utils.BackendUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class LibraryManagerTests {

    private String originalGateLibrary;

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @BeforeEach
    void saveSettings() {
        originalGateLibrary = CircuitSettings.getGateLibrary();
    }

    @AfterEach
    void restoreSettings() {
        CircuitSettings.setGateLibrary(originalGateLibrary);
    }

    @Test
    void testValidLibraryHasNoLoadError() {
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        Assertions.assertNull(LibraryManager.getLibraryLoadErrorOrNull());
        Assertions.assertFalse(LibraryManager.getLibrary().getNames().isEmpty());
    }

    @Test
    void testBrokenLibraryGivesEmptyLibraryAndError(@TempDir File dir) throws IOException {
        File file = new File(dir, "broken.lib");
        Files.writeString(file.toPath(), "GATE this is not a valid genlib");
        CircuitSettings.setGateLibrary(file.getAbsolutePath());

        Library library = LibraryManager.getLibrary();
        Assertions.assertNotNull(library);
        Assertions.assertTrue(library.getNames().isEmpty());
        Assertions.assertNotNull(LibraryManager.getLibraryLoadErrorOrNull());
        Assertions.assertNull(GenlibUtils.findMapping(new FreeVariable("a"), library));
    }

    @Test
    void testMissingLibraryGivesEmptyLibraryAndError(@TempDir File dir) {
        CircuitSettings.setGateLibrary(new File(dir, "missing.lib").getAbsolutePath());

        Assertions.assertNotNull(LibraryManager.getLibrary());
        Assertions.assertTrue(LibraryManager.getLibrary().getNames().isEmpty());
        Assertions.assertNotNull(LibraryManager.getLibraryLoadErrorOrNull());
    }

}
