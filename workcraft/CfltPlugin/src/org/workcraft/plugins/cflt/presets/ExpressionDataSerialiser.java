package org.workcraft.plugins.cflt.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

public class ExpressionDataSerialiser implements DataSerialiser<ExpressionParameters> {

    private static final String SETTINGS_ELEMENT = "settings";
    private static final String SETTINGS_DESCRIPTION_ATTRIBUTE = "description";
    private static final String SETTINGS_MODE_ATTRIBUTE = "mode";
    private static final String EXPRESSION_ELEMENT = "expression";

    @Override
    public ExpressionParameters fromXML(Element parent, ExpressionParameters defaultParameters) {
        if (defaultParameters == null) {
            defaultParameters = new ExpressionParameters(
                    null, ExpressionParameters.Mode.FAST_MIN, null);
        }
        Element settingsElement = XmlUtils.getChildElement(SETTINGS_ELEMENT, parent);

        String description = XmlUtils.readTextAttribute(settingsElement,
                SETTINGS_DESCRIPTION_ATTRIBUTE, defaultParameters.getDescription());

        ExpressionParameters.Mode mode = XmlUtils.readEnumAttribute(settingsElement,
                SETTINGS_MODE_ATTRIBUTE, ExpressionParameters.Mode.class,
                defaultParameters.getMode());

        Element lastElement = settingsElement == null ? parent : settingsElement;
        Element expressionElement = XmlUtils.getChildElement(EXPRESSION_ELEMENT, lastElement);
        String expression = expressionElement != null ? expressionElement.getTextContent() : lastElement.getTextContent();

        return new ExpressionParameters(description, mode, expression);
    }

    @Override
    public void toXML(ExpressionParameters parameters, Element parent) {
        Element settingsElement = XmlUtils.createChildElement(SETTINGS_ELEMENT, parent);
        settingsElement.setAttribute(SETTINGS_DESCRIPTION_ATTRIBUTE, parameters.getDescription());
        settingsElement.setAttribute(SETTINGS_MODE_ATTRIBUTE, parameters.getMode().name());
        settingsElement.setTextContent(parameters.getExpression());
    }

}
