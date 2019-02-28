package org.workcraft.plugins.wtg.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.VisualWaveform;
import org.workcraft.plugins.wtg.VisualWtg;
import org.workcraft.types.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.*;

public class StructureWaveformTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Structure waveforms (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Structure waveform";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualWtg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualWaveform;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> waveforms = new HashSet<>();
        if (model instanceof VisualWtg) {
            VisualWtg wtg = (VisualWtg) model;
            Container currentLevel = wtg.getCurrentLevel();
            if (currentLevel instanceof VisualWaveform) {
                waveforms.add((VisualWaveform) currentLevel);
            } else {
                waveforms.addAll(wtg.getVisualWaveforms());
                Collection<VisualNode> selection = wtg.getSelection();
                if (!selection.isEmpty()) {
                    waveforms.retainAll(selection);
                }
            }
        }
        return waveforms;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualWtg) && (node instanceof VisualWaveform)) {
            restructureWaveform((VisualWtg) model, (VisualWaveform) node);
        }
    }

    private void restructureWaveform(VisualWtg visualWtg, VisualWaveform visualWaveform) {
        Map<VisualEvent, Integer> outboundDependencies = new HashMap<>();
        Map<VisualEvent, Integer> inboundDependencies = new HashMap<>();
        for (VisualTransitionEvent visualTransition : visualWtg.getVisualSignalTransitions(visualWaveform)) {
            inboundDependencies.put(visualTransition, visualWtg.getPreset(visualTransition).size());
            outboundDependencies.put(visualTransition, visualWtg.getPostset(visualTransition).size());
        }
        Map<VisualEvent, Double> nodesX = new HashMap<>();
        List<VisualEvent> noInboundDependencies = new LinkedList<>();
        List<VisualEvent> noOutboundDependencies = new LinkedList<>();
        for (VisualEntryEvent entry : visualWtg.getVisualSignalEntries(visualWaveform)) {
            for (VisualNode visualNode : visualWtg.getPostset(entry)) {
                if (visualNode instanceof VisualTransitionEvent) {
                    Integer dependencies = inboundDependencies.computeIfPresent((VisualEvent) visualNode, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        noInboundDependencies.add((VisualEvent) visualNode);
                        nodesX.put((VisualEvent) visualNode, entry.getX() + DtdSettings.getTransitionSeparation());
                    }
                }
            }
        }
        for (VisualExitEvent exit : visualWtg.getVisualSignalExits(visualWaveform)) {
            for (VisualNode visualNode : visualWtg.getPreset(exit)) {
                if (visualNode instanceof VisualTransitionEvent) {
                    Integer dependencies = outboundDependencies.computeIfPresent((VisualEvent) visualNode, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        noOutboundDependencies.add((VisualEvent) visualNode);
                    }
                }
            }
        }
        Double maxX = null;
        Queue<VisualEvent> toVisit = new LinkedList<>(noInboundDependencies);
        while (!toVisit.isEmpty()) {
            VisualEvent visitingEvent = toVisit.poll();
            for (VisualNode node : visualWtg.getPostset(visitingEvent))  {
                VisualEvent nextEvent = (VisualEvent) node;
                if (nextEvent instanceof VisualTransitionEvent) {
                    if (inboundDependencies.containsKey(nextEvent)) {
                        double newX = nodesX.get(visitingEvent) + DtdSettings.getTransitionSeparation();
                        nodesX.computeIfPresent(nextEvent, (k, v) -> Math.max(v, newX));
                        nodesX.putIfAbsent(nextEvent, newX);
                        if (maxX == null || maxX < newX) {
                            maxX = newX;
                        }
                    }
                    Integer dependencies = inboundDependencies.computeIfPresent(nextEvent, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        toVisit.add(nextEvent);
                    }
                }
            }
        }

        toVisit.addAll(noOutboundDependencies);
        while (!toVisit.isEmpty()) {
            VisualEvent visitingEvent = toVisit.poll();
            for (VisualNode node : visualWtg.getPreset(visitingEvent))  {
                VisualEvent nextEvent = (VisualEvent) node;
                if (nextEvent instanceof VisualTransitionEvent) {
                    if (outboundDependencies.containsKey(nextEvent)) {
                        Integer dependencies = outboundDependencies.computeIfPresent(nextEvent, (k, v) -> v - 1);
                        if (dependencies == 0) {
                            if (nextEvent.getVisualSignal() != visitingEvent.getVisualSignal()) {
                                nodesX.put(nextEvent, nodesX.get(visitingEvent) - DtdSettings.getTransitionSeparation());
                            }
                            toVisit.add(nextEvent);
                        }
                    }

                }
            }
        }

        VisualExitEvent exit = visualWtg.getVisualSignalExits(visualWaveform).iterator().next();
        double exitX;
        if (maxX != null) {
            exitX = maxX + DtdSettings.getTransitionSeparation();
        } else {
            exitX = visualWtg.getVisualSignalEntries(visualWaveform).iterator().next().getX()
                    + DtdSettings.getTransitionSeparation();
        }

        ArrayList<Pair<VisualEvent, Double>> visualEventsShiftRight = new ArrayList<>();
        ArrayList<Pair<VisualEvent, Double>> visualEventsShiftLeft = new ArrayList<>();
        for (Map.Entry<VisualEvent, Double> eventsNewX : nodesX.entrySet()) {
            if (eventsNewX.getKey().getX() < eventsNewX.getValue()) {
                visualEventsShiftRight.add(new Pair<>(eventsNewX.getKey(), eventsNewX.getValue()));
            } else if (eventsNewX.getKey().getX() > eventsNewX.getValue()) {
                visualEventsShiftLeft.add(new Pair<>(eventsNewX.getKey(), eventsNewX.getValue()));
            }
        }

        if (exitX > exit.getX()) {
            exit.setX(exitX);
        }
        visualEventsShiftRight.sort((p1, p2) -> (p1.getSecond().compareTo(p2.getSecond())) * (-1));
        for (Pair<VisualEvent, Double> visualEventPosition : visualEventsShiftRight) {
            visualEventPosition.getFirst().setX(visualEventPosition.getSecond());
        }
        visualEventsShiftLeft.sort(Comparator.comparing(Pair::getSecond));
        for (Pair<VisualEvent, Double> visualEventPosition : visualEventsShiftLeft) {
            visualEventPosition.getFirst().setX(visualEventPosition.getSecond());
        }
        if (exitX < exit.getX()) {
            exit.setX(exitX);
        }
    }
}
