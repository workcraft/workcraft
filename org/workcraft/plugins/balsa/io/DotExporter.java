package org.workcraft.plugins.balsa.io;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.serialisation.Format;

public class DotExporter implements Exporter {

	@Override
	public void export(Model model, OutputStream outStream) throws IOException,
			ModelValidationException, SerialisationException {
		PrintStream out = new PrintStream(outStream);

		out.println("digraph work {");
		out.println("graph [nodesep=\"2.0\"];");
		out.println("node [shape=box];");


		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualBreezeComponent) {
				VisualBreezeComponent comp = (VisualBreezeComponent) n;
				Integer id = model.getNodeID(comp);
				if(id!=null) {
					Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
					if(bb!=null) {
						double width = bb.getWidth();
						double height = bb.getHeight();
						out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\"];");
					}

					for (VisualHandshake hs : comp.getHandshakes())
					{
						Set<Node> postset = model.getPostset(hs);
						for(Node target : postset) {
							if (target instanceof VisualHandshake) {
								VisualBreezeComponent targetComp = (VisualBreezeComponent)((VisualHandshake)target).getParent();
								Integer targetId = model.getNodeID(targetComp);
								if(targetId!=null) {
									out.println("\""+id+"\" -> \""+targetId+"\";");
								}
							}
						}
					}
				}
			}
		}
		out.println("}");
	}

	@Override
	public int getCompatibility(Model model) {
		if (model instanceof VisualBalsaCircuit)
			return Exporter.BEST_COMPATIBILITY;
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
