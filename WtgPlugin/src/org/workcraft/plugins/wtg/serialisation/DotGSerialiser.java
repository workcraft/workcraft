package org.workcraft.plugins.wtg.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.Transition;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

public class DotGSerialiser implements ModelSerialiser {

    private static final String KEYWORDS_INPUTS = ".inputs";
    private static final String KEYWORD_OUTPUTS = ".outputs";
    private static final String KEYWORD_INTERNAL = ".internal";
    private static final String KEYWORD_INITIAL = ".initial";
    private static final String KEYWORD_WAVEFORM = ".waveform";
    private static final String KEYWORD_ENTRY = ".entry";
    private static final String KEYWORD_EXIT = ".exit";
    private static final String KEYWORD_END = ".end";

    class ReferenceResolver implements ReferenceProducer {
        HashMap<Object, String> refMap = new HashMap<>();

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        PrintWriter writer = new PrintWriter(out);
        writer.write(Info.getGeneratedByText("# WTG file ", "\n"));

        ReferenceResolver resolver = new ReferenceResolver();

        if (model instanceof Wtg) {
            write(writer, (Wtg) model);
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        writer.close();
        return resolver;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Wtg;
    }

    @Override
    public String getDescription() {
        return "Workcraft WTG serialiser";
    }

    @Override
    public String getExtension() {
        return ".wtg";
    }

    @Override
    public UUID getFormatUUID() {
        return Format.WTG;
    }

    private String getSrialisedNodeName(Wtg wtg, Node node) {
        if (node instanceof Transition) {
            Transition transition = (Transition) node;
            Signal signal = transition.getSignal();
            Direction direction = transition.getDirection();
            return wtg.getName(signal) + direction.getSymbol();
        }
        return wtg.getName(node);
    }

    private void write(PrintWriter out, Wtg wtg) {
        writeSignalHeader(out, wtg, Type.INPUT);
        writeSignalHeader(out, wtg, Type.OUTPUT);
        writeSignalHeader(out, wtg, Type.INTERNAL);
        writeInitial(out, wtg);
        for (Waveform waveform: wtg.getWaveforms()) {
            writeWaveform(out, wtg, waveform);
        }
        writeEnd(out);
    }

    private void writeSignalHeader(PrintWriter out, Wtg wtg, Type type) {
        Collection<Node> signals = new HashSet<>();
        signals.addAll(wtg.getSignals(type));
        switch (type) {
        case INPUT:
            writeNodeSet(out, wtg, KEYWORDS_INPUTS, signals);
            break;
        case OUTPUT:
            writeNodeSet(out, wtg, KEYWORD_OUTPUTS, signals);
            break;
        case INTERNAL:
            writeNodeSet(out, wtg, KEYWORD_INTERNAL, signals);
            break;
        }
    }

    private void writeInitial(PrintWriter out, Wtg wtg) {
        HashSet<State> initialStates = new HashSet<>();
        for (State state: wtg.getStates()) {
            if (state.isInitial()) {
                initialStates.add(state);
            }
        }
        if (initialStates.isEmpty()) {
            throw new FormatException("Initial state is undefined");
        } else if (initialStates.size() > 1) {
            throw new FormatException("More that one initial state");
        } else {
            writeNodeSet(out, wtg, KEYWORD_INITIAL, (Collection) initialStates);
        }
    }

    private void writeNodeSet(PrintWriter out, Wtg wtg, String keyword, Collection<Node> nodes) {
        if (nodes != null) {
            HashSet<String> nodeNames = new HashSet<>();
            for (Node node: nodes) {
                String nodeName = getSrialisedNodeName(wtg, node);
                nodeNames.add(nodeName);
            }
            if (!nodeNames.isEmpty()) {
                out.write(keyword);
                for (String nodeName: nodeNames) {
                    out.write(" " + nodeName);
                }
                out.write("\n");
            }
        }
    }

    private void writeWaveform(PrintWriter out, Wtg wtg, Waveform waveform) {
        String waveformName = getSrialisedNodeName(wtg, waveform);
        Set<Node> preset = wtg.getPreset(waveform);
        Set<Node> postset = wtg.getPostset(waveform);
        if ((preset.size() != 1) || (postset.size() != 1)) {
            throw new FormatException("Incorrect preset and/or postset of waveform '" + waveformName + "'");
        } else {
            Node entryState = preset.iterator().next();
            String entryStateName = getSrialisedNodeName(wtg, entryState);
            Node exitState = postset.iterator().next();
            String exitStateName = getSrialisedNodeName(wtg, exitState);
            out.write("\n");
            out.write(KEYWORD_WAVEFORM + " " + waveformName + " " + entryStateName + " " + exitStateName + "\n");
            writeWaveformEntry(out, wtg, waveform);
            writeWaveformBody(out, wtg, waveform);
            writeWaveformExit(out, wtg, waveform);
        }
    }

    private void writeWaveformEntry(PrintWriter out, Wtg wtg, Waveform waveform) {
        HashSet<Transition> entryTransitions = new HashSet<>();
        Collection<Transition> transitions = wtg.getTransitions(waveform);
        for (Transition transition: transitions) {
            HashSet<Node> predTransitions = new HashSet<>();
            predTransitions.addAll(wtg.getPreset(transition));
            predTransitions.retainAll(transitions);
            if (predTransitions.isEmpty()) {
                entryTransitions.add(transition);
            }
        }
        if (entryTransitions.isEmpty()) {
            String waveformName = getSrialisedNodeName(wtg, waveform);
            throw new FormatException("Waveform '" + waveformName + "' has no entry transitions");
        } else {
            writeNodeSet(out, wtg, KEYWORD_ENTRY, (Collection) entryTransitions);
        }
    }

    private void writeWaveformBody(PrintWriter out, Wtg wtg, Waveform waveform) {
        for (Transition transition: wtg.getTransitions(waveform)) {
            Set<Node> succTransitions = new HashSet<>();
            for (Node succNode: wtg.getPostset(transition)) {
                if (succNode instanceof Transition) {
                    succTransitions.add(succNode);
                }
            }
            writeWaveformBodyLine(out, wtg, transition, succTransitions);
        }
    }

    private void writeWaveformExit(PrintWriter out, Wtg wtg, Waveform waveform) {
        HashSet<Transition> exitTransitions = new HashSet<>();
        Collection<Transition> transitions = wtg.getTransitions(waveform);
        for (Transition transition: transitions) {
            HashSet<Node> succTransitions = new HashSet<>();
            succTransitions.addAll(wtg.getPostset(transition));
            succTransitions.retainAll(transitions);
            if (succTransitions.isEmpty()) {
                exitTransitions.add(transition);
            }
        }
        if (exitTransitions.isEmpty()) {
            String waveformName = getSrialisedNodeName(wtg, waveform);
            throw new FormatException("Waveform '" + waveformName + "' has no exit transitions");
        } else {
            writeNodeSet(out, wtg, KEYWORD_EXIT, (Collection) exitTransitions);
        }
    }

    private void writeWaveformBodyLine(PrintWriter out, Wtg wtg, Node node, Set<Node> succNodes) {
        if ((node != null) && !succNodes.isEmpty()) {
            String nodeName = getSrialisedNodeName(wtg, node);
            out.write(nodeName);
            for (Node succNode: succNodes) {
                String succNodeName = getSrialisedNodeName(wtg, succNode);
                out.write(" " + succNodeName);
            }
            out.write("\n");
        }
    }

    private void writeEnd(PrintWriter out) {
        out.write("\n" + KEYWORD_END + "\n");
    }

}
