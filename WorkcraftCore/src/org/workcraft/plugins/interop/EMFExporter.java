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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
//import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;

public class EMFExporter implements Exporter {


    public void export(Model model, OutputStream out) throws IOException, SerialisationException {
        InputStream svg = SVGExportUtils.stream(model);

        UserAgentAdapter ua = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(ua);
        try {
            BridgeContext bridgeContext = new BridgeContext(ua, loader);
            try {
                Document svgDocument = loader.loadDocument("", svg);

                GVTBuilder gvtBuilder = new GVTBuilder();
                GraphicsNode rootNode = gvtBuilder.build(bridgeContext, svgDocument);

                Rectangle2D bounds = rootNode.getBounds();
                Dimension size = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
                // FIXME: freehep SVG2EMF converter does not seem to produce good results
//                EMFGraphics2D eg2d = new EMFGraphics2D(out, size);
//                eg2d.startExport();
//                rootNode.paint(eg2d);
//                eg2d.endExport();
            } finally {
                bridgeContext.dispose();
            }
        } finally {
            loader.dispose();
        }
    }

    public String getDescription() {
        return ".emf (Freehep EMF converter)";
    }

    public String getExtenstion() {
        return ".emf";
    }

    public int getCompatibility(Model model) {
        if (model instanceof VisualModel)
            return Exporter.GENERAL_COMPATIBILITY;
        else
            return Exporter.NOT_COMPATIBLE;
    }

    @Override
    public UUID getTargetFormat() {
        return Format.EMF;
    }
}