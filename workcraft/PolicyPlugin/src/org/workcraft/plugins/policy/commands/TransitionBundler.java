package org.workcraft.plugins.policy.commands;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.converters.PolicyToPetriConverter;

import java.util.*;

public class TransitionBundler {

    private static final class Step extends HashSet<Transition> {
    }

    private static final class Marking extends HashMap<Place, Integer> {
    }

    private final VisualPolicy policyNet;
    private final Petri model;
    private final Step unbundled;

    private final HashMap<Transition, VisualBundledTransition> t2vbt;

    public TransitionBundler(PolicyToPetriConverter converter) {
        policyNet = converter.getPolicyNet();
        VisualPetri petriNet = converter.getPetriNet();
        model = petriNet.getMathModel();
        unbundled = new Step();

        HashMap<VisualBundledTransition, Transition> vbt2t = new HashMap<>();
        this.t2vbt = new HashMap<>();
        for (VisualBundledTransition vbt: policyNet.getVisualBundledTransitions()) {
            Transition t = null;
            for (VisualTransition vt: converter.getRelatedTransitions(vbt)) {
                if (t == null) {
                    t = vt.getReferencedComponent();
                } else {
                    t = null;
                    break;
                }
            }
            if (t != null) {
                vbt2t.put(vbt, t);
                t2vbt.put(t, vbt);
            }
        }
    }

    private Marking getMarking() {
        Marking result = new Marking();
        for (Place p: model.getPlaces()) {
            result.put(p, p.getTokens());
        }
        return result;
    }

    private void setMarking(Marking marking) {
        for (Place p: model.getPlaces()) {
            int token = 0;
            if (marking.containsKey(p)) {
                token = marking.get(p);
            }
            p.setTokens(token);
        }
    }

    private Set<Transition> getConflict(Transition t, Collection<Transition> enabled) {
        Set<Transition> result = new HashSet<>();
        Set<MathNode> tPreset = model.getPreset(t);
        for (Transition c: enabled) {
            Set<MathNode> cPreset = model.getPreset(c);
            if (cPreset.equals(tPreset)) {
                result.add(c);
            }
        }
        return result;
    }

    private Collection<Step> resolveConflicts(Step enabled) {
        HashSet<Step> result = new HashSet<>();
        if (!enabled.isEmpty()) {
            for (Transition t: enabled) {
                Set<Transition> conflict = getConflict(t, enabled);
                if (conflict.size() > 1) {
                    for (Transition c: conflict) {
                        Step newStep = new Step();
                        newStep.addAll(enabled);
                        newStep.removeAll(conflict);
                        newStep.add(c);
                        result.addAll(resolveConflicts(newStep));
                    }
                    return result;
                }
            }
            result.add(enabled);
        }
        return result;
    }

    private Collection<Step> getEnabledSteps() {
        Step enabled = new Step();
        for (Transition t: model.getTransitions()) {
            if (model.isEnabled(t) && unbundled.contains(t)) {
                enabled.add(t);
            }
        }
        return resolveConflicts(enabled);
    }

    public void run() {
        for (Transition t: model.getTransitions()) {
            VisualBundledTransition vbt = t2vbt.get(t);
            if ((vbt != null) && policyNet.getBundlesOfTransition(vbt).isEmpty()) {
                unbundled.add(t);
            }
        }

        HashMap<Step, Marking> step2marking = new HashMap<>();
        HashSet<Step> steps = new HashSet<>();
        Queue<Step> slice = null;
        Queue<Step> queue = null;
        do {
            if (queue == null) {
                queue = new ArrayDeque<>();
                slice = new ArrayDeque<>();
            } else {
                Step step = queue.remove();
                setMarking(step2marking.get(step));
                for (Transition t: step) {
                    model.fire(t);
                }
            }
            // Get all currently enabled steps
            Marking marking = getMarking();
            for (Step step: getEnabledSteps()) {
                boolean ok = true;
                for (Step n: step2marking.keySet()) {
                    if (n.containsAll(step) || step.containsAll(n)) {
                        ok = false;
                        steps.remove(n);
                    }
                }
                step2marking.put(step, marking);
                if (ok) {
                    slice.add(step);
                    steps.add(step);
                }
            }
            // If the queue is empty, proceed to the next slice
            if (queue.isEmpty()) {
                while (!slice.isEmpty()) {
                    Step step = slice.remove();
                    unbundled.removeAll(step);
                    queue.add(step);
                }
            }
        } while (!queue.isEmpty());

        // Bundle maximal non-singleton steps
        for (Step step: steps) {
            HashSet<VisualBundledTransition> bundle = new HashSet<>();
            for (Transition t: step) {
                VisualBundledTransition vbt = t2vbt.get(t);
                bundle.add(vbt);
            }
            if (step.size() > 1) {
                policyNet.bundleTransitions(bundle);
            }
        }
    }

}
