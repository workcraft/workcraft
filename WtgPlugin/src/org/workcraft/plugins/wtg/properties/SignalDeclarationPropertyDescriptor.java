package org.workcraft.plugins.wtg.properties;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.dtd.DtdSettings;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.wtg.*;
import org.workcraft.plugins.wtg.tools.WtgSignalGeneratorTool;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import static org.workcraft.plugins.wtg.utils.WtgUtils.getFinalSignalStatesFromWaveform;
public class SignalDeclarationPropertyDescriptor implements PropertyDescriptor {
    private final VisualWtg visualWtg;
    private final VisualWaveform visualWaveform;
    private final String signalName;
    public SignalDeclarationPropertyDescriptor(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        this.visualWtg = visualWtg;
        this.visualWaveform = visualWaveform;
        this.signalName = signalName;
    }
    @Override
    public Map<Object, String> getChoice() {
        return null;
    }
    @Override
    public String getName() {
        return signalName + " declared";
    }
    @Override
    public Class<?> getType() {
        return Boolean.class;
    }
    @Override
    public Object getValue() {
        Wtg wtg = (Wtg) visualWtg.getMathModel();
        for (Signal signal : wtg.getSignals(visualWaveform.getReferencedWaveform())) {
            if (wtg.getName(signal).equals(signalName)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isWritable() {
        return true;
    }
    @Override
    public void setValue(Object value) {
        Wtg wtg = (Wtg) visualWtg.getMathModel();
        if ((value instanceof Boolean) && (signalName != null)) {
            if ((Boolean) value) {
                for (GraphEditorTool tool : visualWtg.getGraphEditorTools()) {
                    if (tool instanceof WtgSignalGeneratorTool) {
                        Point2D position = newSignalPosition();
                        VisualSignal newSignal;
                        try {
                            newSignal = (VisualSignal)
                                    ((WtgSignalGeneratorTool) tool).generateNode(visualWtg, position);
                        } catch (NodeCreationException e1) {
                            throw new RuntimeException(e1);
                        }
                        Signal signal = newSignal.getReferencedSignal();
                        wtg.setName(signal, signalName);
                        signal.sendNotification(new PropertyChangedEvent(signal, Signal.PROPERTY_NAME));
                        Signal.State initialState = inferrInitialState();
                        if (initialState != null) {
                            signal.setInitialState(initialState);
                        }
                        return;
                    }
                }
            } else {
                deleteAndSpaceVertically(wtg);
            }
        }
    }
    private Signal.State inferrInitialState() {
        Signal.State result;
        Guard guard = visualWaveform.getReferencedWaveform().getGuard();
        if (guard.containsKey(signalName)) {
            result = guard.get(signalName) ? Signal.State.HIGH : Signal.State.LOW;
        } else {
            result = findPreviousSignalState();
        }
        return result;
    }
    private Point2D newSignalPosition() {
        Point2D result = null;
        for (VisualComponent visualComponent  : visualWaveform.getComponents()) {
            if (visualComponent instanceof VisualSignal) {
                VisualSignal visualSignal = (VisualSignal) visualComponent;
                if (result == null) {
                    result = new Point2D.Double(visualSignal.getX(), visualSignal.getY());
                } else if (visualSignal.getY() > result.getY()) {
                    result.setLocation(visualSignal.getX(), visualSignal.getY());
                }
            }
        }
        Point2D waveformCenter = visualWaveform.getCenter();
        if (result != null) {
            result.setLocation(result.getX() + waveformCenter.getX(),
                    result.getY() + DtdSettings.getVerticalSeparation() + waveformCenter.getY());
        } else {
            result = waveformCenter;
        }
        return result;
    }
    private Signal.State findPreviousSignalState() {
        Wtg wtg = (Wtg) visualWtg.getMathModel();
        Waveform waveform = visualWaveform.getReferencedWaveform();
        Signal.State result = null;
        for (Node state : wtg.getPreset(waveform)) {
            if (state instanceof State) {
                for (Node node : wtg.getPreset(state)) {
                    if (node instanceof Waveform) {
                        Map<String, Signal.State> finalStates = getFinalSignalStatesFromWaveform(wtg, (Waveform) node);
                        if (finalStates.containsKey(signalName)) {
                            if (result == null) {
                                result = finalStates.get(signalName);
                            } else if (finalStates.get(signalName) != result) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    private void deleteAndSpaceVertically(Wtg wtg) {
        ArrayList<VisualSignal> visualSignals = new ArrayList<>();
        for (VisualComponent visualComponent  : visualWaveform.getComponents()) {
            if (visualComponent instanceof VisualSignal) {
                visualSignals.add((VisualSignal) visualComponent);
            }
        }
        visualSignals.sort(Comparator.comparing(VisualSignal::getY));
        VisualSignal previousSignal = null;
        boolean signalFound = false;
        Double nextSignalY = null;
        double offset = 0;
        for (VisualSignal visualSignal : visualSignals) {
            if (wtg.getName(visualSignal.getReferencedSignal()).equals(signalName)) {
                signalFound = true;
                //Setting the Y for the next signal
                if (previousSignal == null) {
                    nextSignalY = visualSignal.getY();
                } else {
                    nextSignalY = previousSignal.getY() + DtdSettings.getVerticalSeparation();
                }
                //Deleting the signal
                visualWtg.select(visualSignal);
                visualWtg.deleteSelection();
            } else if (signalFound) {
                if (nextSignalY != null) {
                    offset = visualSignal.getY() - nextSignalY;
                    visualSignal.setY(nextSignalY);
                    nextSignalY = null;
                } else {
                    visualSignal.setY(visualSignal.getY() - offset);
                }
            } else {
                previousSignal = visualSignal;
            }
        }
    }
    @Override
    public boolean isCombinable() {
        return false;
    }
    @Override
    public boolean isTemplatable() {
        return false;
    }
}