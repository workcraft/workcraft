package org.workcraft.plugins.atacs;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.atacs.commands.AtacsAbstractSynthesisCommand;
import org.workcraft.plugins.atacs.commands.AtacsComplexGateSynthesisCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AtacsSynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            AtacsSettings.setCommand("dist-template/linux/tools/ATACS/atacs");
            break;
        case MACOS:
            AtacsSettings.setCommand("dist-template/osx/Contents/Resources/tools/ATACS/atacs");
            break;
        case WINDOWS:
            AtacsSettings.setCommand("dist-template\\windows\\tools\\ATACS\\atacs.exe");
            break;
        default:
        }
    }

    @Test @Ignore
    public void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test @Ignore
    public void edcCscComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 7);
    }

    private void testComplexGateSynthesisCommand(String workName, int expectedGateCount) {
        try {
            testSynthesisCommand(AtacsComplexGateSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <C extends AtacsAbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String workName, int expectedGateCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Signal.Type.INPUT, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Signal.Type.OUTPUT, null);
        srcOutputs.addAll(srcStg.getSignalNames(Signal.Type.INTERNAL, null));
        Set<String> srcMutexes = getMutexNames(srcStg);

        C command = cls.newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        for (Contact port: dstCircuit.getPorts()) {
            if (port.isInput()) {
                dstInputs.add(port.getName());
            }
            if (port.isOutput()) {
                dstOutputs.add(port.getName());
            }
        }
        Set<String> dstMutexes = getMutexNames(dstCircuit);
        int dstGateCount = dstCircuit.getFunctionComponents().size();

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcMutexes, dstMutexes);
        Assert.assertEquals(expectedGateCount, dstGateCount);
    }

    private Set<String> getMutexNames(Stg stg) {
        HashSet<String> result = new HashSet<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            result.add(stg.getNodeReference(place));
        }
        return result;
    }

    private Set<String> getMutexNames(Circuit circuit) {
        HashSet<String> result = new HashSet<>();
        Mutex mutex = CircuitSettings.parseMutexData();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (mutex.name.equals(component.getModule())) {
                result.add(circuit.getNodeReference(component));
            }
        }
        return result;
    }

}
