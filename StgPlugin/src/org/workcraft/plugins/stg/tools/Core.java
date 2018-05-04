package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.HashSet;

import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.util.MultiSet;

@SuppressWarnings("serial")
public class Core extends HashSet<String> {
    private Color color = Color.getHSBColor((float) Math.random(), 0.3f, 0.7f);
    private final Trace firstConfiguration;
    private final Trace secondConfiguration;
    private final String comment;

    public Core(Trace firstConfiguration, Trace secondConfiguration, String comment) {
        super();
        this.firstConfiguration = firstConfiguration;
        this.secondConfiguration = secondConfiguration;
        this.comment = comment;

        MultiSet<String> union = new MultiSet<>();
        MultiSet<String> intersection = new MultiSet<>();
        if (this.firstConfiguration != null) {
            union.addAll(this.firstConfiguration);
            intersection.addAll(this.firstConfiguration);
        }
        if (this.secondConfiguration != null) {
            union.addAll(this.secondConfiguration);
            intersection.retainAll(this.secondConfiguration);
        }
        union.removeAll(intersection);
        union.removeAll(intersection);
        addAll(union);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Trace getFirstConfiguration() {
        return firstConfiguration;
    }

    public Trace getSecondConfiguration() {
        return secondConfiguration;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        String result = "";
        boolean first = true;
        for (String s: this) {
            if (!first) {
                result += ", ";
            }
            result += s;
            first = false;
        }
        return "{" + result + "}";
    }

}
