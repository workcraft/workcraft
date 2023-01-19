package org.workcraft.plugins.parity;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

/**
 * Visual component of the ParityDescriptor.
 * Subclass of VisualModelDescriptor.
 */
public class VisualParityDescriptor implements VisualModelDescriptor {

    /**
     * Generates an instance of VisualParity based off of a given MathModel,
     * which in this case will be Parity.
     * @param mathModel    Math Model of the Parity game
     * @return             An instance of VisualParity generated based off of Parity
     */
    @Override
    public VisualParity create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Parity.class, VisualParity.class.getSimpleName());
        return new VisualParity((Parity) mathModel);
    }

}