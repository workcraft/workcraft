package org.workcraft.plugins.circuit;

import java.net.URL;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ContractJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.DetachJointTransformationCommand;
import org.workcraft.plugins.circuit.commands.DissolveJointTransformationCommand;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

class JointTransformationCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testVmeJointTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testJointTransformationCommands(workName, new String[] {"lds"});
    }

    private void testJointTransformationCommands(String workName, String[] portRefs)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        int srcForkCount = countForks(circuit);

        DissolveJointTransformationCommand command = new DissolveJointTransformationCommand();
        command.execute(we);

        Assertions.assertEquals(0, circuit.getJoints().size());

        DetachJointTransformationCommand detachCommand = new DetachJointTransformationCommand();
        detachCommand.execute(we);

        Assertions.assertEquals(srcForkCount, circuit.getJoints().size());

        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        for (String portRef: portRefs) {
            VisualContact port = visualCircuit.getVisualComponentByMathReference(portRef, VisualContact.class);
            if (port != null) {
                visualCircuit.remove(port);
            }
        }

        int redundantJointCount = countRedundantJoints(circuit);

        ContractJointTransformationCommand contractCommand = new ContractJointTransformationCommand();
        contractCommand.execute(we);

        Assertions.assertEquals(srcForkCount, circuit.getJoints().size() + redundantJointCount);

        framework.closeWork(we);
    }

    private int countForks(Circuit circuit) {
        int result = 0;
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            for (FunctionContact contact: component.getFunctionOutputs()) {
                Collection<Contact> driven = CircuitUtils.findDriven(circuit, contact, false);
                if ((driven != null) && (driven.size() > 1)) {
                    result++;
                }
            }
        }
        for (Contact input: circuit.getInputPorts()) {
            Collection<Contact> driven = CircuitUtils.findDriven(circuit, input, false);
            if ((driven != null) && (driven.size() > 1)) {
                result++;
            }
        }
        return result;
    }


    private int countRedundantJoints(Circuit circuit) {
        int result = 0;
        for (Joint joint: circuit.getJoints()) {
            if ((circuit.getPreset(joint).size() < 2) && (circuit.getPostset(joint).size() < 2)) {
                result++;
            }
        }
        return result;
    }

}
