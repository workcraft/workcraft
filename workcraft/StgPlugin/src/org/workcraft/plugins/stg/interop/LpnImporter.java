package org.workcraft.plugins.stg.interop;

import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.plugins.stg.jj.StgParser;
import org.workcraft.workspace.ModelEntry;

public class LpnImporter implements Importer {

    @Override
    public LpnFormat getFormat() {
        return LpnFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new StgDescriptor(), importSTG(in));
    }

    public StgModel importSTG(InputStream in) throws DeserialisationException {
        try {
            StgParser parser = new StgParser(in);
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
