package org.workcraft.plugins.stg.tools;

import org.workcraft.traces.Trace;
import org.workcraft.types.MultiSet;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EncodingConflict {

    private Color color = Color.getHSBColor((float) Math.random(), 0.3f, 0.7f);
    private final Trace firstTrace;
    private final Trace secondTrace;
    private final String signalName;

    private final Set<String> core = new HashSet<>();
    private final Set<String> overlay = new HashSet<>();

    public EncodingConflict(Trace firstTrace, Trace secondTrace, String signalName) {
        super();
        this.firstTrace = firstTrace;
        this.secondTrace = secondTrace;
        this.signalName = signalName;

        MultiSet<String> union = new MultiSet<>();
        MultiSet<String> intersection = new MultiSet<>();
        if (this.firstTrace != null) {
            union.addAll(this.firstTrace);
            intersection.addAll(this.firstTrace);
        }
        if (this.secondTrace != null) {
            union.addAll(this.secondTrace);
            intersection.retainAll(this.secondTrace);
        }
        union.removeAll(intersection);
        union.removeAll(intersection);
        core.addAll(union);
        overlay.addAll(intersection);
    }

    public Set<String> getCore() {
        return Collections.unmodifiableSet(core);
    }

    public Set<String> getOverlay() {
        return Collections.unmodifiableSet(overlay);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getSignalName() {
        return signalName;
    }

    public String getCoreAsString() {
        return "{" + String.join(", ", core) + "}";
    }

    public String getDescription() {
        String result = "Conflict core";
        if (signalName != null) {
            result += " for signal '" + signalName + "'";
        }
        result += ": " + getCoreAsString();
        result += "\n  - Trace 1: " + firstTrace;
        result += "\n  - Trace 2: " + secondTrace;
        return result;
    }

}
