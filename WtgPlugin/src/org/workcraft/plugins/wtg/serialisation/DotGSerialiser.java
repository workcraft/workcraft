package org.workcraft.plugins.wtg.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
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
        writeWtg(out, wtg);
        for (Waveform waveform: wtg.getWaveforms()) {
            writeWaveform(out, wtg, waveform);
        }
    }

    private void writeSignalHeader(PrintWriter out, Wtg wtg, Type type) {
        HashSet<String> names = new HashSet<>();
        for (Signal signal: wtg.getSignals(type)) {
            String name = getSrialisedNodeName(wtg, signal);
            names.add(name);
        }
        if (!names.isEmpty()) {
            switch (type) {
            case INPUT:
                out.write(".inputs");
                break;
            case OUTPUT:
                out.write(".outputs");
                break;
            case INTERNAL:
                out.write(".internal");
                break;
            }
            for (String name: names) {
                out.write(" " + name);
            }
            out.write("\n");
        }
    }

    private void writeWtg(PrintWriter out, Wtg wtg) {
        out.write(".wtg\n");
        for (State state: wtg.getStates()) {
            writeGraphEntry(out, wtg, state);
        }
        for (Waveform waveform: wtg.getWaveforms()) {
            writeGraphEntry(out, wtg, waveform);
        }
        writeMarking(out, wtg);
    }

    private void writeWaveform(PrintWriter out, Wtg wtg, Waveform waveform) {
        String waveformName = getSrialisedNodeName(wtg, waveform);
        out.write("\n");
        out.write(".waveform " + waveformName + "\n");
        for (Transition transition: wtg.getTransitions(waveform)) {
            writeGraphEntry(out, wtg, transition);
        }
    }

    private void writeGraphEntry(PrintWriter out, Wtg wtg, MathNode node) {
        if (node != null) {
            String curNodeName = getSrialisedNodeName(wtg, node);
            Set<Node> postset = wtg.getPostset(node);
            if (!postset.isEmpty()) {
                out.write(curNodeName);
                for (Node succNode: postset) {
                    String succNodeName = getSrialisedNodeName(wtg, succNode);
                    out.write(" " + succNodeName);
                }
                out.write("\n");
            }
        }
    }

    private void writeMarking(PrintWriter out, Wtg wtg) {
        for (State state: wtg.getStates()) {
            if (state.isInitial()) {
                String stateStr = getSrialisedNodeName(wtg, state);
                out.write(".marking " + stateStr + "\n");
            }
        }
    }

}
