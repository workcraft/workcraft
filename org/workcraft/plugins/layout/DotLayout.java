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

package org.workcraft.plugins.layout;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.LayoutFailedException;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.layout.Layout;

@DisplayName ("Dot")
public class DotLayout implements Layout {

	private void saveGraph(VisualModel model) throws IOException {
		PrintStream out = new PrintStream(new File(DotLayoutSettings.tmpGraphFilePath));
		out.println("digraph work {");
		out.println("graph [nodesep=\"2.0\"];");
		out.println("node [shape=box];");


		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualComponent) {
				VisualComponent comp = (VisualComponent) n;
				Integer id = model.getNodeID(comp);
				if(id!=null) {
					Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
					if(bb!=null) {
						double width = bb.getWidth();
						double height = bb.getHeight();
						out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\"];");
					}

					Set<Node> postset = model.getPostset(comp);

					for(Node target : postset) {
						Integer targetId = model.getNodeID(target);
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

			Node comp = model.getNodeByID(id);

			if(comp==null || !(comp instanceof Movable))
				continue;
			Movable m = (Movable)comp;
			MovableHelper.resetTransform(m);
			MovableHelper.translate(m,
					Integer.parseInt(matcher.group(4))*DotLayoutSettings.dotPositionScaleFactor,
					-Integer.parseInt(matcher.group(5))*DotLayoutSettings.dotPositionScaleFactor);
		}
	}

	private void cleanUp() {
		(new File(DotLayoutSettings.tmpGraphFilePath)).delete();
	}

	public void doLayout(VisualModel model) throws LayoutFailedException {
		try {
			saveGraph(model);
			SynchronousExternalProcess p = new SynchronousExternalProcess(
					new String[] {DotLayoutSettings.dotCommand, "-Tdot", "-O", DotLayoutSettings.tmpGraphFilePath}, ".");
			p.start(10000);
			if(p.getReturnCode()==0) {
				String in = fileToString(DotLayoutSettings.tmpGraphFilePath+".dot");
				applyLayout(in, model);
				cleanUp();
			}
			else {
				cleanUp();
				throw new LayoutFailedException("External process (dot) failed (code " + p.getReturnCode() +")\n\n"+new String(p.getOutputData())+"\n\n"+new String(p.getErrorData()) );
			}
		} catch(IOException e) {
			cleanUp();
			throw new LayoutFailedException(e);
		}
	}

	public boolean isApplicableTo(VisualModel model) {
		return true;
	}


}
