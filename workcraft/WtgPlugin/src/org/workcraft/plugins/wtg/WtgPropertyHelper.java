package org.workcraft.plugins.wtg;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.dtd.DtdSettings;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.plugins.wtg.tools.SignalGeneratorTool;
import org.workcraft.plugins.wtg.utils.WtgUtils;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.SortUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Queue;
import java.util.*;

import static org.workcraft.plugins.wtg.utils.WtgUtils.getFinalSignalStatesFromWaveform;

public class WtgPropertyHelper {

    public static Collection<PropertyDescriptor> getSignalProperties(VisualWtg visualWtg) {
        Collection<PropertyDescriptor> result = new ArrayList<>();
        Wtg mathWtg = visualWtg.getMathModel();
        if (SignalCommonSettings.getGroupByType()) {
            for (Signal.Type type : Signal.Type.values()) {
                List<String> signalNames = SortUtils.getSortedNatural(mathWtg.getSignalNames(type));
                for (final String signalName : signalNames) {
                    result.add(getSignalProperty(visualWtg, signalName));
                }
            }
        } else {
            List<String> signalNames = SortUtils.getSortedNatural(mathWtg.getSignalNames());
            for (final String signalName : signalNames) {
                result.add(getSignalProperty(visualWtg, signalName));
            }
        }
        return result;
    }

    private static PropertyDescriptor getSignalProperty(VisualWtg visualWtg, String signalName) {
        Wtg wtg = visualWtg.getMathModel();
        Signal.Type signalType = wtg.getSignalType(signalName);
        Color color = DtdUtils.getTypeColor(signalType);

        Action leftAction = new Action(PropertyHelper.BULLET_SYMBOL,
                () -> {
                    for (Signal signal : wtg.getSignals()) {
                        if (signalName.equals(wtg.getName(signal))) {
                            signal.setType(signalType.toggle());
                        }
                    }
                }, "Toggle type of signal '" + signalName + "'");

        Action rightAction = new Action(PropertyHelper.CLEAR_SYMBOL,
                () -> {
                    for (VisualWaveform visualWaveform : visualWtg.getVisualWaveforms()) {
                        for (VisualSignal visualSignal : visualWtg.getVisualSignals(visualWaveform)) {
                            if (signalName.equals(visualWtg.getMathName(visualSignal))) {
                                deleteAndSpaceVertically(visualWtg, visualWaveform, signalName);
                            }
                        }
                    }
                }, "Toggle type of signal '" + signalName + "'");

        return new PropertyDeclaration<>(TextAction.class, null,
                value -> WtgUtils.renameSignal(wtg, signalName, value.getText()),
                () -> new TextAction(signalName).setLeftAction(leftAction).setRightAction(rightAction).setForeground(color)
        ).setSpan();
    }

    public static Collection<PropertyDescriptor> getSignalDeclarationProperties(VisualWtg visualWtg, VisualWaveform waveform) {
        Collection<PropertyDescriptor> result = new ArrayList<>();
        List<String> signalNames = SortUtils.getSortedNatural(visualWtg.getMathModel().getSignalNames());
        for (String signalName : signalNames) {
            result.add(getSignalDeclarationProperty(visualWtg, waveform, signalName));
        }
        return result;
    }

    public static PropertyDescriptor getSignalDeclarationProperty(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        Wtg wtg = visualWtg.getMathModel();
        Signal.Type signalType = wtg.getSignalType(signalName);
        String colorCode = ColorUtils.getHexRGB(DtdUtils.getTypeColor(signalType));
        return new PropertyDeclaration<>(Boolean.class,
                "<html>Declare <span style='color: " + colorCode + "'>" + signalName + "</span></html>",
                value -> {
                    if (signalName != null) {
                        if (value) {
                            insertNewSignal(visualWtg, visualWaveform, signalName);
                        } else {
                            deleteAndSpaceVertically(visualWtg, visualWaveform, signalName);
                        }
                    }
                },
                () -> {
                    if (signalName != null) {
                        for (Signal signal : wtg.getSignals(visualWaveform.getReferencedComponent())) {
                            if (signalName.equals(wtg.getName(signal))) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }

    private static void insertNewSignal(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        for (GraphEditorTool tool : visualWtg.getGraphEditorTools()) {
            if (tool instanceof SignalGeneratorTool) {
                Point2D position = newSignalPosition(visualWaveform);
                SignalGeneratorTool signalGeneratorTool = (SignalGeneratorTool) tool;
                VisualSignal newSignal = signalGeneratorTool.generateNode(visualWtg, position);
                Signal signal = newSignal.getReferencedComponent();
                visualWtg.getMathModel().setName(signal, signalName);
                signal.sendNotification(new PropertyChangedEvent(signal, Signal.PROPERTY_NAME));
                Signal.State initialState = inferInitialState(visualWtg, visualWaveform, signalName);
                if (initialState != null) {
                    signal.setInitialState(initialState);
                }
                return;
            }
        }
    }

    private static Signal.State inferInitialState(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        Signal.State result;
        Guard guard = visualWaveform.getReferencedComponent().getGuard();
        if (guard.containsKey(signalName)) {
            result = guard.get(signalName) ? Signal.State.HIGH : Signal.State.LOW;
        } else {
            result = findPreviousSignalState(visualWtg, visualWaveform, signalName);
        }
        return result;
    }

    private static Point2D newSignalPosition(VisualWaveform visualWaveform) {
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

    private static Signal.State findPreviousSignalState(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
        Wtg wtg = visualWtg.getMathModel();
        Waveform waveform = visualWaveform.getReferencedComponent();
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

    private static void deleteAndSpaceVertically(VisualWtg visualWtg, VisualWaveform visualWaveform, String signalName) {
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
            String name = visualWtg.getMathModel().getName(visualSignal.getReferencedComponent());
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
