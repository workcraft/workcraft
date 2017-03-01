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
import org.workcraft.plugins.dtd.DtdUtils;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.SignalEntry;
import org.workcraft.plugins.dtd.SignalExit;
import org.workcraft.plugins.dtd.SignalTransition;
import org.workcraft.plugins.dtd.SignalTransition.Direction;
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
        private static final String SEPARATOR_INSTANCE = "/";
        private static final String SEPARATOR_STATE = "@";
        HashMap<Object, String> refMap = new HashMap<>();

        ReferenceResolver(Wtg wtg) {
            for (Signal signal: wtg.getSignals()) {
                refMap.put(signal, wtg.getName(signal));
            }
            for (State state: wtg.getStates()) {
                refMap.put(state, wtg.getName(state));
            }
            for (Waveform waveform: wtg.getWaveforms()) {
                refMap.put(waveform, wtg.getName(waveform));
                HashMap<String, Integer> instanceCount = new HashMap<>();
                for (SignalTransition transition: wtg.getTransitions(waveform)) {
                    String transitionName = getSrialisedTransitionName(wtg, transition);
                    if (!instanceCount.containsKey(transitionName)) {
                        instanceCount.put(transitionName, 1);
                    } else {
                        int count = instanceCount.get(transitionName);
                        transitionName += SEPARATOR_INSTANCE + count;
                        instanceCount.put(transitionName, count + 1);
                    }
                    refMap.put(transition, transitionName);
                }
            }
        }

        private String getSrialisedTransitionName(Wtg wtg, SignalTransition transition) {
            Signal signal = transition.getSignal();
            String result = wtg.getName(signal);
            Signal.State previousState = wtg.getPreviousState(transition);
            Direction direction = transition.getDirection();
            switch (previousState) {
            case UNSTABLE:
                Signal.State signalState = DtdUtils.getNextState(direction);
                result += SEPARATOR_STATE + signalState.getSymbol();
                break;
            default:
                result += direction.getSymbol();
                break;
            }
            return result;
        }

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (model instanceof Wtg) {
            Wtg wtg = (Wtg) model;
            ReferenceResolver resolver = new ReferenceResolver(wtg);
            PrintWriter writer = new PrintWriter(out);
            write(writer, wtg, resolver);
            writer.close();
            return resolver;
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
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

    private void write(PrintWriter out, Wtg wtg, ReferenceProducer refs) {
        out.write(Info.getGeneratedByText("# WTG file ", "\n"));
        writeSignalHeader(out, wtg, refs, Type.INPUT);
        writeSignalHeader(out, wtg, refs, Type.OUTPUT);
        writeSignalHeader(out, wtg, refs, Type.INTERNAL);
        writeInitial(out, wtg, refs);
        for (Waveform waveform: wtg.getWaveforms()) {
            writeWaveform(out, wtg, refs, waveform);
        }
        writeEnd(out);
    }

    private void writeSignalHeader(PrintWriter out, Wtg wtg, ReferenceProducer refs, Type type) {
        Collection<Node> signals = new HashSet<>();
        signals.addAll(wtg.getSignals(type));
        switch (type) {
        case INPUT:
            writeNodeSet(out, wtg, refs, KEYWORDS_INPUTS, signals);
            break;
        case OUTPUT:
            writeNodeSet(out, wtg, refs, KEYWORD_OUTPUTS, signals);
            break;
        case INTERNAL:
            writeNodeSet(out, wtg, refs, KEYWORD_INTERNAL, signals);
            break;
        }
    }

    private void writeInitial(PrintWriter out, Wtg wtg, ReferenceProducer refs) {
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
            writeNodeSet(out, wtg, refs, KEYWORD_INITIAL, (Collection) initialStates);
        }
    }

    private void writeNodeSet(PrintWriter out, Wtg wtg, ReferenceProducer refs, String keyword, Collection<Node> nodes) {
        if (nodes != null) {
            HashSet<String> nodeNames = new HashSet<>();
            for (Node node: nodes) {
                String nodeName = refs.getReference(node);
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

    private void writeWaveform(PrintWriter out, Wtg wtg, ReferenceProducer refs, Waveform waveform) {
        String waveformName = refs.getReference(waveform);
        Set<Node> preset = wtg.getPreset(waveform);
        Set<Node> postset = wtg.getPostset(waveform);
        if ((preset.size() != 1) || (postset.size() != 1)) {
            throw new FormatException("Incorrect preset and/or postset of waveform '" + waveformName + "'");
        } else {
            Node entryState = preset.iterator().next();
            String entryStateName = refs.getReference(entryState);
            Node exitState = postset.iterator().next();
            String exitStateName = refs.getReference(exitState);
            out.write("\n");
            out.write(KEYWORD_WAVEFORM + " " + waveformName + " " + entryStateName + " " + exitStateName + "\n");
            writeWaveformEntry(out, wtg, refs, waveform);
            writeWaveformBody(out, wtg, refs, waveform);
            writeWaveformExit(out, wtg, refs, waveform);
        }
    }

    private void writeWaveformEntry(PrintWriter out, Wtg wtg, ReferenceProducer refs, Waveform waveform) {
        HashSet<SignalTransition> entryTransitions = new HashSet<>();
        for (SignalEntry entry: wtg.getEntrys(waveform)) {
            for (Node node: wtg.getPostset(entry)) {
                if (node instanceof SignalTransition) {
                    SignalTransition transition = (SignalTransition) node;
                    entryTransitions.add(transition);
                }
            }
        }
        if (entryTransitions.isEmpty()) {
            String waveformName = refs.getReference(waveform);
            throw new FormatException("Waveform '" + waveformName + "' has no entry transitions");
        } else {
            writeNodeSet(out, wtg, refs, KEYWORD_ENTRY, (Collection) entryTransitions);
        }
    }

    private void writeWaveformBody(PrintWriter out, Wtg wtg, ReferenceProducer refs, Waveform waveform) {
        for (SignalTransition transition: wtg.getTransitions(waveform)) {
            Set<SignalTransition> succTransitions = new HashSet<>();
            for (Node succNode: wtg.getPostset(transition)) {
                if (succNode instanceof SignalTransition) {
                    SignalTransition succTransition = (SignalTransition) succNode;
                    succTransitions.add(succTransition);
                }
            }
            writeWaveformBodyLine(out, wtg, refs, transition, succTransitions);
        }
    }

    private void writeWaveformExit(PrintWriter out, Wtg wtg, ReferenceProducer refs, Waveform waveform) {
        HashSet<SignalTransition> exitTransitions = new HashSet<>();
        for (SignalExit exit: wtg.getExits(waveform)) {
            for (Node node: wtg.getPreset(exit)) {
                if (node instanceof SignalTransition) {
                    SignalTransition transition = (SignalTransition) node;
                    exitTransitions.add(transition);
                }
            }
        }
        if (exitTransitions.isEmpty()) {
            String waveformName = refs.getReference(waveform);
            throw new FormatException("Waveform '" + waveformName + "' has no exit transitions");
        } else {
            writeNodeSet(out, wtg, refs, KEYWORD_EXIT, (Collection) exitTransitions);
        }
    }

    private void writeWaveformBodyLine(PrintWriter out, Wtg wtg, ReferenceProducer refs, SignalTransition transition, Set<SignalTransition> succTransitions) {
        if ((transition != null) && (succTransitions != null) && !succTransitions.isEmpty()) {
            String transitionName = refs.getReference(transition);
            out.write(transitionName);
            for (Node succTransition: succTransitions) {
                String succTransitionName = refs.getReference(succTransition);
                out.write(" " + succTransitionName);
            }
            out.write("\n");
        }
    }

    private void writeEnd(PrintWriter out) {
        out.write("\n" + KEYWORD_END + "\n");
    }

}
