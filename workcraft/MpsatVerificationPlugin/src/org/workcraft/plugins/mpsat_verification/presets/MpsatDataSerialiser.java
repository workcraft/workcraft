package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters.SolutionMode;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

public class MpsatDataSerialiser implements DataSerialiser<VerificationParameters> {

    private static final String SETTINGS_ELEMENT = "settings";
    private static final String SETTINGS_DESCRIPTION_ATTRIBUTE = "description";
    private static final String SETTINGS_MODE_ATTRIBUTE = "mode";
    private static final String SETTINGS_VERBOSITY_ATTRIBUTE = "verbosity";
    private static final String SETTINGS_SOLUTION_LIMIT_ATTRIBUTE = "solutionNumberLimit";
    private static final String SETTINGS_SOLUTION_MODE_ATTRIBUTE = "solutionMode";
    private static final String SETTINGS_INVERSE_PREDICATE_ATTRIBUTE = "inversePredicate";
    private static final String REACH_ELEMENT = "reach";

    @Override
    public VerificationParameters fromXML(Element parent, VerificationParameters defaultVerificationParameters) {
        if (defaultVerificationParameters == null) {
            defaultVerificationParameters = new VerificationParameters(
                    null, VerificationMode.STG_REACHABILITY, 0,
                    MpsatVerificationSettings.getSolutionMode(),
                    MpsatVerificationSettings.getSolutionCount());
        }
        Element settingsElement = XmlUtils.getChildElement(SETTINGS_ELEMENT, parent);

        String description = XmlUtils.readTextAttribute(settingsElement,
                SETTINGS_DESCRIPTION_ATTRIBUTE, defaultVerificationParameters.getDescription());

        VerificationMode mode = XmlUtils.readEnumAttribute(settingsElement,
                SETTINGS_MODE_ATTRIBUTE, VerificationMode.class, defaultVerificationParameters.getMode());

        int verbosity = XmlUtils.readIntAttribute(settingsElement, SETTINGS_VERBOSITY_ATTRIBUTE,
                defaultVerificationParameters.getVerbosity());

        int solutionNumberLimit = XmlUtils.readIntAttribute(settingsElement,
                SETTINGS_SOLUTION_LIMIT_ATTRIBUTE, defaultVerificationParameters.getSolutionNumberLimit());

        SolutionMode solutionMode = XmlUtils.readEnumAttribute(settingsElement,
                SETTINGS_SOLUTION_MODE_ATTRIBUTE, SolutionMode.class,
                defaultVerificationParameters.getSolutionMode());

        boolean inversePredicate = XmlUtils.readBooleanAttribute(settingsElement,
                SETTINGS_INVERSE_PREDICATE_ATTRIBUTE, defaultVerificationParameters.getInversePredicate());

        Element lastElement = settingsElement == null ? parent : settingsElement;
        Element reachElement = XmlUtils.getChildElement(REACH_ELEMENT, lastElement);
        String reach = reachElement != null ? reachElement.getTextContent() : lastElement.getTextContent();

        return new VerificationParameters(description, mode, verbosity, solutionMode,
                solutionNumberLimit, reach, inversePredicate);
    }

    @Override
    public void toXML(VerificationParameters verificationParameters, Element parent) {
        Element settingsElement = XmlUtils.createChildElement(SETTINGS_ELEMENT, parent);

        settingsElement.setAttribute(SETTINGS_DESCRIPTION_ATTRIBUTE,
                verificationParameters.getDescription());

        settingsElement.setAttribute(SETTINGS_MODE_ATTRIBUTE,
                verificationParameters.getMode().name());

        settingsElement.setAttribute(SETTINGS_VERBOSITY_ATTRIBUTE,
                Integer.toString(verificationParameters.getVerbosity()));

        settingsElement.setAttribute(SETTINGS_SOLUTION_LIMIT_ATTRIBUTE,
                Integer.toString(verificationParameters.getSolutionNumberLimit()));

        settingsElement.setAttribute(SETTINGS_SOLUTION_MODE_ATTRIBUTE,
                verificationParameters.getSolutionMode().name());

        settingsElement.setAttribute(SETTINGS_INVERSE_PREDICATE_ATTRIBUTE,
                Boolean.toString(verificationParameters.getInversePredicate()));

        settingsElement.setTextContent(verificationParameters.getExpression());
    }

}
