package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.Collection;
import java.util.HashSet;

public class HandshakeDataSerialiser implements DataSerialiser<HandshakeParameters> {

    private static final String SETTINGS_ELEMENT = "settings";
    private static final String SETTINGS_NAME_ATTRIBUTE = "name";
    private static final String SETTINGS_REQ_ELEMENT = "req";
    private static final String SETTINGS_ACK_ELEMENT = "ack";
    private static final String SETTINGS_CHECK_ASSERTION_ATTRIBUTE = "check-assertion";
    private static final String SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE = "check-withdrawal";
    private static final String SETTINGS_STATE_ATTRIBUTE = "state";
    private static final String SETTINGS_ALLOW_INVERSION_ATTRIBUTE = "allow-inversion";

    @Override
    public HandshakeParameters fromXML(Element parent, HandshakeParameters defaultHandshakeParameters) {
        if (defaultHandshakeParameters == null) {
            defaultHandshakeParameters = new HandshakeParameters();
        }

        Element settingsElement = XmlUtils.getChildElement(SETTINGS_ELEMENT, parent);
        Collection<String> reqs = readSignals(settingsElement, SETTINGS_REQ_ELEMENT);
        Collection<String> acks = readSignals(settingsElement, SETTINGS_ACK_ELEMENT);

        boolean checkAssert = XmlUtils.readBooleanAttribute(settingsElement,
                SETTINGS_CHECK_ASSERTION_ATTRIBUTE, defaultHandshakeParameters.isCheckAssertion());

        boolean checkWithdraw = XmlUtils.readBooleanAttribute(settingsElement,
                SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE, defaultHandshakeParameters.isCheckWithdrawal());

        HandshakeParameters.State state = XmlUtils.readEnumAttribute(settingsElement,
                SETTINGS_STATE_ATTRIBUTE, HandshakeParameters.State.class,
                defaultHandshakeParameters.getState());

        boolean allowInversion = XmlUtils.readBooleanAttribute(settingsElement,
                SETTINGS_ALLOW_INVERSION_ATTRIBUTE, defaultHandshakeParameters.isAllowInversion());

        return new HandshakeParameters(reqs, acks, checkAssert, checkWithdraw, state,
                allowInversion);
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
        Element settingsElement = XmlUtils.createChildElement(SETTINGS_ELEMENT, parent);
        writeSignals(settingsElement, handshakeParameters.getReqs(), SETTINGS_REQ_ELEMENT);
        writeSignals(settingsElement, handshakeParameters.getAcks(), SETTINGS_ACK_ELEMENT);

        settingsElement.setAttribute(SETTINGS_CHECK_ASSERTION_ATTRIBUTE,
                Boolean.toString(handshakeParameters.isCheckAssertion()));

        settingsElement.setAttribute(SETTINGS_CHECK_WITHDRAWAL_ATTRIBUTE,
                Boolean.toString(handshakeParameters.isCheckWithdrawal()));

        settingsElement.setAttribute(SETTINGS_STATE_ATTRIBUTE,
                handshakeParameters.getState().name());

        settingsElement.setAttribute(SETTINGS_ALLOW_INVERSION_ATTRIBUTE,
                Boolean.toString(handshakeParameters.isAllowInversion()));
    }

    private void writeSignals(Element parent, Collection<String> signals, String elementName) {
        for (String signal : signals) {
            Element element = XmlUtils.createChildElement(elementName, parent);
            element.setAttribute(SETTINGS_NAME_ATTRIBUTE, signal);
        }
    }

}
