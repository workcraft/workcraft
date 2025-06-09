package org.workcraft.plugins.cflt.node;

import org.workcraft.plugins.stg.SignalTransition;

import static org.workcraft.plugins.cflt.utils.ExpressionUtils.*;

public class NodeDetails {
    private final String name;
    private final String label;
    private final SignalTransition.Direction direction;

    public NodeDetails(String name, String label) {
        char lastChar = label.charAt(label.length() - 1);
        boolean isDirectionChar = lastChar == PLUS_DIR || lastChar == MINUS_DIR || lastChar == TOGGLE_DIR;
        this.direction = getDirection(lastChar);
        this.label = isDirectionChar ? label.substring(0, label.length() - 1) : label;
        this.name = name;
    }

    public SignalTransition.Direction getDirection() {
        return direction;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    private static SignalTransition.Direction getDirection(char dir) {
        return switch (dir) {
            case PLUS_DIR -> SignalTransition.Direction.PLUS;
            case MINUS_DIR -> SignalTransition.Direction.MINUS;
            default -> SignalTransition.Direction.TOGGLE;
        };
    }
}
