package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.ModelPropertyUtils;

class PropertiesTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testProperties() throws VisualModelInstantiationException, InvalidConnectionException {
        StgDescriptor modelDescriptor = new StgDescriptor();
        Stg model = modelDescriptor.createMathModel();

        StgPlace place = model.createPlace("p1", null);
        SignalTransition inputTransition = model.createSignalTransition("in1", null);
        inputTransition.setSignalType(Signal.Type.INPUT);
        DummyTransition dummyTransition = model.createDummyTransition("dum1", null);

        model.connect(place, inputTransition);
        model.connect(inputTransition, dummyTransition);

        Node implicitPlace = model.getNodeByReference("<in1~,dum1>");
        Assertions.assertTrue(implicitPlace instanceof StgPlace);
        StgPlace stgPlace = (StgPlace) implicitPlace;
        Assertions.assertTrue(stgPlace.isImplicit());

        VisualStg visualModel = modelDescriptor.getVisualModelDescriptor().create(model);

        VisualStgPlace visualPlace = visualModel.getVisualComponent(place, VisualStgPlace.class);
        visualModel.select(visualPlace);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 15);

        VisualSignalTransition visualOutputTransition = visualModel.createVisualSignalTransition("out1", Signal.Type.OUTPUT, SignalTransition.Direction.MINUS);
        VisualConnection visualUndirectedConnection = visualModel.connectUndirected(visualPlace, visualOutputTransition);
        visualModel.select(visualUndirectedConnection);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 4);

        VisualSignalTransition visualInputTransition = visualModel.getVisualComponent(inputTransition, VisualSignalTransition.class);
        visualModel.select(visualInputTransition);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 5);

        VisualConnection visualConnection = visualModel.getConnection(visualPlace, visualInputTransition);
        visualModel.select(visualConnection);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 6);

        VisualDummyTransition visualDummyTransition = visualModel.getVisualComponent(dummyTransition, VisualDummyTransition.class);
        visualModel.select(visualDummyTransition);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 3);

        VisualConnection visualImplicitPlaceArc = visualModel.getConnection(visualInputTransition, visualDummyTransition);
        visualModel.select(visualImplicitPlaceArc);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 9);

        visualModel.selectNone();
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 5);
    }

    private void check(ModelProperties properties, int expectedCount) {
        Assertions.assertEquals(expectedCount, properties.getDescriptors().size());
    }

}
