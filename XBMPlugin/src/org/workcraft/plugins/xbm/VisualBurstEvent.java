package org.workcraft.plugins.xbm;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;

import java.util.Set;

public class VisualBurstEvent extends VisualEvent {

//    private final InputBurst inputBurst;
//    private final OutputBurst outputBurst;

    public VisualBurstEvent() {
        this(null, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection, VisualState first, VisualState second) {
        super(mathConnection, first, second);
//        inputBurst = new InputBurst(mathConnection.getBurst());
//        outputBurst = new OutputBurst(mathConnection.getBurst());
    }

    public BurstEvent getReferencedBurstEvent() {
        return (BurstEvent) getReferencedEvent();
    }

    private Burst getReferencedBurst() {
        return getReferencedBurstEvent().getBurst();
    }

    private Set<XbmSignal> getReferencedSignals() {
        return getReferencedBurst().getSignals();
    }

//    public InputBurst getInputBurst() {
//        return inputBurst;
//    }
//
//    public OutputBurst getOutputBurst() {
//        return outputBurst;
//    }

    @Override
    public String getLabel(DrawRequest r) {
        return getReferencedBurstEvent().getAsString();
    }



    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualBurstEvent) {
             VisualBurstEvent srcBurstEvent = (VisualBurstEvent) src;
             getReferencedBurstEvent();
        }
    }
}
//
//class InputBurst extends VisualEvent {
//
//    private final Set<XbmSignal> signals = new LinkedHashSet<>();
//    private final Burst burst;
//
//    public InputBurst(Burst burst) {
//        this.burst = burst;
//    }
//
//    public void add(XbmSignal signal) {
//        if (signal.getType() == XbmSignal.Type.INPUT) {
//            signals.add(signal);
//        }
//    }
//
//    public void remove(XbmSignal signal) {
//        if (signal.getType() == XbmSignal.Type.INPUT && signals.contains(signal)) {
//            signals.remove(signal);
//        }
//    }
//
//    public boolean contains(XbmSignal signal) {
//        return signals.contains(signal);
//    }
//
//    @Override
//    public String getLabel(DrawRequest r) {
//        String result = "";
//        for (XbmSignal signal: signals) {
//            if (!result.isEmpty()) result += ", ";
//            result += signal.getName() + burst.getDirection().get(signal);
//        }
//        return result;
//    }
//
//    @Override
//    public Color getLabelColor() {
//        return CommonSignalSettings.getInputColor();
//    }
//}
//
//class OutputBurst extends VisualEvent {
//
//    private final Set<XbmSignal> signals = new LinkedHashSet<>();
//    private final Burst burst;
//
//    public OutputBurst(Burst burst) {
//        this.burst = burst;
//    }
//
//    public void add(XbmSignal signal) {
//        if (signal.getType() == XbmSignal.Type.INPUT) {
//            signals.add(signal);
//        }
//    }
//
//    public void remove(XbmSignal signal) {
//        if (signal.getType() == XbmSignal.Type.INPUT && signals.contains(signal)) {
//            signals.remove(signal);
//        }
//    }
//
//    public boolean contains(XbmSignal signal) {
//        return signals.contains(signal);
//    }
//
//    @Override
//    public String getLabel(DrawRequest r) {
//        String result = "";
//        for (XbmSignal signal: signals) {
//            if (!result.isEmpty()) result += ", ";
//            result += signal.getName() + burst.getDirection().get(signal);
//        }
//        return result;
//    }
//
//    @Override
//    public Color getLabelColor() {
//        return CommonSignalSettings.getOutputColor();
//    }
//}