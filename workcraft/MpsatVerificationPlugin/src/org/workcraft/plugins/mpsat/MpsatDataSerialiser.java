package org.workcraft.plugins.mpsat;

import org.w3c.dom.Element;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

public class MpsatDataSerialiser implements DataSerialiser<VerificationParameters> {

    public static final String SETTINGS_ELEMENT = "settings";
    public static final String SETTINGS_NAME_ATTRIBUTE = "name";
    public static final String SETTINGS_MODE_ATTRIBUTE = "mode";
    public static final String SETTINGS_VERBOSITY_ATTRIBUTE = "verbosity";
    public static final String SETTINGS_SOLUTION_LIMIT_ATTRIBUTE = "solutionNumberLimit";
    public static final String SETTINGS_SOLUTION_MODE_ATTRIBUTE = "solutionMode";
    public static final String SETTINGS_REACH_ELEMENT = "reach";
    public static final String SETTINGS_INVERSE_PREDICATE_ATTRIBUTE = "inversePredicate";

    @Override
    public VerificationParameters fromXML(Element parent) {
        Element element = XmlUtils.getChildElement(SETTINGS_ELEMENT, parent);
        String name = element.getAttribute(SETTINGS_NAME_ATTRIBUTE);
        VerificationMode mode = VerificationMode.getModeByArgument(element.getAttribute(SETTINGS_MODE_ATTRIBUTE));
        int verbosity = readIntAttr(element, SETTINGS_VERBOSITY_ATTRIBUTE, 0);
        int solutionNumberLimit = readIntAttr(element, SETTINGS_SOLUTION_LIMIT_ATTRIBUTE, -1);
        SolutionMode solutionMode = SolutionMode.valueOf(element.getAttribute(SETTINGS_SOLUTION_MODE_ATTRIBUTE));

        Element re = XmlUtils.getChildElement(SETTINGS_REACH_ELEMENT, element);
        String reach = re.getTextContent();
        boolean inversePredicate = Boolean.parseBoolean(element.getAttribute(SETTINGS_INVERSE_PREDICATE_ATTRIBUTE));

        return new VerificationParameters(name, mode, verbosity, solutionMode, solutionNumberLimit, reach, inversePredicate);
    }


    private int readIntAttr(Element element, String name, int defaultValue) {
        String value = element.getAttribute(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    @Override
    public void toXML(VerificationParameters verificationParameters, Element parent) {
        Element element = parent.getOwnerDocument().createElement(SETTINGS_ELEMENT);
        element.setAttribute(SETTINGS_NAME_ATTRIBUTE, verificationParameters.getName());
        element.setAttribute(SETTINGS_MODE_ATTRIBUTE, verificationParameters.getMode().getArgument());
        element.setAttribute(SETTINGS_VERBOSITY_ATTRIBUTE, Integer.toString(verificationParameters.getVerbosity()));
        element.setAttribute(SETTINGS_SOLUTION_LIMIT_ATTRIBUTE, Integer.toString(verificationParameters.getSolutionNumberLimit()));
        element.setAttribute(SETTINGS_SOLUTION_MODE_ATTRIBUTE, verificationParameters.getSolutionMode().name());

        Element reach = parent.getOwnerDocument().createElement(SETTINGS_REACH_ELEMENT);
        reach.setTextContent(verificationParameters.getExpression());
        element.appendChild(reach);

        element.setAttribute(SETTINGS_INVERSE_PREDICATE_ATTRIBUTE, Boolean.toString(verificationParameters.getInversePredicate()));

        parent.appendChild(element);
    }

}
