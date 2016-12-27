package org.workcraft.plugins.circuit.interop;

import java.io.File;
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
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.workspace.ModelEntry;

public class GenlibImporter implements Importer {

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".lib");
    }

    @Override
    public String getDescription() {
        return "Genlib (.lib)";
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new CircuitDescriptor(), importGenlib(in));
    }

    public Circuit importGenlib(InputStream in) throws DeserialisationException {
        final Circuit circuit = new Circuit();
        GenlibParser genlibParser = new GenlibParser(in);
        if (CommonDebugSettings.getParserTracing()) {
            genlibParser.enable_tracing();
        } else {
            genlibParser.disable_tracing();
        }
        try {
            Library library = genlibParser.parseGenlib();
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
