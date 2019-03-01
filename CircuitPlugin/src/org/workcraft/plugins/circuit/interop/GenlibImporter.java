package org.workcraft.plugins.circuit.interop;

import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.plugins.circuit.jj.genlib.ParseException;
import org.workcraft.plugins.builtin.settings.CommonDebugSettings;
import org.workcraft.workspace.ModelEntry;

public class GenlibImporter implements Importer {

    @Override
    public GenlibFormat getFormat() {
        return GenlibFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new CircuitDescriptor(), importGenlib(in));
    }

    public Circuit importGenlib(InputStream in) throws DeserialisationException {
        final Circuit circuit = new Circuit();
        GenlibParser parser = new GenlibParser(in);
        if (CommonDebugSettings.getParserTracing()) {
            parser.enable_tracing();
        } else {
            parser.disable_tracing();
        }
        try {
            Library library = parser.parseGenlib();
            for (final String name: library.getNames()) {
                final Gate gate = library.get(name);
                GenlibUtils.instantiateGate(gate, null, circuit);
            }
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
        return circuit;
    }

}
