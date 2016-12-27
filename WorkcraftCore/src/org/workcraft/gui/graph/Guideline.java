package org.workcraft.gui.graph;

/**
 * Utility class that represents a horizontal or vertical guideline that facilitates editing by allowing
 * objects to be "snapped" to it.
 *  *
 * @author Ivan Poliakov
 *
 */
public class Guideline {
    enum GuidelineType {
        HORIZONTAL_GUIDE,
        VERTICAL_GUIDE
    }

    protected GuidelineType type;
    protected double position;

    public Guideline(GuidelineType type, double position) {
        this.type = type;
        this.position = position;
    }

    public double getPosition() {
        return position;
    }

    public GuidelineType getType() {
        return type;
    }
}