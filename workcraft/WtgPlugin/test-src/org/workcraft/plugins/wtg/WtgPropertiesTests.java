package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.ModelPropertyUtils;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.TransitionEvent;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.dtd.VisualTransitionEvent;

class WtgPropertiesTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testProperties() throws VisualModelInstantiationException, InvalidConnectionException {
        WtgDescriptor modelDescriptor = new WtgDescriptor();
        Wtg model = modelDescriptor.createMathModel();

        State state = model.createNode("state1", null, State.class);
        Waveform waveform = model.createNode("waveform1", null, Waveform.class);
        model.connect(state, waveform);
        model.connect(waveform, state);
        Signal signal = model.createNode("signal1", waveform, Signal.class);
        VisualWtg visualModel = modelDescriptor.getVisualModelDescriptor().create(model);

        VisualState visualState = visualModel.getVisualComponent(state, VisualState.class);
        visualModel.select(visualState);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 11);

        VisualWaveform visualWaveform = visualModel.getVisualComponent(waveform, VisualWaveform.class);
        visualModel.select(visualWaveform);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 11);

        VisualConnection visualConnection = visualModel.getConnection(visualState, visualWaveform);
        visualModel.select(visualConnection);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 6);

        VisualSignal visualSignal = visualModel.getVisualComponent(signal, VisualSignal.class);
        visualModel.createSignalEntryAndExit(visualSignal);
        VisualTransitionEvent visualTransition = visualModel.createVisualTransition(visualSignal, TransitionEvent.Direction.RISE);

        visualModel.selectNone();
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 3);
        visualModel.setCurrentLevel(visualWaveform);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 1);

        visualModel.select(visualSignal);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 6);

        visualModel.select(visualTransition);
        check(ModelPropertyUtils.getSelectionProperties(visualModel), 4);
    }

    private void check(ModelProperties properties, int expectedCount) {
        Assertions.assertEquals(expectedCount, properties.getDescriptors().size());
    }

}
