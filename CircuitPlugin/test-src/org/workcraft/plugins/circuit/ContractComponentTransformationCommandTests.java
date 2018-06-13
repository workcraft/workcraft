package org.workcraft.plugins.circuit;

import java.net.URL;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ContractComponentTransformationCommand;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ContractComponentTransformationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testVmeContractComponentTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testContractComponentTransformationCommands(workName);
    }

    private void testContractComponentTransformationCommands(String workName)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);

        HashSet<VisualFunctionComponent> trivialGates = getTrivialGates(circuit);

        int srcTrivialGateCount = trivialGates.size();
        int srcGateCount = circuit.getVisualFunctionComponents().size();
        circuit.selectNone();
        for (VisualFunctionComponent gate: trivialGates) {
            circuit.addToSelection(gate);
        }

        ContractComponentTransformationCommand contractCommand = new ContractComponentTransformationCommand();
        contractCommand.execute(we);

        int dstGateCount = circuit.getVisualFunctionComponents().size();

        Assert.assertEquals(srcGateCount - srcTrivialGateCount, dstGateCount);

        framework.closeWork(we);
    }

    private HashSet<VisualFunctionComponent> getTrivialGates(VisualCircuit circuit) {
        HashSet<VisualFunctionComponent> result = new HashSet<>();
        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            if (component.isBuffer() || component.isInverter()) {
                result.add(component);
            }
        }
        return result;
    }

}
