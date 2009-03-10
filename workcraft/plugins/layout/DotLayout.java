package org.workcraft.plugins.layout;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.layout.Layout;

public class DotLayout implements Layout {

	private static final double dotPositionScaleFactor = 0.02;

	private static final String tmpGraphFilePath = "tmp.dot";
	private static final String dotCommandPath = "\"C:/Program Files/Graphviz2.20/bin/dot\"";

	private void saveGraph(VisualModel model) throws IOException {
		PrintStream out = new PrintStream(new File(tmpGraphFilePath));
		out.println("digraph work {");
		out.println("graph [nodesep=\"2.0\"];");
		out.println("node [shape=box];");
		for (VisualNode n : model.getRoot().getChildren()) {
			if (n instanceof VisualComponent) {
				VisualComponent comp = (VisualComponent) n;
				Integer id = comp.getReferencedComponent().getID();
				if(id!=null) {
					Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
					if(bb!=null) {
						double width = bb.getWidth();
						double height = bb.getHeight();
						out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\"];");
					}
					Set<VisualComponent> postset = comp.getPostset();
					for(VisualComponent target : postset) {
						Integer targetId = target.getReferencedComponent().getID();
						if(targetId!=null) {
							out.println("\""+id+"\" -> \""+targetId+"\";");
						}
					}
				}
			}
		}
		out.println("}");
		out.close();
	}

	private static String fileToString(String path) throws IOException {
		FileInputStream f = new FileInputStream(path);
		byte[] buf = new byte[f.available()];
		f.read(buf);
		return new String(buf);
	}

	private void applyLayout(String in, VisualModel model) {
		Pattern regexp = Pattern.compile("\\s*\\\"?(.+)\\\"?\\s+\\[width=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*height=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*pos=\\\"?(-?\\d+)\\s*,\\s*(-?\\d+)\\\"?\\];\\s*\\n");
		Matcher matcher = regexp.matcher(in);

		while(matcher.find()) {
			Integer id = Integer.parseInt(matcher.group(1));
			VisualComponent comp = model.getComponentByRefID(id);
			if(comp==null)
				continue;
			comp.setX(Integer.parseInt(matcher.group(4))*dotPositionScaleFactor);
			comp.setY(-Integer.parseInt(matcher.group(5))*dotPositionScaleFactor);
		}

	}

	private void cleanUp() {
		(new File(tmpGraphFilePath)).delete();
	}

	public void doLayout(VisualModel model) throws LayoutFailedException {
		try {
			saveGraph(model);
			SynchronousExternalProcess p = new SynchronousExternalProcess(
					new String[] {dotCommandPath, "-Tdot", "-O", tmpGraphFilePath}, ".");
			p.start(10000);
			if(p.getReturnCode()==0) {
				String in = fileToString(tmpGraphFilePath+".dot");
				applyLayout(in, model);
				cleanUp();
			}
			else {
				cleanUp();
				throw new LayoutFailedException("External process (dot) failed.");
			}
		} catch(IOException e) {
			cleanUp();
			throw new LayoutFailedException(e.getMessage());
		}
	}

	public String getDisplayName() {
		return "Dot layout";
	}

	public boolean isApplicableTo(VisualModel model) {
		return true;
	}

}
