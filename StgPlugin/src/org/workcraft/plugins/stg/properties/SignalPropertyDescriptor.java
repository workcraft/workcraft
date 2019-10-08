package org.workcraft.plugins.stg.properties;

import org.workcraft.dom.Container;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;

import java.util.Map;

public class SignalPropertyDescriptor implements PropertyDescriptor<TextAction> {

    private static final char SEARCH_SYMBOL = 0x2315;
    private static final String SEARCH_TEXT = Character.toString(SEARCH_SYMBOL);

    private final VisualStg visualStg;
    private final String signal;
    private final Container container;

    public SignalPropertyDescriptor(VisualStg visualStg, String signal, Container container) {
        this.visualStg = visualStg;
        this.signal = signal;
        this.container = container;
    }

    @Override
    public Map<TextAction, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return signal + " name";
    }

    @Override
    public Class<TextAction> getType() {
        return TextAction.class;
    }

    @Override
    public TextAction getValue() {
        return new TextAction(signal, new Action(SEARCH_TEXT,
                () -> {
                    visualStg.selectNone();
                    Stg mathStg = visualStg.getMathModel();
                    for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                        visualStg.getVisualComponent(transition, VisualSignalTransition.class);
                        visualStg.addToSelection(visualStg.getVisualComponent(transition, VisualSignalTransition.class));
                    }
                }));
    }

    @Override
    public void setValue(TextAction value) {
        String newName = value.getText();
        if (!signal.equals(newName)) {
            Stg mathStg = visualStg.getMathModel();
            for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                mathStg.setName(transition, newName);
            }
        }
    }

}
