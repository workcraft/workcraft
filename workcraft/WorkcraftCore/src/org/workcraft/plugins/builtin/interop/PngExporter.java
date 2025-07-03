package org.workcraft.plugins.builtin.interop;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.utils.BatikUtils;

import java.io.OutputStream;

public class PngExporter implements Exporter {

    @Override
    public boolean isCompatible(Model<?, ?> model) {
        return model instanceof VisualModel;
    }

    @Override
    public PngFormat getFormat() {
        return PngFormat.getInstance();
    }

    @Override
    public void serialise(Model<?, ?> model, OutputStream out) throws SerialisationException {
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR,
                EditorCommonSettings.getPngBackgroundColor());

        BatikUtils.transcode((VisualModel) model, out, transcoder);
    }

}
