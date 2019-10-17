package org.workcraft.plugins.xbm.serialisation;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.interop.NouncFormat;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.utils.ExportUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NouncSerialiser implements ModelSerialiser {

    private static final String KEYWORD_NAME = "name";
    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_BURST_DIVIDER = "|";
    private static final String KEYWORD_STATE_LOW = "0";
    private static final String KEYWORD_STATE_HIGH = "1";
    private static final String KEYWORD_COMMENT_OUT = ";;";
    private static int stateCounter = 0;

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) throws SerialisationException {
        PrintWriter writer = new PrintWriter(out);
        writer.write(Info.getGeneratedByText(KEYWORD_COMMENT_OUT + " NOUNC file", "\n"));
        String title = ExportUtils.asIdentifier(model.getTitle());
        writer.write(KEYWORD_COMMENT_OUT + " " + KEYWORD_NAME + " " + title + "\n");
        if (model instanceof Xbm) {
            writeXbm(writer, (Xbm) model);
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        writer.close();
        return refs;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Xbm;
    }

    @Override
    public UUID getFormatUUID() {
        return NouncFormat.getInstance().getUuid();
    }

    private void writeXbm(PrintWriter writer, Xbm xbm) {
        Map<XbmState, Integer> stateMapping = mapStateToInteger(xbm);
        writer.write(writeInitialState(xbm));
        writer.write(writeBursts(xbm, stateMapping));
    }

    private String writeInitialState(Xbm xbm) {
        Collection<XbmSignal> conditionals = xbm.getSignals(XbmSignal.Type.CONDITIONAL);
        Collection<XbmSignal> inputs = xbm.getSignals(XbmSignal.Type.INPUT);
        Collection<XbmSignal> outputs = xbm.getSignals(XbmSignal.Type.OUTPUT);
        XbmState initState = (XbmState) xbm.getInitialState();
        String result = KEYWORD_COMMENT_OUT + " Conditionals\n";
        for (XbmSignal conditional: conditionals) {
            result += KEYWORD_INPUT + " " + xbm.getName(conditional) + " " + KEYWORD_STATE_LOW + "\n";
        }
        result += KEYWORD_COMMENT_OUT + " Inputs\n";
        for (XbmSignal input: inputs) {
            result += KEYWORD_INPUT + " " + xbm.getName(input) + " " + writeSignalValue(initState, input) + "\n";
        }
        result += KEYWORD_COMMENT_OUT + " Outputs\n";
        for (XbmSignal output: outputs) {
            result += KEYWORD_OUTPUT + " " + xbm.getName(output) + " " + writeSignalValue(initState, output) + "\n";
        }
        return result;
    }

    private String writeSignalValue(XbmState state, XbmSignal signal) {
        switch (state.getEncoding().get(signal)) {
        case HIGH:
            return KEYWORD_STATE_HIGH;
        case LOW:
            return KEYWORD_STATE_LOW;
        default:
            return "";
        }
    }

    private String writeBursts(Xbm xbm, Map<XbmState, Integer> stateMapping) {
        String result = KEYWORD_COMMENT_OUT + " Transitions\n";
        for (BurstEvent burstEvent: xbm.getBurstEvents()) {
            result += writeBurst(stateMapping, burstEvent.getConditionalMapping(), burstEvent.getBurst()) + "\n";
        }
        return result;
    }

    private String writeBurst(Map<XbmState, Integer> stateMapping, Conditional conditional, Burst burst) {
        int from = stateMapping.get(burst.getFrom());
        int to = stateMapping.get(burst.getTo());
        String conditionalDirections = "[" + writeConditionalTransition(conditional) + "]";
        String inputDirections = burst.getInputBurstAsString().replace(",", "");
        String outputDirections = burst.getOutputBurstAsString().replace(",", "");
        String result = from + " " + to + " ";
        if (!conditional.isEmpty()) {
            result += conditionalDirections + " ";
        }
        result += inputDirections + " " + KEYWORD_BURST_DIVIDER + " " + outputDirections;
        return result;
    }

    private String writeConditionalTransition(Conditional conditional) {
        String result = "";
        for (Map.Entry<String, Boolean> entry : conditional.entrySet()) {
            if (!result.isEmpty()) {
                result += " ";
            }
            result += entry.getKey();
            if (entry.getValue()) {
                result += "+";
            } else {
                result += "-";
            }
        }
        return result;
    }

    private Map<XbmState, Integer> mapStateToInteger(Xbm xbm) {
        Map<XbmState, Integer> result = new HashMap<>();
        for (XbmState state : xbm.getXbmStates()) {
            if (!result.keySet().contains(state)) {
                result.put(state, stateCounter);
                ++stateCounter;
            }
        }
        return result;
    }
}
