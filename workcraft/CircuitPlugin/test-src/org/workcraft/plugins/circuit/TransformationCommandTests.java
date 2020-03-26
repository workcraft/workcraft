package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TransformationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testVmeTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);

        Set<VisualFunctionComponent> optZeroDelays = getZeroDelayGates(circuit);
        VisualFunctionComponent bubble25 = circuit.getVisualComponentByMathReference("IN_BUBBLE25",
                VisualFunctionComponent.class);

        new OptimiseZeroDelayTransformationCommand().execute(we);

        optZeroDelays.removeAll(getZeroDelayGates(circuit));
        Assert.assertEquals(new HashSet<>(Arrays.asList(bubble25)), optZeroDelays);

        circuit.selectAll();

        new PropagateInversionTransformationCommand().execute(we);

        int dstMappedGateCount = 0;
        int dstUnmappedGateCount = 0;
        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            if (component.isMapped()) {
                dstMappedGateCount++;
            } else {
                dstUnmappedGateCount++;
            }
        }

        Assert.assertEquals(12, dstMappedGateCount);
        Assert.assertEquals(9, dstUnmappedGateCount);

        Assert.assertEquals(true, new CombinedVerificationCommand().execute(we));

        // Note that U31.C2 was renamed to U31.C2N after inversion propagation command
        VisualContact contact = circuit.getVisualComponentByMathReference("U31.C2N", VisualContact.class);
        Assert.assertNotNull(contact);

        Set<VisualConnection> connections = circuit.getConnections(contact);
        Assert.assertEquals(1, connections.size());

        circuit.select(connections);
        new InsertBufferTransformationCommand().execute(we);
        Set<VisualFunctionComponent> buffers = getBuffers(circuit);
        Assert.assertEquals(connections.size(), buffers.size());
        Assert.assertEquals(false, new CombinedVerificationCommand().execute(we));

        circuit.select(buffers);
        Set<VisualFunctionComponent> zeroDelaysBefore = getZeroDelayGates(circuit);
        new ToggleZeroDelayTransformationCommand().execute(we);
        Set<VisualFunctionComponent> zeroDelaysAfter = getZeroDelayGates(circuit);
        Assert.assertEquals(zeroDelaysBefore.size() + buffers.size(), zeroDelaysAfter.size());
        Assert.assertEquals(true, new CombinedVerificationCommand().execute(we));

        ToggleBubbleTransformationCommand toggleBubbleCommand = new ToggleBubbleTransformationCommand();
        circuit.select(buffers);
        Set<VisualFunctionComponent> invertersBefore = getInverters(circuit);
        toggleBubbleCommand.execute(we);
        Set<VisualFunctionComponent> invertersAfter = getInverters(circuit);
        Assert.assertEquals(invertersBefore.size() + buffers.size(), invertersAfter.size());
        Assert.assertEquals(false, new CombinedVerificationCommand().execute(we));

        ContractComponentTransformationCommand contractCommand = new ContractComponentTransformationCommand();
        circuit.select(buffers);
        Set<VisualFunctionComponent> trivialsBefore = getTrivialGates(circuit);
        contractCommand.execute(we);
        Set<VisualFunctionComponent> trivialsAfter = getTrivialGates(circuit);
        Assert.assertEquals(trivialsBefore.size() - buffers.size(), trivialsAfter.size());
        Assert.assertEquals(true, new CombinedVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private Set<VisualFunctionComponent> getInverters(VisualCircuit circuit) {
        Set<VisualFunctionComponent>  result = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            if (component.isInverter()) {
                result.add(component);
            }
        }
        return result;
    }

    private Set<VisualFunctionComponent> getBuffers(VisualCircuit circuit) {
        Set<VisualFunctionComponent>  result = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            if (component.isBuffer()) {
                result.add(component);
            }
        }
        return result;
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

    private Set<VisualFunctionComponent> getZeroDelayGates(VisualCircuit circuit) {
        Set<VisualFunctionComponent>  result = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                result.add(component);
            }
        }
        return result;
    }

}
