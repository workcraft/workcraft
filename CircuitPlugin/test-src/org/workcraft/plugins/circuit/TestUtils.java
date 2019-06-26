package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    public static String getLibraryPath(String fileName) {
        switch (DesktopApi.getOs()) {
        case LINUX:
            return "dist-template/linux/libraries/" + fileName;
        case MACOS:
            return "dist-template/osx/Contents/Resources/libraries/" + fileName;
        case WINDOWS:
            return "dist-template\\windows\\libraries\\" + fileName;
        }
        return fileName;
    }

    public static String getToolPath(String dirName, String fileName) {
        switch (DesktopApi.getOs()) {
        case LINUX:
            return "dist-template/linux/tools/" + dirName + "/" + fileName;
        case MACOS:
            return "dist-template/osx/Contents/Resources/tools/" + dirName + "/" + fileName;
        case WINDOWS:
            return "dist-template\\windows\\tools\\" + dirName + "\\" + fileName;
        }
        return dirName + "/" + fileName;
    }

    public static <C extends AbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String workName,
            int expectedComponentCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {
        testSynthesisCommand(cls, workName, expectedComponentCount, expectedComponentCount);
    }

    public static <C extends AbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String workName,
            int minComponentCount, int maxComponentCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcMutexes = MutexUtils.getMutexPlaceReferences(srcStg);
        Set<String> srcPageRefs = new HashSet<>();
        for (PageNode page: Hierarchy.getChildrenOfType(srcStg.getRoot(), PageNode.class)) {
            boolean hasInputs = !srcStg.getSignalNames(Signal.Type.INPUT, page).isEmpty();
            boolean hasOutputs = !srcStg.getSignalNames(Signal.Type.OUTPUT, page).isEmpty();
            if (hasInputs || hasOutputs) {
                String srcPageRef = srcStg.getNodeReference(page);
                srcPageRefs.add(srcPageRef);
            }
        }

        C command = cls.newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        // Process primary ports
        for (Contact port: dstCircuit.getPorts()) {
            String dstSignal = dstCircuit.getNodeReference(port);
            if (port.isInput()) {
                dstInputs.add(dstSignal);
            }
            if (port.isOutput()) {
                dstOutputs.add(dstSignal);
            }
        }
        // Process environment pins
        Set<String> dstPageRefs = new HashSet<>();
        for (PageNode page: Hierarchy.getChildrenOfType(dstCircuit.getRoot(), PageNode.class)) {
            for (Contact port: dstCircuit.getPorts()) {
                if (port.getParent() == page) {
                    dstPageRefs.add(dstCircuit.getNodeReference(page));
                    break;
                }
            }
        }
        Set<String> dstMutexes = getMutexComponentReferences(dstCircuit);
        int dstComponentCount = dstCircuit.getFunctionComponents().size();

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcMutexes, dstMutexes);
        Assert.assertTrue(minComponentCount <= dstComponentCount);
        Assert.assertTrue(maxComponentCount >= dstComponentCount);
        Assert.assertEquals(srcPageRefs, dstPageRefs);
    }

    private static Set<String> getMutexComponentReferences(Circuit circuit) {
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
