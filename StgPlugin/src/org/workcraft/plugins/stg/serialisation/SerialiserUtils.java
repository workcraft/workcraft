package org.workcraft.plugins.stg.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;

public class SerialiserUtils {

    private static final String KEYWORD_MODEL = ".model";
    private static final String KEYWORD_NAME = ".name";
    private static final String KEYWORD_INPUTS = ".inputs";
    private static final String KEYWORD_OUTPUTS = ".outputs";
    private static final String KEYWORD_INTERNAL = ".internal";
    private static final String KEYWORD_DUMMY = ".dummy";
    private static final String KEYWORD_GRAPH = ".graph";
    private static final String KEYWORD_MARKING = ".marking";
    private static final String KEYWORD_CAPACITY = ".capacity";
    private static final String KEYWORD_END = ".end";

    public enum Style { STG, LPN };

    public static void writeModel(Model model, OutputStream out, Style style) {
        if (!(model instanceof PetriNetModel)) {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        PetriNetModel petriModel = (PetriNetModel) model;

        String prefix = "# STG file ";
        String keyword = KEYWORD_MODEL;
        boolean needInstanceNumbers = false;
        if (style == Style.LPN) {
            prefix = "# LPN file ";
            keyword = KEYWORD_NAME;
            needInstanceNumbers = hasInstanceNumbers(petriModel);
        }
        PrintWriter writer = new PrintWriter(out);
        writer.write(Info.getGeneratedByText(prefix, "\n"));
        writer.write(keyword + " " + getClearTitle(petriModel) + "\n");
        if (petriModel instanceof StgModel) {
            writeSTG((StgModel) petriModel, writer, needInstanceNumbers);
        } else {
            writePN(petriModel, writer);
        }
        writer.write(KEYWORD_END + "\n");
        writer.close();
    }

    private static boolean hasInstanceNumbers(PetriNetModel petriModel) {
        if (petriModel instanceof StgModel) {
            StgModel stg = (StgModel) petriModel;
            for (Node n: stg.getSignalTransitions()) {
                if (stg.getInstanceNumber(n) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getReference(Model model, Node node, boolean needInstanceNumbers) {
        String result = model.getNodeReference(node);
        if (needInstanceNumbers && (model instanceof StgModel) && (node instanceof Transition)) {
            Transition transition = (Transition) node;
            StgModel stg = (StgModel) model;
            if (stg.getInstanceNumber(transition) == 0) {
                result += "/0";
            }
        }
        return result;
    }

    private static void writeSignalsHeader(PrintWriter out, Collection<String> signalNames, String header) {
        if (!signalNames.isEmpty()) {
            LinkedList<String> sortedNames = new LinkedList<>(signalNames);
            Collections.sort(sortedNames);
            out.write(header);
            for (String name : sortedNames) {
                out.write(" ");
                out.write(name);
            }
            out.write("\n");
        }
    }

    private static Iterable<Node> sortNodes(Collection<? extends Node> nodes, final Model model) {
        ArrayList<Node> list = new ArrayList<>(nodes);
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return model.getNodeReference(o1).compareTo(model.getNodeReference(o2));
            }
        });
        return list;
    }

    private static void writeGraphEntry(PrintWriter out, Model model, Node node, boolean needInstanceNumbers) {
        if (node instanceof StgPlace) {
            StgPlace stgPlace = (StgPlace) node;
            if (stgPlace.isImplicit()) {
                return;
            }
        }
        String nodeRef = getReference(model, node, needInstanceNumbers);
        out.write(nodeRef);
        Set<Node> postset = model.getPostset(node);
        for (Node succNode : sortNodes(postset, model)) {
            String succNodeRef = getReference(model, succNode, needInstanceNumbers);
            if (succNode instanceof StgPlace) {
                StgPlace succPlace = (StgPlace) succNode;
                if (succPlace.isImplicit()) {
                    Collection<Node> succPostset = model.getPostset(succNode);
                    if (succPostset.size() > 1) {
                        throw new FormatException("Implicit place cannot have more than one node in postset");
                    }
                    Node succTransition = succPostset.iterator().next();
                    String succTransitionRef = getReference(model, succTransition, needInstanceNumbers);
                    out.write(" " + succTransitionRef);
                } else {
                    out.write(" " + succNodeRef);
                }
            } else {
                out.write(" " + succNodeRef);
            }
        }
        out.write("\n");
    }

    public static String getClearTitle(Model model) {
        String title = model.getTitle();
        // Non-empty model name must be present in .model line of .g file.
        // Otherwise Petrify will use the full file name (possibly with bad characters) as a Verilog module name.
        if ((title == null) || title.isEmpty()) {
            title = "Untitled";
        }
        // If the title start with a number then prepend it with an underscore.
        if (Character.isDigit(title.charAt(0))) {
            title = "_" + title;
        }
        // Petrify does not allow spaces and special symbols in the model name, so replace them with underscores.
        String result = title.replaceAll("[^A-Za-z0-9_]", "_");
        if (!result.equals(model.getTitle())) {
            LogUtils.logWarning("Model title was exported as '" + result + "'.");
        }
        return result;
    }

    private static void writeSTG(StgModel stg, PrintWriter out, boolean needInstanceNumbers) {
        writeSignalsHeader(out, stg.getSignalReferences(Type.INTERNAL), KEYWORD_INTERNAL);
        writeSignalsHeader(out, stg.getSignalReferences(Type.INPUT), KEYWORD_INPUTS);
        writeSignalsHeader(out, stg.getSignalReferences(Type.OUTPUT), KEYWORD_OUTPUTS);
        Set<String> pageRefs = getPageReferences(stg);
        if (!pageRefs.isEmpty()) {
            out.write("# Pages added as dummies: " + String.join(", ", pageRefs) + "\n");
        }
        Set<String> dummyRefs = stg.getDummyReferences();
        dummyRefs.addAll(pageRefs);
        writeSignalsHeader(out, dummyRefs, KEYWORD_DUMMY);

        out.write(KEYWORD_GRAPH + "\n");
        for (Node n : sortNodes(stg.getSignalTransitions(), stg)) {
            writeGraphEntry(out, stg, n, needInstanceNumbers);
        }
        for (Node n : sortNodes(stg.getDummyTransitions(), stg)) {
            writeGraphEntry(out, stg, n, needInstanceNumbers);
        }
        for (Node n : sortNodes(stg.getPlaces(), stg)) {
            writeGraphEntry(out, stg, n, needInstanceNumbers);
        }
        writeMarking(stg, stg.getPlaces(), out, needInstanceNumbers);
    }

    private static Set<String> getPageReferences(StgModel stg) {
        Set<String> result = new HashSet<>();
        for (PageNode page: Hierarchy.getDescendantsOfType(stg.getRoot(), PageNode.class)) {
            result.add(stg.getNodeReference(page));
        }
        return result;
    }

    private static void writeMarking(Model model, Collection<Place> places, PrintWriter out, boolean needInstanceNumbers) {
        ArrayList<String> markingEntries = new ArrayList<>();
        for (Place p: places) {
            final int tokens = p.getTokens();
            final String reference;
            if (p instanceof StgPlace) {
                if (((StgPlace) p).isImplicit()) {
                    Node predNode = model.getPreset(p).iterator().next();
                    String predRef = getReference(model, predNode, needInstanceNumbers);
                    Node succNode = model.getPostset(p).iterator().next();
                    String succRef = getReference(model, succNode, needInstanceNumbers);
                    reference = "<" + predRef + "," + succRef + ">";
                } else {
                    reference = getReference(model, p, needInstanceNumbers);
                }
            } else {
                reference = getReference(model, p, needInstanceNumbers);
            }
            if (tokens == 1) {
                markingEntries.add(reference);
            } else if (tokens > 1) {
                markingEntries.add(reference + "=" + tokens);
            }
        }
        Collections.sort(markingEntries);
        out.write(KEYWORD_MARKING + " {");
        boolean first = true;
        for (String m : markingEntries) {
            if (!first) {
                out.write(" ");
            } else {
                first = false;
            }
            out.write(m);
        }
        out.write("}\n");
        StringBuilder capacity = new StringBuilder();
        for (Place p : places) {
            if (p instanceof StgPlace) {
                StgPlace stgPlace = (StgPlace) p;
                if (stgPlace.getCapacity() != 1) {
                    String placeRef = getReference(model, p, needInstanceNumbers);
                    capacity.append(" " + placeRef + "=" + stgPlace.getCapacity());
                }
            }
        }
        if (capacity.length() > 0) {
            out.write(KEYWORD_CAPACITY + capacity + "\n");
        }
    }

    private static void writePN(PetriNetModel petriModel, PrintWriter out) {
        LinkedList<String> transitions = new LinkedList<>();
        for (Transition t : petriModel.getTransitions()) {
            String transitionRef = petriModel.getNodeReference(t);
            transitions.add(transitionRef);
        }
        writeSignalsHeader(out, transitions, KEYWORD_DUMMY);
        out.write(KEYWORD_GRAPH + "\n");
        for (Transition t : petriModel.getTransitions()) {
            writeGraphEntry(out, petriModel, t, false);
        }
        for (Place p : petriModel.getPlaces()) {
            writeGraphEntry(out, petriModel, p, false);
        }
        writeMarking(petriModel, petriModel.getPlaces(), out, false);
    }

}
