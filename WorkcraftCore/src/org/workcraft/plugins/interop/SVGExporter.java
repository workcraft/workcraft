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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;
import org.workcraft.util.XmlUtil;

public class SVGExporter implements Exporter {


	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, SerialisationException {

		if (model == null)
			throw new SerialisationException ("Not a visual model");

			try {
				Document doc = XmlUtil.createDocument();

				SVGGraphics2D g2d = new SVGGraphics2D(doc);

				g2d.scale(50, 50);

				Rectangle2D bounds = ((VisualGroup)model.getRoot()).getBoundingBoxInLocalSpace();

				g2d.translate(-bounds.getMinX(), -bounds.getMinY());
				g2d.setSVGCanvasSize(new Dimension((int)(bounds.getWidth()*50), (int)(bounds.getHeight()*50)));

				((VisualModel)model).draw(g2d, Decorator.Empty.INSTANCE);

				g2d.stream(new OutputStreamWriter(out));

			} catch (ParserConfigurationException e) {
				throw new SerialisationException(e);
			}
		}


	public String getDescription() {
		return ".svg (Batik SVG generator)";
	}

	public String getExtenstion() {
		return ".svg";
	}

	public int getCompatibility(Model model) {
		if (model instanceof VisualModel)
			return Exporter.GENERAL_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	@Override
	public UUID getTargetFormat() {
		return Format.SVG;
	}
}