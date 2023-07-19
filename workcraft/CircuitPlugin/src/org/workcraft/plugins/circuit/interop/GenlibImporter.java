package org.workcraft.plugins.circuit.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.plugins.circuit.jj.genlib.ParseException;
import org.workcraft.workspace.ModelEntry;

import java.io.InputStream;

public class GenlibImporter implements Importer {

    @Override
    public GenlibFormat getFormat() {
        return GenlibFormat.getInstance();
    }

    @Override
    public ModelEntry deserialise(InputStream in, String serialisedUserData) throws DeserialisationException {
        GenlibParser parser = new GenlibParser(in);
        if (DebugCommonSettings.getParserTracing()) {
            parser.enable_tracing();
        } else {
            parser.disable_tracing();
        }
        Circuit circuit = new Circuit();
        try {
            Library library = parser.parseGenlib();
            for (final String name: library.getNames()) {
                Gate gate = library.get(name);
                GenlibUtils.instantiateGate(gate, null, circuit);
            }
            return new ModelEntry(new CircuitDescriptor(), circuit);
        } catch (ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
