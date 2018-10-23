package org.workcraft.plugins.dfs.tools;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.dfs.*;

import java.util.*;

public class Cycle implements Comparable<Cycle> {
    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final char RIGHT_ARROW_SYMBOL = 0x2192;

    public final VisualDfs dfs;
    public final LinkedHashSet<VisualDelayComponent> components;
    public final int tokenCount;
    public final double totalDelay;
    public final double throughput;
    public final double minDelay;
    public final double maxDelay;
    private final String toString;

    public Cycle(VisualDfs dfs, LinkedHashSet<VisualDelayComponent> components) {
        this.dfs = dfs;
        this.components = components;
        this.tokenCount = getTokenCount();
        this.totalDelay = getTotalDelay();
        this.throughput = getThroughput();
        this.minDelay = getMinDelay();
        this.maxDelay = getMaxDelay();
        this.toString = getStringRepresentation();
    }

    private int getTokenCount() {
        Integer result = 0;
        boolean spreadTokenDetected = false;
        boolean isMarkedFirstRegister = false;
        boolean isMarkedLastRegister = false;
        boolean isFirstRegister = true;
        for (VisualComponent c: components) {
            if (c instanceof VisualRegister || c instanceof VisualBinaryRegister) {
                boolean hasToken = false;
                if (c instanceof VisualRegister) {
                    hasToken = ((VisualRegister) c).getReferencedRegister().isMarked();
                }
                if (c instanceof VisualBinaryRegister) {
                    BinaryRegister ref = ((VisualBinaryRegister) c).getReferencedBinaryRegister();
                    hasToken = ref.isTrueMarked() || ref.isFalseMarked();
                }

                if (!hasToken) {
                    spreadTokenDetected = false;
                } else {
                    if (!spreadTokenDetected) {
                        result++;
                        spreadTokenDetected = true;
                    }
                }

                if (isFirstRegister) {
                    isMarkedFirstRegister = hasToken;
                    isFirstRegister = false;
                }
                isMarkedLastRegister = hasToken;
            }
        }
        if (isMarkedFirstRegister && isMarkedLastRegister && (result > 1)) {
            result--;
        }
        return result;
    }

    private double getTotalDelay() {
        double result = 0.0;
        for (VisualDelayComponent component: components) {
            result += getEffectiveDelay(component);
        }
        return result;
    }

    public Set<VisualPushRegister> getPushPreset(VisualNode node) {
        HashSet<VisualPushRegister> result = new HashSet<>();
        HashSet<VisualNode> visited = new HashSet<>();
        Queue<VisualNode> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            VisualNode cur = queue.remove();
            if (visited.contains(cur) || !components.contains(cur)) continue;
            visited.add(cur);
            for (VisualNode pred: dfs.getPreset(cur)) {
                if (!(pred instanceof VisualComponent)) continue;
                if (pred instanceof VisualPushRegister) {
                    result.add((VisualPushRegister) pred);
                } else     if (!(pred instanceof VisualPopRegister)) {
                    queue.add(pred);
                }
            }
        }
        return result;
    }

    public double getEffectiveDelay(VisualDelayComponent component) {
        HashSet<VisualControlRegister> controls = new HashSet<>();
        for (VisualPushRegister push: getPushPreset(component)) {
            controls.addAll(dfs.getPreset(push, VisualControlRegister.class));
        }
        double probability = 1.0;
        for (VisualControlRegister control: controls) {
            probability *= control.getReferencedControlRegister().getProbability();
        }
        double delay = ((MathDelayNode) component.getReferencedComponent()).getDelay();
        return delay * probability;
    }

    private double getMinDelay() {
        double result = 0.0;
        boolean first = true;
        for (VisualDelayComponent component: components) {
            double delay = getEffectiveDelay(component);
            if (first || delay < result) {
                result = delay;
                first = false;
            }
        }
        return result;
    }

    private double getMaxDelay() {
        double result = 0.0;
        boolean first = true;
        for (VisualDelayComponent component: components) {
            double delay = getEffectiveDelay(component);
            if (first || delay > result) {
                result = delay;
                first = false;
            }
        }
        return result;
    }

    private Double getThroughput() {
        double delay = getTotalDelay();
        if (delay == 0.0) {
            return Double.MAX_VALUE;
        }
        return getTokenCount() / delay;
    }

    @Override
    public int compareTo(Cycle other) {
        double thisThroughput = this.getThroughput();
        double otherThroughput = other.getThroughput();
        if (thisThroughput > otherThroughput) {
            return 1;
        } else if (thisThroughput < otherThroughput) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return toString;
    }

    public String getStringRepresentation() {
        String result = "";
        if (components != null && dfs != null) {
            for (VisualDelayComponent component: components) {
                if (result.length() > 0) {
                    result += Character.toString(RIGHT_ARROW_SYMBOL);
                }
                result += dfs.getMathModel().getNodeReference(component.getReferencedComponent());
            }
        }
        return result;
    }

}
