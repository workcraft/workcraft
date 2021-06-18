package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.AnonymiseTransformationCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

class AnonymiseCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testBufferTmAnonymiseCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testAnonymiseCommands(workName, new String[]{"in0", "out0", "g0.i0", "g0.o0"});
    }

    private void testAnonymiseCommands(String workName, String[] contactRefs)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        AnonymiseTransformationCommand command = new AnonymiseTransformationCommand();
        command.execute(we);

        HashSet<String> expectedContactRefs = new HashSet<>(Arrays.asList(contactRefs));
        HashSet<String> actualContactRefs = new HashSet<>();
        for (Contact contact : circuit.getFunctionContacts()) {
            actualContactRefs.add(circuit.getNodeReference(contact));
        }
        Assertions.assertEquals(expectedContactRefs, actualContactRefs);

        Assertions.assertEquals("", circuit.getTitle());

        framework.closeWork(we);
    }

}
