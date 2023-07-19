package org.workcraft.plugins.stg.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.plugins.stg.jj.StgParser;
import org.workcraft.workspace.ModelEntry;

import java.io.InputStream;

public class LpnImporter implements Importer {

    @Override
    public LpnFormat getFormat() {
        return LpnFormat.getInstance();
    }

    @Override
    public ModelEntry deserialise(InputStream in, String serialisedUserData) throws DeserialisationException {
        return new ModelEntry(new StgDescriptor(), deserialiseStg(in));
    }

    public static Stg deserialiseStg(InputStream in) throws DeserialisationException {
        StgParser parser = new StgParser(in);
        if (DebugCommonSettings.getParserTracing()) {
            parser.enable_tracing();
        } else {
            parser.disable_tracing();
        }
        try {
            return parser.parse();
        } catch (FormatException | ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
