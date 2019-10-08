package org.workcraft.plugins.xbm.properties;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyUtils;
import org.workcraft.plugins.xbm.BurstEvent;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.XbmSignal;

import java.util.Map;
import java.util.regex.Matcher;

public class SignalPropertyDescriptor implements PropertyDescriptor<TextAction> {

    private final Xbm xbm;
    private final XbmSignal xbmSignal;

    public SignalPropertyDescriptor(final Xbm xbm, final XbmSignal xbmSignal) {
        this.xbm = xbm;
        this.xbmSignal = xbmSignal;
    }

    @Override
    public Map<TextAction, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return xbm.getName(xbmSignal) + " name";
    }

    @Override
    public Class<TextAction> getType() {
        return TextAction.class;
    }

    @Override
    public TextAction getValue() {
        return new TextAction(xbm.getName(xbmSignal),
                new Action(PropertyUtils.CLEAR_TEXT, () -> xbm.removeSignal(xbmSignal)));
    }

    @Override
    public void setValue(TextAction value) {
        String newName = value.getText();
        Node node = xbm.getNodeByReference(newName);
        Matcher matcher = XbmSignal.VALID_SIGNAL_NAME.matcher(newName);
        if ((node == null) && matcher.find()) {
            String origName = xbmSignal.getName();
            for (BurstEvent event: xbm.getBurstEvents()) {
                for (String ref: event.getConditionalMapping().keySet()) {
                    if (ref.equals(origName)) {
                        boolean mapValue = event.getConditionalMapping().get(origName);
                        event.getConditionalMapping().put(newName, mapValue);
                        event.getConditionalMapping().remove(origName);
                        break;
                    }
                }
            }
            xbm.setName(xbmSignal, newName);
            xbmSignal.setName(newName);
        } else if (!matcher.find()) {
            throw new ArgumentException(value + " is not a valid node name.");
        } else if (xbmSignal != node) {
            throw new ArgumentException("Node " + value + " already exists.");
        }

    }

}
