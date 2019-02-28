package org.workcraft.plugins.wtg.properties;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.dtd.DtdSettings;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.wtg.*;
import org.workcraft.plugins.wtg.tools.WtgSignalGeneratorTool;

import java.awt.geom.Point2D;
import java.util.*;

import static org.workcraft.plugins.wtg.utils.WtgUtils.getFinalSignalStatesFromWaveform;

public class SignalDeclarationPropertyDescriptor implements PropertyDescriptor<Boolean> {

    private final VisualWtg visualWtg;
    private final VisualWaveform visualWaveform;
    private final String signalName;

    public SignalDeclarationPropertyDescriptor(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        this.visualWtg = visualWtg;
        this.visualWaveform = visualWaveform;
        this.signalName = signalName;
    }

    @Override
    public Map<Boolean, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return signalName + " declared";
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean getValue() {
        Wtg wtg = visualWtg.getMathModel();
        for (Signal signal : wtg.getSignals(visualWaveform.getReferencedWaveform())) {
            if (wtg.getName(signal).equals(signalName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setValue(Boolean value) {
        if (signalName != null) {
            if (value) {
                insertNewSignal();
            } else {
                deleteAndSpaceVertically();
            }
        }
    }

    private void insertNewSignal() {
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
                visualWtg.getMathModel().setName(signal, signalName);
                signal.sendNotification(new PropertyChangedEvent(signal, Signal.PROPERTY_NAME));
                Signal.State initialState = inferInitialState();
                if (initialState != null) {
                    signal.setInitialState(initialState);
                }
                return;
            }
        }
    }

    private Signal.State inferInitialState() {
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
        for (VisualComponent visualComponent : visualWaveform.getComponents()) {
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
        Wtg wtg = visualWtg.getMathModel();
        Waveform waveform = visualWaveform.getReferencedWaveform();
        if (wtg.getPreset(waveform).size() == 0) {
            return null;
        }
        Signal.State result = null;
        Set<MathNode> visitedNodes = new HashSet<>();
        Queue<MathNode> nodesToVisit = new LinkedList<>();
        visitedNodes.add(waveform);
        MathNode previousState = wtg.getPreset(waveform).iterator().next();
        visitedNodes.add(previousState);
        nodesToVisit.add(previousState);
        while (!nodesToVisit.isEmpty()) {
            MathNode node = nodesToVisit.poll();
            if (node instanceof Waveform) {
                Map<String, Signal.State> finalStates = getFinalSignalStatesFromWaveform(wtg, (Waveform) node);
                if (finalStates.containsKey(signalName)) {
                    if (result == null) {
                        result = finalStates.get(signalName);
                    } else if (finalStates.get(signalName) != result) {
                        return null;
                    }
                    continue;
                }
            }

            for (MathNode n : wtg.getPreset(node)) {
                if (!visitedNodes.contains(n)) {
                    nodesToVisit.add(n);
                    visitedNodes.add(n);
                }
            }
        }

        return result;
    }

    private void deleteAndSpaceVertically() {
        ArrayList<VisualSignal> visualSignals = new ArrayList<>();
        for (VisualComponent visualComponent : visualWaveform.getComponents()) {
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
            String name = visualWtg.getMathModel().getName(visualSignal.getReferencedSignal());
            if (name.equals(signalName)) {
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

}
