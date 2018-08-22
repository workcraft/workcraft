package org.workcraft.plugins.stg.interop;

import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.jj.ParseException;
import org.workcraft.plugins.stg.jj.StgParser;
import org.workcraft.workspace.ModelEntry;

public class StgImporter implements Importer {

    @Override
    public StgFormat getFormat() {
        return StgFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new StgDescriptor(), importStg(in));
    }

    public StgModel importStg(InputStream in) throws DeserialisationException {
        try {
            StgParser parser = new StgParser(in);
            if (CommonDebugSettings.getParserTracing()) {
                parser.enable_tracing();
            } else {
                parser.disable_tracing();
            }
            Stg stg = parser.parse();
            StgUtils.checkStg(stg, false);
            return stg;
        } catch (FormatException | ParseException e) {
            throw new DeserialisationException(e);
        }
    }

}
