package org.workcraft.plugins.fst.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.jj.ParseException;
import org.workcraft.plugins.fst.jj.SgParser;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.workspace.ModelEntry;

import java.io.InputStream;

public class SgImporter implements Importer {

    @Override
    public SgFormat getFormat() {
        return SgFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new FstDescriptor(), importSG(in));
    }

    public Fst importSG(InputStream in) throws DeserialisationException {
        try {
            SgParser parser = new SgParser(in);
            if (DebugCommonSettings.getParserTracing()) {
                parser.enable_tracing();
            } else {
                parser.disable_tracing();
            }
            return parser.parse();
        } catch (FormatException | ParseException e) {
            throw new DeserialisationException(e);
        }
    }
}
