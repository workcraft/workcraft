package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.RefinementStatisticsCommand;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.TextUtils;
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
                "Refinement analysis:"
                        + TextUtils.getBulletpoint("Top-level Circuit:")
                        + "\n    buffer.circuit.work"
                        + TextUtils.getBulletpoint("Top-level environment:")
                        + "\n    buffer-compact.stg.work"
                        + TextUtils.getBulletpoint("No Circuit dependencies")
                        + TextUtils.getBulletpoint("No STG dependencies")
                        + TextUtils.getBulletpoint("No invalid dependencies")
                        + TextUtils.getBulletpoint("No additional STG environments"));
    }

    @Test
    void testInvalidDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-invalid/top.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:"
                        + TextUtils.getBulletpoint("Top-level Circuit:")
                        + "\n    rdg-invalid/top.circuit.work"
                        + TextUtils.getBulletpoint("No top-level environment")
                        + TextUtils.getBulletpoint("2 Circuit dependencies:")
                        + "\n    rdg-invalid/buf.circuit.work"
                        + "\n    rdg-invalid/mid.circuit.work"
                        + TextUtils.getBulletpoint("STG dependency:")
                        + "\n    rdg-invalid/buf.stg.work"
                        + TextUtils.getBulletpoint("Invalid dependency:")
                        + "\n    rdg-invalid/invalid.stg.work"
                        + TextUtils.getBulletpoint("Additional STG environment:")
                        + "\n    rdg-invalid/mid-env.stg.work");
    }

    @Test
    void testCyclicDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-cyclic/top.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:"
                        + TextUtils.getBulletpoint("Top-level Circuit:")
                        + "\n    rdg-cyclic/top.circuit.work"
                        + TextUtils.getBulletpoint("No top-level environment")
                        + TextUtils.getBulletpoint("Circuit dependency:")
                        + "\n    rdg-cyclic/mid.circuit.work"
                        + TextUtils.getBulletpoint("STG dependency:")
                        + "\n    rdg-cyclic/buf.stg.work"
                        + TextUtils.getBulletpoint("No invalid dependencies")
                        + TextUtils.getBulletpoint("No additional STG environments"));
    }

    @Test
    void testAcyclicDependency() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-acyclic/top.circuit.work");
        testRefinementStatisticsCommand(workName,
                "Refinement analysis:"
                        + TextUtils.getBulletpoint("Top-level Circuit:")
                        + "\n    rdg-acyclic/top.circuit.work"
                        + TextUtils.getBulletpoint("No top-level environment")
                        + TextUtils.getBulletpoint("2 Circuit dependencies:")
                        + "\n    rdg-acyclic/buf.circuit.work"
                        + "\n    rdg-acyclic/mid.circuit.work"
                        + TextUtils.getBulletpoint("STG dependency:")
                        + "\n    rdg-acyclic/buf.stg.work"
                        + TextUtils.getBulletpoint("No invalid dependencies")
                        + TextUtils.getBulletpoint("No additional STG environments"));
    }

    private void testRefinementStatisticsCommand(String workName, String expected)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        String workPath = url.getFile();
        WorkspaceEntry we = framework.loadWork(workPath);

        StringBuilder expectedRefinementStatistics = new StringBuilder();
        Pattern pattern = Pattern.compile("^ {4}(.+\\.work)$");
        for (String line : expected.split("\n")) {
            Matcher matcher  = pattern.matcher(line);
            if (matcher.find()) {
                String refinementName = PackageUtils.getPackagePath(getClass(), matcher.group(1));
                File refinementFile = new File(workPath.replaceFirst(workName + "$", refinementName));
                String refinementPath = FileUtils.getFullPath(refinementFile);
                expectedRefinementStatistics.append("    ").append(refinementPath).append('\n');
            } else {
                expectedRefinementStatistics.append(line).append('\n');
            }
        }

        String refinementStatistics = new RefinementStatisticsCommand().execute(we);
        Assertions.assertEquals(expectedRefinementStatistics.toString(), refinementStatistics);

        framework.closeWork(we);
    }

}
