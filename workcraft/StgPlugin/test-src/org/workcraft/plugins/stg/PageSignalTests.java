package org.workcraft.plugins.stg;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.commands.ExpandHandshakeReqAckTransformationCommand;
import org.workcraft.plugins.stg.commands.MirrorSignalTransformationCommand;
import org.workcraft.plugins.stg.commands.SplitTransitionTransformationCommand;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * Signal operations on a transition inside a page, with a same-named signal at the root.
 */
@Execution(SAME_THREAD)
class PageSignalTests {

    private WorkspaceEntry we;
    private VisualStg visualStg;
    private Stg stg;
    private VisualPage page;
    private Container mathPage;
    private VisualSignalTransition pageTransition;

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    @BeforeEach
    void createWork() throws InvalidConnectionException {
        stg = new Stg();
        visualStg = new VisualStg(stg);
        we = Framework.getInstance().createWork(new ModelEntry(new StgDescriptor(), visualStg), "page");
        visualStg.createVisualSignalTransition("a", Signal.Type.INPUT, SignalTransition.Direction.PLUS);
        page = visualStg.createVisualPage(visualStg.getRoot());
        mathPage = NamespaceHelper.getMathContainer(visualStg, page);
        VisualStgPlace p0 = visualStg.createVisualPlace("p0", page);
        VisualStgPlace p1 = visualStg.createVisualPlace("p1", page);
        pageTransition = visualStg.createVisualSignalTransition("a", Signal.Type.OUTPUT,
                SignalTransition.Direction.PLUS, page);

        visualStg.connect(p0, pageTransition);
        visualStg.connect(pageTransition, p1);
        visualStg.setCurrentLevel(page);
        visualStg.select(pageTransition);
    }

    @AfterEach
    void closeWork() {
        Framework.getInstance().closeWork(we);
    }

    @Test
    @SuppressWarnings("unchecked")
    void signalTypePropertyTest() {
        PropertyDescriptor<Signal.Type> property = (PropertyDescriptor<Signal.Type>)
                StgPropertyHelper.getSignalTypeProperty(stg, pageTransition.getReferencedComponent());

        property.setValue(Signal.Type.INTERNAL);
        Assertions.assertEquals(Signal.Type.INTERNAL, stg.getSignalType("a", mathPage));
        Assertions.assertEquals(Signal.Type.INPUT, stg.getSignalType("a", stg.getRoot()));
    }

    @Test
    void mirrorSignalTest() {
        new MirrorSignalTransformationCommand().execute(we);
        Assertions.assertEquals(Signal.Type.INPUT, stg.getSignalType("a", mathPage));
        Assertions.assertEquals(Signal.Type.INPUT, stg.getSignalType("a", stg.getRoot()));
    }

    @Test
    void splitTransitionTest() {
        new SplitTransitionTransformationCommand().execute(we);
        Assertions.assertEquals(2, stg.getSignalTransitions("a", mathPage).size());
        Assertions.assertEquals(1, stg.getSignalTransitions("a", stg.getRoot()).size());
    }

    @Test
    void expandHandshakeTest() {
        new ExpandHandshakeReqAckTransformationCommand().execute(we);
        Assertions.assertEquals(Signal.Type.OUTPUT, stg.getSignalType("a_req", mathPage));
        Assertions.assertEquals(Signal.Type.INPUT, stg.getSignalType("a_ack", mathPage));
        Assertions.assertEquals(1, stg.getSignalTransitions("a", stg.getRoot()).size());
    }

}
