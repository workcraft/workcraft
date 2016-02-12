/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.interop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;

public class PDFExporter implements Exporter {

    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        InputStream svg = SVGExportUtils.stream(model);
        Transcoder transcoder = new PDFTranscoder();
        TranscoderInput transcoderInput = new TranscoderInput(svg);
        TranscoderOutput transcoderOutput = new TranscoderOutput(out);
        try {
            transcoder.transcode(transcoderInput, transcoderOutput);
        } catch (TranscoderException e) {
            throw new SerialisationException(e);
        }
    }

    public String getDescription() {
        return ".pdf (FOP PDF generator)";
    }

    public String getExtenstion() {
        return ".pdf";
    }

    public int getCompatibility(Model model) {
        if (model instanceof VisualModel)
            return Exporter.GENERAL_COMPATIBILITY;
        else
            return Exporter.NOT_COMPATIBLE;
    }

    @Override
    public UUID getTargetFormat() {
        return Format.PDF;
    }
}