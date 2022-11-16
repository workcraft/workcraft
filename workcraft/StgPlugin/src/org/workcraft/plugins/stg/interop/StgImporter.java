package org.workcraft.plugins.stg.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.plugins.stg.jj.StgParser;
import org.workcraft.workspace.ModelEntry;

import java.io.InputStream;

public class StgImporter implements Importer {

    @Override
    public StgFormat getFormat() {
        return StgFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in, String serialisedUserData) throws DeserialisationException {
        Stg stg = importStg(in);
        PetriUtils.checkSoundness(stg, false);
        return new ModelEntry(new StgDescriptor(), stg);
    }

    public Stg importStg(InputStream in) throws DeserialisationException {
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
