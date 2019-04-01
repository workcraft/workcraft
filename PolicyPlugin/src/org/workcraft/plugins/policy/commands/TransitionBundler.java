package org.workcraft.plugins.policy.commands;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;

import java.util.*;

public class TransitionBundler {

    @SuppressWarnings("serial")
    private class Step extends HashSet<Transition> {
    }

    @SuppressWarnings("serial")
    private class Marking extends HashMap<Place, Integer> {
    }

    private final VisualPolicy policyNet;
    private final Petri model;
    final Step unbundled;

    private final HashMap<Transition, VisualBundledTransition> t2vbt;

    public TransitionBundler(PolicyToPetriConverter converter) {
        policyNet = converter.getPolicyNet();
        VisualPetri petriNet = converter.getPetriNet();
        model = petriNet.getPetriNet();
        unbundled = new Step();

        HashMap<VisualBundledTransition, Transition> vbt2t = new HashMap<>();
        this.t2vbt = new HashMap<Transition, VisualBundledTransition>();
        for (VisualBundledTransition vbt: policyNet.getVisualBundledTransitions()) {
            Transition t = null;
            for (VisualTransition vt: converter.getRelatedTransitions(vbt)) {
                if (t == null) {
                    t = vt.getReferencedTransition();
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

/*
    private void printStep(Collection<Transition> step) {
        boolean first = true;
        System.out.printf("{");
        for (Node n: step) {
            if (first) {
                first = false;
            } else {
                System.out.printf(", ");
            }
            System.out.print(model.getNodeReference(n));

        }
        System.out.println("}");
    }
*/

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
//        System.out.printf("    confict(%s) = ", model.getNodeReference(t));
//        printStep(result);
        return result;
    }

    private Collection<Step> resolveConflicts(Step enabled) {
        HashSet<Step> result = new HashSet<>();
        if (enabled.size() > 0) {
            for (Transition t: enabled) {
                Set<Transition> conflict = getConflict(t, enabled);
                if (conflict.size() > 1) {
                    for (Transition c: conflict) {
                        Step newStep = new Step();
                        newStep.addAll(enabled);
                        newStep.removeAll(conflict);
                        newStep.add(c);
//                        System.out.print("    new step: ");
//                        printStep(newStep);
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
//        System.out.print("enabled: ");
//        printStep(enabled);

        Collection<Step> result = resolveConflicts(enabled);

//        System.out.println("steps: ");
//        for (Step s: result) {
//            System.out.print("  ");
//            printStep(s);
//        }

        return result;
    }

    public void run() {
        for (Transition t: model.getTransitions()) {
            VisualBundledTransition vbt = t2vbt.get(t);
            if (vbt != null && policyNet.getBundlesOfTransition(vbt).size() == 0) {
                unbundled.add(t);
            }
        }

        HashMap<Step, Marking> step2marking = new HashMap<>();
        HashSet<Step> steps = new HashSet<>();
        Queue<Step> slice = null;
        Queue<Step> queue = null;
        do {
            if (queue == null) {
                queue = new LinkedList<Step>();
                slice = new LinkedList<Step>();
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
//                        System.out.println("problem b/w steps:");
//                        System.out.print("  ");    printStep(n);
//                        System.out.print("  "); printStep(step);
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
//                System.out.println("progress: ");
                while (!slice.isEmpty()) {
                    Step step = slice.remove();
//                    System.out.print("  ");    printStep(step);
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
