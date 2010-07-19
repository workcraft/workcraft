package org.workcraft.plugins.interop;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.serialisation.Format;

public class DotExporter implements Exporter {

	@Override
	public void export(Model model, OutputStream outStream) throws IOException,
			ModelValidationException, SerialisationException {
		PrintStream out = new PrintStream(outStream);

		out.println("digraph work {");
		out.println("graph [nodesep=\"2.0\", overlap=false];");
		out.println("node [shape=box];");


		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualComponent) {
				VisualComponent comp = (VisualComponent) n;
				String id = model.getNodeReference(comp);
				if(id!=null) {
					Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
					if(bb!=null) {
						double width = bb.getWidth();
						double height = bb.getHeight();
						out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\", fixedsize=\"true\"];");
					}

					Set<Node> postset = model.getPostset(comp);

					for(Node target : postset) {
						String targetId = model.getNodeReference(target);
						if(targetId!=null) {
							out.println("\""+id+"\" -> \""+targetId+"\";");
						}
					}
				}
			}
		}
		out.println("}");
	}

	@Override
	public int getCompatibility(Model model) {
		if (model instanceof VisualModel)
			return Exporter.GENERAL_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	@Override
	public String getDescription() {
		return ".dot (GraphViz dot graph format)";
	}

	@Override
	public String getExtenstion() {
		return ".dot";
	}

	@Override
	public UUID getTargetFormat() {
		return Format.DOT;
	}
}
