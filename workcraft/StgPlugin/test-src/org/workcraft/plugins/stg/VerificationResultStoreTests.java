package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.ModelHasher;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.verification.VerificationRecord;
import org.workcraft.verification.VerificationResultStore;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class VerificationResultStoreTests {

    private final AtomicReference<String> exceptions = new AtomicReference<>("");

    private final VerificationGoal plainGoal = new VerificationGoal("test.plain", "Plain goal", we -> true);

    private final VerificationGoal exceptionsGoal = new VerificationGoal("test.exceptions", "Goal with exceptions",
            VerificationGoal.Scope.MODEL, true, we -> true, we -> exceptions.get());

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    private WorkspaceEntry loadVmeWork() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        URL url = ClassLoader.getSystemClassLoader().getResource(workName);
        return Framework.getInstance().loadWork(url.getFile());
    }

    private File getResourceFile(String name) throws URISyntaxException {
        URL url = ClassLoader.getSystemClassLoader().getResource(PackageUtils.getPackagePath(getClass(), name));
        return new File(url.toURI());
    }

    @Test
    void testFileHashIsMathHashOfModelInFile() throws DeserialisationException, URISyntaxException {
        // Work file is hashed in the same way as the open model, i.e. via its math layer
        File workFile = getResourceFile("vme.stg.work");
        WorkspaceEntry we = Framework.getInstance().loadWork(workFile);
        Assertions.assertEquals(ModelHasher.getMathHash(we), ModelHasher.getFileHash(workFile));

        // Missing file is a valid state with its own hash
        Assertions.assertEquals(ModelHasher.NO_FILE_HASH, ModelHasher.getFileHash(null));
        Assertions.assertEquals(ModelHasher.NO_FILE_HASH,
                ModelHasher.getFileHash(new File(workFile.getParentFile(), "missing.stg.work")));

        // Only work files are supported, any other file cannot be hashed
        Assertions.assertNull(ModelHasher.getFileHash(getResourceFile("buffer-name_clash.g")));
        File unreadableFile = FileUtils.createTempFile("verification-cache-test-", ".work");
        FileUtils.deleteOnExitRecursively(unreadableFile);
        Assertions.assertNull(ModelHasher.getFileHash(unreadableFile));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testHashIsStableAndIgnoresVisualLayer()throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        String hash = ModelHasher.getMathHash(we);
        Assertions.assertNotNull(hash);
        Assertions.assertEquals(hash, ModelHasher.getMathHash(we));

        VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
        VisualStgPlace visualPlace = visualStg.getVisualPlaces().iterator().next();
        visualPlace.setX(visualPlace.getX() + 5.0);
        Assertions.assertEquals(hash, ModelHasher.getMathHash(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testHashIgnoresTitle() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        store.put(plainGoal, VerificationRecord.Outcome.PASS, "Test command", "Passed", null);
        String hash = ModelHasher.getMathHash(we);

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        stg.setTitle("Some other title");
        Assertions.assertEquals("Some other title", stg.getTitle());
        Assertions.assertEquals(hash, ModelHasher.getMathHash(we));
        Assertions.assertTrue(store.lookup(plainGoal).isValid());

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testHashChangesWithMathModel()throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        String hash = ModelHasher.getMathHash(we);

        WorkspaceUtils.getAs(we, Stg.class).createPlace("pTest", null);
        Assertions.assertNotEquals(hash, ModelHasher.getMathHash(we));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testHashSurvivesSaveAndLoad() throws DeserialisationException, SerialisationException {
        Framework framework = Framework.getInstance();
        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        WorkspaceEntry we = loadVmeWork();
        String hash = ModelHasher.getMathHash(we);

        File file = new File(directory, "saved.stg.work");
        framework.saveWork(we, file);
        WorkspaceEntry savedWe = framework.loadWork(file);
        Assertions.assertEquals(hash, ModelHasher.getMathHash(savedWe));

        framework.closeWork(savedWe);
        framework.closeWork(we);
    }

    @Test
    void testLookupStatus() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        Assertions.assertEquals(VerificationResultStore.Status.MISSING, store.lookup(plainGoal).getStatus());

        store.put(plainGoal, VerificationRecord.Outcome.PASS, "Test command", "Passed", null);
        VerificationResultStore.Lookup lookup = store.lookup(plainGoal);
        Assertions.assertTrue(lookup.isValid());
        Assertions.assertTrue(lookup.getRecord().isPass());
        Assertions.assertEquals("Passed", lookup.getRecord().message());

        // Visual change keeps the result valid
        VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
        VisualStgPlace visualPlace = visualStg.getVisualPlaces().iterator().next();
        visualPlace.setX(visualPlace.getX() + 5.0);
        Assertions.assertTrue(store.lookup(plainGoal).isValid());

        // Math change makes the result stale
        WorkspaceUtils.getAs(we, Stg.class).createPlace("pTest", null);
        lookup = store.lookup(plainGoal);
        Assertions.assertEquals(VerificationResultStore.Status.STALE, lookup.getStatus());
        Assertions.assertNotNull(lookup.getRecord());
        Assertions.assertTrue(lookup.getReason().contains("Model was changed"));

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testExceptionsInvalidateOnlyTheirGoal() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        exceptions.set("a, b");
        store.put(plainGoal, VerificationRecord.Outcome.PASS, "Test command", "Passed", null);
        store.put(exceptionsGoal, VerificationRecord.Outcome.FAIL, "Test command", "Failed", null);
        Assertions.assertTrue(store.lookup(plainGoal).isValid());
        Assertions.assertTrue(store.lookup(exceptionsGoal).isValid());
        Assertions.assertEquals("a, b", store.getRecord(exceptionsGoal).exceptions());

        exceptions.set("a");
        Assertions.assertTrue(store.lookup(plainGoal).isValid());
        VerificationResultStore.Lookup lookup = store.lookup(exceptionsGoal);
        Assertions.assertEquals(VerificationResultStore.Status.STALE, lookup.getStatus());
        Assertions.assertTrue(lookup.getReason().contains("exceptions"));

        exceptions.set("");
        Framework.getInstance().closeWork(we);
    }

    private static final String REFS = "a3, page1.a1, u2, x1";

    @Test
    void testDependencies() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        store.put(plainGoal, VerificationRecord.Outcome.PASS, "Test command", "Passed", Map.of("", new VerificationRecord.Dependency("env.stg.work", "h1")));

        Assertions.assertEquals(VerificationResultStore.Status.STALE, store.lookup(plainGoal).getStatus());
        Assertions.assertTrue(store.lookup(plainGoal, path -> "h1").isValid());
        Assertions.assertEquals(VerificationResultStore.Status.STALE,
                store.lookup(plainGoal, path -> "h2").getStatus());
        Assertions.assertEquals(VerificationResultStore.Status.STALE,
                store.lookup(plainGoal, path -> null).getStatus());

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testRecordsSurviveSaveAndLoad() throws DeserialisationException, SerialisationException {
        Framework framework = Framework.getInstance();
        File directory = FileUtils.createTempDirectory("verification-cache-test-");
        FileUtils.deleteOnExitRecursively(directory);
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        File dependencyFile = new File(new File(directory, "dependencies"), "env.stg.work");
        exceptions.set("x");
        store.put(exceptionsGoal, VerificationRecord.Outcome.FAIL, "Test command", "Violated <&> \"quoted\"",
                Map.of(REFS, new VerificationRecord.Dependency(dependencyFile.getAbsolutePath(), "h1"),
                        "", new VerificationRecord.Dependency(null, "h1")));

        VerificationRecord record = store.getRecord(exceptionsGoal);
        File file = new File(directory, "saved.stg.work");
        framework.saveWork(we, file);

        // Path to the dependency file is stored relative to the work file
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry("resources/" + VerificationResultStore.KEY);
            String text = new String(zipFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            Assertions.assertTrue(text.matches("(?s).*work-file=\"dependencies[/\\\\]env\\.stg\\.work\".*"), text);
            Assertions.assertTrue(text.contains("refs=\"a3, page1.a1, u2, x1\""), text);
            Assertions.assertTrue(text.contains("refs=\"\""), text);
            Assertions.assertFalse(text.contains(directory.getAbsolutePath()), text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // and it is made absolute again when the work is loaded
        WorkspaceEntry savedWe = framework.loadWork(file);
        VerificationResultStore savedStore = new VerificationResultStore(savedWe);
        VerificationRecord savedRecord = savedStore.getRecord(exceptionsGoal);
        Assertions.assertEquals(record.message(), savedRecord.message());
        Assertions.assertEquals(record.validityHash(), savedRecord.validityHash());
        Assertions.assertEquals(dependencyFile, new File(savedRecord.dependencies().get(REFS).file()));
        Assertions.assertEquals("h1", savedRecord.dependencies().get(REFS).hash());
        Assertions.assertEquals("h1", savedRecord.dependencies().get("").hash());
        Assertions.assertTrue(savedStore.lookup(exceptionsGoal, path -> "h1").isValid());

        exceptions.set("");
        framework.closeWork(savedWe);
        framework.closeWork(we);
    }

    @Test
    void testRemove() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        VerificationResultStore store = new VerificationResultStore(we);
        store.put(plainGoal, VerificationRecord.Outcome.PASS, "Test command", "Passed", null);
        store.remove(plainGoal);
        Assertions.assertEquals(VerificationResultStore.Status.MISSING, store.lookup(plainGoal).getStatus());
        Assertions.assertNull(we.getResource(VerificationResultStore.KEY));

        Framework.getInstance().closeWork(we);
    }

}
