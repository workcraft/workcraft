package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TransformationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PcompSettings.setCommand("dist-template/linux/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/linux/tools/UnfoldingTools/punf");
            MpsatVerificationSettings.setCommand("dist-template/linux/tools/UnfoldingTools/mpsat");
            break;
        case MACOS:
            PcompSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatVerificationSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            break;
        case WINDOWS:
            PcompSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\pcomp.exe");
            PunfSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatVerificationSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            break;
        default:
        }
    }

    @Test
    public void testVmeTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testTransformationCommand(workName, 12, 9);
    }

    private void testTransformationCommand(String workName, int expMappedGateCount, int expUnmappedGateCount)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);

        circuit.selectAll();

        PropagateInversionTransformationCommand command = new PropagateInversionTransformationCommand();
        command.execute(we);

        int dstMappedGateCount = 0;
        int dstUnmappedGateCount = 0;
        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            if (component.isMapped()) {
                dstMappedGateCount++;
            } else {
                dstUnmappedGateCount++;
            }
        }

        Assert.assertEquals(expMappedGateCount, dstMappedGateCount);
        Assert.assertEquals(expUnmappedGateCount, dstUnmappedGateCount);

        CombinedVerificationCommand verificationCommand = new CombinedVerificationCommand();
        Assert.assertEquals(true, verificationCommand.execute(we));

        VisualFunctionContact contact = circuit.getVisualComponentByMathReference("OUT_BUBBLE2.ON", VisualFunctionContact.class);
        Assert.assertNotNull(contact);

        Set<VisualConnection> connections = circuit.getConnections(contact);
        Assert.assertEquals(2, connections.size());

        InsertBufferTransformationCommand insertBufferCommand = new InsertBufferTransformationCommand();
        circuit.select(connections);
        insertBufferCommand.execute(we);
        Set<VisualFunctionComponent> buffers = getBuffers(circuit);
        Assert.assertEquals(connections.size(), buffers.size());
        Assert.assertEquals(false, verificationCommand.execute(we));

        ToggleZeroDelayTransformationCommand toggleZeroDelayCommand = new ToggleZeroDelayTransformationCommand();
        circuit.select(buffers);
        Set<VisualFunctionComponent> zeroDelaysBefore = getZeroDelayGates(circuit);
        toggleZeroDelayCommand.execute(we);
        Set<VisualFunctionComponent> zeroDelaysAfter = getZeroDelayGates(circuit);
        Assert.assertEquals(zeroDelaysBefore.size() + buffers.size(), zeroDelaysAfter.size());
        Assert.assertEquals(true, verificationCommand.execute(we));

        ToggleBubbleTransformationCommand toggleBubbleCommand = new ToggleBubbleTransformationCommand();
        circuit.select(buffers);
        Set<VisualFunctionComponent> invertersBefore = getInverters(circuit);
        toggleBubbleCommand.execute(we);
        Set<VisualFunctionComponent> invertersAfter = getInverters(circuit);
        Assert.assertEquals(invertersBefore.size() + buffers.size(), invertersAfter.size());
        Assert.assertEquals(false, verificationCommand.execute(we));

        ContractComponentTransformationCommand contractCommand = new ContractComponentTransformationCommand();
        circuit.select(buffers);
        Set<VisualFunctionComponent> trivialsBefore = getTrivialGates(circuit);
        contractCommand.execute(we);
        Set<VisualFunctionComponent> trivialsAfter = getTrivialGates(circuit);
        Assert.assertEquals(trivialsBefore.size() - buffers.size(), trivialsAfter.size());
        Assert.assertEquals(true, verificationCommand.execute(we));

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
