package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.PackageUtils;
import org.workcraft.verification.CachedVerification;
import org.workcraft.verification.VerificationGoal;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.List;

class CachedVerificationMessageTests {

    private static final String NOTE_REGEX = "(These are cached results|This is a cached result) obtained on \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} using %s\\.";

    private final VerificationGoal plainGoal = new VerificationGoal("test.plain", "Plain goal", we -> true);

    private final VerificationGoal exceptionsGoal = new VerificationGoal("test.exceptions", "Goal with exceptions",
            VerificationGoal.Scope.MODEL, true, we -> true, we -> "a\nb");

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    private WorkspaceEntry loadVmeWork() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        URL url = ClassLoader.getSystemClassLoader().getResource(workName);
        return Framework.getInstance().loadWork(url.getFile());
    }

    @Test
    void testSingleGoalMessageIsOriginalMessageAfterHeader() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        CachedVerification.create(we, List.of(exceptionsGoal), "MPSat")
                .store(false, "Goal with exceptions (a, b) is violated.");

        CachedVerification cachedVerification = CachedVerification.create(we, List.of(exceptionsGoal), "MPSat");
        Assertions.assertTrue(cachedVerification.prepare(true, null));
        Assertions.assertFalse(cachedVerification.getReusableOutcome());

        String[] lines = cachedVerification.getReusableMessage().split("\n");
        Assertions.assertEquals(3, lines.length);
        Assertions.assertEquals("Goal with exceptions (a, b) is violated.", lines[0]);
        Assertions.assertEquals("", lines[1]);
        Assertions.assertTrue(lines[2].matches(String.format(NOTE_REGEX, "MPSat")), lines[2]);

        Framework.getInstance().closeWork(we);
    }

    @Test
    void testCombinedMessageListsPassedPropertiesWithExceptions() throws DeserialisationException {
        WorkspaceEntry we = loadVmeWork();
        List<VerificationGoal> goals = List.of(plainGoal, exceptionsGoal);
        CachedVerification.create(we, goals, "Determinator").store(true, null);

        CachedVerification cachedVerification = CachedVerification.create(we, goals, "Determinator");
        Assertions.assertTrue(cachedVerification.prepare(true, null));
        Assertions.assertTrue(cachedVerification.getReusableOutcome());

        String[] lines = cachedVerification.getReusableMessage().split("\n");
        Assertions.assertEquals(5, lines.length);
        Assertions.assertEquals("The following properties have been successfully verified:", lines[0]);
        Assertions.assertTrue(lines[1].endsWith("Plain goal"), lines[1]);
        Assertions.assertTrue(lines[2].endsWith("Goal with exceptions with exceptions (a, b)"), lines[2]);
        Assertions.assertEquals("", lines[3]);
        Assertions.assertTrue(lines[4].matches(String.format(NOTE_REGEX, "Determinator")), lines[4]);

        Framework.getInstance().closeWork(we);
    }

}
