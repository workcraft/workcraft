package org.workcraft.plugins.interop;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.serialisation.Format;

public class DotExporter implements Exporter {

	public static void export(DotExportable model, OutputStream outStream) throws IOException
	{
		PrintStream out = new PrintStream(outStream);

		out.println("digraph work {");
		out.println("graph [nodesep=\"0.5\", overlap=\"false\", splines=\"true\"];");
		out.println("node [shape=box];");

		for (DotExportNode node : model.getNodes()) {
			String id = node.getName();
			Dimension2D dimensions = node.getDimensions();
			double width = dimensions.getWidth();
			double height = dimensions.getHeight();
			out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\", fixedsize=\"true\"];");

			for(String target : node.getOutgoingArcs()) {
				out.println("\""+id+"\" -> \""+target+"\";");
			}
		}
		out.println("}");
	}

	@Override
	public void export(Model model, OutputStream outStream) throws IOException,
			ModelValidationException, SerialisationException {

		final List<DotExportNode> export = new ArrayList<DotExportNode>();
		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualComponent) {
				VisualComponent comp = (VisualComponent) n;
				final String id = model.getNodeReference(comp);

				if(id!=null) {
					final Rectangle2D bb = comp.getBoundingBoxInLocalSpace();

					if(bb!=null)
					{
						final List<String> destinations = new ArrayList<String>();


						Set<Node> postset = model.getPostset(comp);

						for(Node target : postset) {
							String targetId = model.getNodeReference(target);
							if(targetId!=null)
								destinations.add(targetId);
						}

						export.add(new DotExportNode()
						{
							@Override
							public String getName() {
								return id;
							}

							@Override
							public Dimension2D getDimensions() {
								return new Dimension2D()
								{
									@Override
									public double getHeight() {
										return bb.getHeight();
									}
									@Override
									public double getWidth() {
										return bb.getWidth();
									}
									@Override
									public void setSize(double width, double height) {
										throw new NotSupportedException();
									}
								};
							}

							@Override
							public String[] getOutgoingArcs() {
								return destinations.toArray(new String[0]);
							}
						});
					}
				}
			}
		}

		export(new DotExportable() {
			@Override
			public DotExportNode[] getNodes() {
				return export.toArray(new DotExportNode[0]);
			}
		}, outStream);
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
