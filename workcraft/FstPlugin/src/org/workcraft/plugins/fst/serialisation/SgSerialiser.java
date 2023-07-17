package org.workcraft.plugins.fst.serialisation;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.SignalEvent;
import org.workcraft.plugins.fst.interop.SgFormat;
import org.workcraft.serialisation.AbstractBasicModelSerialiser;
import org.workcraft.utils.ExportUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.UUID;

public class SgSerialiser extends AbstractBasicModelSerialiser {

    @Override
    public void serialise(Model model, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        String title = ExportUtils.getTitleAsIdentifier(model.getTitle());
        SgFormat format = SgFormat.getInstance();
        writer.write(ExportUtils.getExportHeader("SG file", "#", title, format));
        writer.write(".model " + title + '\n');
        if (model instanceof Fsm) {
            writeFsm(writer, (Fsm) model);
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        writer.close();
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Fsm;
    }

    @Override
    public UUID getFormatUUID() {
        return SgFormat.getInstance().getUuid();
    }

    private String getSerialisedNodeName(Fsm fsm, Node node) {
        return fsm.getNodeReference(node);
    }

    private String getSerialisedEventName(Fsm fsm, Event event) {
        String result = null;
        if (event instanceof SignalEvent) {
            SignalEvent signalEvent = (SignalEvent) event;
            Signal signal = signalEvent.getSymbol();
            result = fsm.getNodeReference(signal);
            if (signal.hasDirection()) {
                result += signalEvent.getDirection();
            }
        } else {
            Symbol symbol = event.getSymbol();
            if (symbol == null) {
                result = Fsm.EPSILON_SERIALISATION;
            } else {
                result = fsm.getNodeReference(symbol);
            }
        }
        return result;
    }

    private void writeSignalHeader(PrintWriter out, Fst fst, Signal.Type type) {
        HashSet<String> names = new HashSet<>();
        for (Signal signal: fst.getSignals(type)) {
            String name = getSerialisedNodeName(fst, signal);
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
            case DUMMY:
                out.write(".dummy");
                break;
            }
            for (String name: names) {
                out.write(' ' + name);
            }
            out.write('\n');
        }
    }

    private void writeSymbolHeader(PrintWriter out, Fsm fsm) {
        HashSet<String> names = new HashSet<>();
        for (Event event: fsm.getEvents()) {
            String name = getSerialisedEventName(fsm, event);
            names.add(name);
        }
        if (!names.isEmpty()) {
            out.write(".dummy");
            for (String s: names) {
                out.write(' ' + s);
            }
            out.write('\n');
        }
    }

    private void writeGraphEntry(PrintWriter out, Fsm fsm, Event event) {
        if (event != null) {
            State firstState = (State) event.getFirst();
            State secondState = (State) event.getSecond();
            if ((firstState != null) && (secondState != null)) {
                String eventName = getSerialisedEventName(fsm, event);
                String firstStateName = getSerialisedNodeName(fsm, firstState);
                String secondStateName = getSerialisedNodeName(fsm, secondState);
                out.write(firstStateName + ' ' + eventName + ' ' + secondStateName + '\n');
            }
        }
    }

    private void writeMarking(PrintWriter out, Fsm fsm, State state) {
        if (state != null) {
            String stateStr = getSerialisedNodeName(fsm, state);
            out.write(".marking {" + stateStr + "}\n");
        }
    }

    private void writeFsm(PrintWriter out, Fsm fsm) {
        if (fsm instanceof Fst) {
            Fst fst = (Fst) fsm;
            writeSignalHeader(out, fst, Signal.Type.INPUT);
            writeSignalHeader(out, fst, Signal.Type.OUTPUT);
            writeSignalHeader(out, fst, Signal.Type.INTERNAL);
            writeSignalHeader(out, fst, Signal.Type.DUMMY);
        } else {
            writeSymbolHeader(out, fsm);
        }

        out.write(".state graph\n");
        for (Event event : fsm.getEvents()) {
            writeGraphEntry(out, fsm, event);
        }

        writeMarking(out, fsm, fsm.getInitialState());
        out.write(".end\n");
    }

}
