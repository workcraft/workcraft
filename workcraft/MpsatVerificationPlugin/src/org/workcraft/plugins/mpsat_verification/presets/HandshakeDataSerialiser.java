package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.Collection;
import java.util.HashSet;

public class HandshakeDataSerialiser implements DataSerialiser<HandshakeParameters> {

    private static final String SETTINGS_ELEMENT = "settings";
    private static final String SETTINGS_NAME_ATTRIBUTE = "name";
    private static final String SETTINGS_TYPE_ATTRIBUTE = "type";
    private static final String SETTINGS_REQ_ELEMENT = "req";
    private static final String SETTINGS_ACK_ELEMENT = "ack";
    private static final String SETTINGS_CHECK_ASSERTION_ATTRIBUTE = "check-assertion";
    private static final String SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE = "check-withdrawal";
    private static final String SETTINGS_STATE_ATTRIBUTE = "state";
    private static final String SETTINGS_ALLOW_INVERSION_ATTRIBUTE = "allow-inversion";

    @Override
    public HandshakeParameters fromXML(Element parent) {
        Element element = XmlUtils.getChildElement(SETTINGS_ELEMENT, parent);
        if (element == null) {
            return null;
        }

        HandshakeParameters.Type type = XmlUtils.readEnumAttribute(element, SETTINGS_TYPE_ATTRIBUTE,
                HandshakeParameters.Type.class, HandshakeParameters.Type.PASSIVE);

        Collection<String> reqs = readSignals(element, SETTINGS_REQ_ELEMENT);
        Collection<String> acks = readSignals(element, SETTINGS_ACK_ELEMENT);

        boolean checkAssert = XmlUtils.readBooleanAttribute(element, SETTINGS_CHECK_ASSERTION_ATTRIBUTE, true);
        boolean checkWithdraw = XmlUtils.readBooleanAttribute(element, SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE, true);

        HandshakeParameters.State state = XmlUtils.readEnumAttribute(element, SETTINGS_STATE_ATTRIBUTE,
                HandshakeParameters.State.class, HandshakeParameters.State.REQ0ACK0);

        boolean allowInversion = XmlUtils.readBooleanAttribute(element, SETTINGS_ALLOW_INVERSION_ATTRIBUTE, false);

        return new HandshakeParameters(type, reqs, acks, checkAssert, checkWithdraw, state, allowInversion);
    }

    private Collection<String> readSignals(Element parent, String elementName) {
        Collection<String> result = new HashSet<>();
        for (Element element : XmlUtils.getChildElements(elementName, parent)) {
            result.add(element.getAttribute(SETTINGS_NAME_ATTRIBUTE));
        }
        return result;
    }

    @Override
    public void toXML(HandshakeParameters handshakeParameters, Element parent) {
        Element element = parent.getOwnerDocument().createElement(SETTINGS_ELEMENT);
        element.setAttribute(SETTINGS_TYPE_ATTRIBUTE, handshakeParameters.getType().name());
        writeSignalse(element, handshakeParameters.getReqs(), SETTINGS_REQ_ELEMENT);
        writeSignalse(element, handshakeParameters.getAcks(), SETTINGS_ACK_ELEMENT);
        element.setAttribute(SETTINGS_CHECK_ASSERTION_ATTRIBUTE, Boolean.toString(handshakeParameters.isCheckAssertion()));
        element.setAttribute(SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE, Boolean.toString(handshakeParameters.isCheckWithdrawal()));
        element.setAttribute(SETTINGS_STATE_ATTRIBUTE, handshakeParameters.getState().name());
        element.setAttribute(SETTINGS_ALLOW_INVERSION_ATTRIBUTE, Boolean.toString(handshakeParameters.isAllowInversion()));
        parent.appendChild(element);
    }

    private void writeSignalse(Element parent, Collection<String> signals, String elementName) {
        for (String signal : signals) {
            Element element = parent.getOwnerDocument().createElement(elementName);
            element.setAttribute(SETTINGS_NAME_ATTRIBUTE, signal);
            parent.appendChild(element);
        }
    }

}
