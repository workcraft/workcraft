package org.workcraft.plugins.parity;

import org.workcraft.dom.ModelDescriptor;

/**
 * ModelDescriptor subclass specifically for Parity games.
 * This will define the name of the game in the menu, call for a new Parity game
 * to be made, generate the visualDescriptor, and get the rating.
 */
public class ParityDescriptor implements ModelDescriptor {

    /**
     * Get the display name of the Parity game selection within the menu.
     * @return    "Parity Game"
     */
    @Override
    public String getDisplayName() {
        return "Parity Game";
    }

    /**
     * Generate the math model for the Parity game. This makes an instance of
     * Parity.
     * @return    Parity instance
     */
    @Override
    public Parity createMathModel() {
        return new Parity();
    }

    /**
     * Get the VisualParityDescriptor.
     * @return    VisualParityDescriptor instance
     */
    @Override
    public VisualParityDescriptor getVisualModelDescriptor() {
        return new VisualParityDescriptor();
    }

    /**
     * Get the rating of the model.
     * This is set as trivial (it will appear near the bottom of the menu),
     * however this is not too relevant.
     * @return    trivial rating
     */
    @Override
    public Rating getRating() {
        return Rating.TRIVIAL;
    }
}