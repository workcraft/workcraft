package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.commands.RefinementStatisticsCommand;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RefinementStatisticsCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testBufferDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:\n"
                        + PropertyHelper.BULLET_PREFIX + "Top-level Circuit:\n"
                        + "    buffer.circuit.work\n"
                        + PropertyHelper.BULLET_PREFIX + "Top-level environment:\n"
                        + "    buffer-compact.stg.work\n"
                        + PropertyHelper.BULLET_PREFIX + "No Circuit dependencies\n"
                        + PropertyHelper.BULLET_PREFIX + "No STG dependencies\n"
                        + PropertyHelper.BULLET_PREFIX + "No additional STG environments"
                        + "\n");
    }

    @Test
    void testAcyclicDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-acyclic/top.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:\n"
                        + PropertyHelper.BULLET_PREFIX + "Top-level Circuit:\n"
                        + "    rdg-acyclic/top.circuit.work\n"
                        + PropertyHelper.BULLET_PREFIX + "No top-level environment\n"
                        + PropertyHelper.BULLET_PREFIX + "2 Circuit dependencies:\n"
                        + "    rdg-acyclic/buf.circuit.work\n"
                        + "    rdg-acyclic/mid.circuit.work\n"
                        + PropertyHelper.BULLET_PREFIX + "STG dependencies:\n"
                        + "    rdg-acyclic/buf.stg.work\n"
                        + PropertyHelper.BULLET_PREFIX + "No additional STG environments"
                        + "\n");
    }

    @Test
    void testCyclicDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-cyclic/top.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:\n"
                        + PropertyHelper.BULLET_PREFIX + "Top-level Circuit:\n"
                        + "    rdg-cyclic/top.circuit.work\n"
                        + PropertyHelper.BULLET_PREFIX + "No top-level environment\n"
                        + PropertyHelper.BULLET_PREFIX + "2 Circuit dependencies:\n"
                        + "    rdg-cyclic/buf.circuit.work\n"
                        + "    rdg-cyclic/mid.circuit.work\n"
                        + PropertyHelper.BULLET_PREFIX + "STG dependencies:\n"
                        + "    rdg-cyclic/buf.stg.work\n"
                        + PropertyHelper.BULLET_PREFIX + "No additional STG environments"
                        + "\n");
    }

    private void testRefinementStatisticsCommand(String workName, String expected)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        StringBuilder expectedRefinementStatistics = new StringBuilder();
        Pattern pattern = Pattern.compile("^ {4}(.+\\.work)$");
        for (String line : expected.split("\n")) {
            Matcher matcher  = pattern.matcher(line);
            if (matcher.find()) {
                String name = matcher.group(1);
                String resourceName = PackageUtils.getPackagePath(getClass(), name);
                File file = new File(classLoader.getResource(resourceName).getFile());
                expectedRefinementStatistics.append("    ").append(FileUtils.getFullPath(file)).append("\n");
            } else {
                expectedRefinementStatistics.append(line).append("\n");
            }
        }

        RefinementStatisticsCommand refinementStatisticsCommand = new RefinementStatisticsCommand();
        String refinementStatistics = refinementStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedRefinementStatistics.toString(), refinementStatistics);

        framework.closeWork(we);
    }

}
