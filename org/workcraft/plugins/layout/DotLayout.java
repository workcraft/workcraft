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

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Framework;
import org.workcraft.PluginConsumer;
import org.workcraft.PluginProvider;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;

@DisplayName ("Layout using dot")
public class DotLayout implements Tool, PluginConsumer {
	PluginProvider pluginProvider;
	File tmp1 = null, tmp2 = null;

	private void saveGraph(VisualModel model, File file) throws IOException, ModelValidationException, SerialisationException {
		Exporter exporter = Export.chooseBestExporter(pluginProvider, model, Format.DOT);
		if (exporter == null)
			throw new RuntimeException ("Cannot find a .dot exporter for the model " + model);
		FileOutputStream out = new FileOutputStream(file);
		exporter.export(model, out);
		out.close();
	}

	List<Point2D> parseConnectionSpline(String pos)
	{
		ArrayList<Point2D> result = new ArrayList<Point2D>();
		Point2D end = null;
		String [] split = pos.split(" ");

		for(String s : split)
		{
			String [] ss = s.split(",");
			if(ss.length <2 || ss.length>3)
				throw new RuntimeException("bad connection position format");
			double pointsToInches = 1.0/72;
			if(ss.length == 3)
			{
				double x = Double.parseDouble(ss[1])*pointsToInches ;
				double y = Double.parseDouble(ss[2])*pointsToInches;
				Point2D p = new Point2D.Double(x,y);
				if(ss[0].equals("s"))
					result.add(0,p);
				else
					if(ss[0].equals("e"))
						end = p;
					else
						throw new RuntimeException("bad connection position format");
			}
			else
			{
				double x = Double.parseDouble(ss[0])*pointsToInches;
				double y = Double.parseDouble(ss[1])*pointsToInches;
				result.add(0,new Point2D.Double(x,y));
			}
		}

		if(end!=null)
			result.add(0,end);
		return result;
	}

	private void applyLayout(String in, VisualModel model) {

		System.out.println("=================================");
		System.out.println("dot output: ");
		System.out.println("=================================");
		System.out.println(in);

		System.out.println("=================================");
		Pattern regexp = Pattern.compile("\\s*\\\"?(.+)\\\"?\\s+\\[width=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*height=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*, fixedsize=true,\\s*pos=\\\"?(-?\\d*\\.?\\d+)\\s*,\\s*(-?\\d*\\.?\\d+)\\\"?\\];\\s*\\n");
		Pattern regexpConn = Pattern.compile("\\s*\\\"?(.+)\\\"?\\s+\\-\\>\\s+\\\"?(.+)\\\"?\\s+\\[pos=\\\"?([se,0-9\\ ]+)\\\"\\];\\n");
		Matcher matcher = regexp.matcher(in);

		while(matcher.find()) {
			String id = matcher.group(1);

			Node comp = model.getNodeByReference(id);

			if(comp==null || !(comp instanceof Movable))
				continue;
			Movable m = (Movable)comp;
			MovableHelper.resetTransform(m);
			MovableHelper.translate(m,
					Double.parseDouble(matcher.group(4))*1.0/72,
					Double.parseDouble(matcher.group(5))*1.0/72);
		}

		matcher = regexpConn.matcher(in);

		while(matcher.find())
		{
			String from = matcher.group(1);
			String to = matcher.group(2);
			String info = matcher.group(3);

			System.out.println("connection info found: " + info);

			Node comp1 = model.getNodeByReference(from);
			Node comp2 = model.getNodeByReference(to);
			Set<Connection> connections = model.getConnections(comp1);
			Connection con = null;
			for(Connection c : connections)
			{
				if(c.getSecond() == comp2)
					con = c;
			}
			if(con!=null)
			{
				System.out.println("Applying!");

				VisualConnection vc = (VisualConnection)con;
				vc.setConnectionType(ConnectionType.POLYLINE);

				Polyline poly = (Polyline)vc.getGraphic();
				poly.remove(poly.getChildren());
				List<Point2D> points = parseConnectionSpline(info);

				for(int i=points.size()-1;i>=0;i--)
				{
					Point2D p = points.get(i);
					ControlPoint cp = new ControlPoint();
					cp.setPosition(p);
					poly.add(cp);
				}
			}
		}

	}

	private void cleanUp() {
		if (tmp1 != null) {
			tmp1.delete();
			tmp1 = null;
		}
		if (tmp2 != null) {
			tmp2.delete();
			tmp2 = null;
		}
	}

	public void run (Model model, Framework framework) {
		try {
			tmp1 = File.createTempFile("work", ".dot");
			tmp2 = File.createTempFile("worklayout", ".dot");

			saveGraph((VisualModel)model, tmp1);
			SynchronousExternalProcess p = new SynchronousExternalProcess(
					new String[] {DotLayoutSettings.dotCommand, "-Tdot", "-o", tmp2.getAbsolutePath(), tmp1.getAbsolutePath()}, ".");
			p.start(10000);
			if(p.getReturnCode()==0) {
				String in = FileUtils.readAllText(tmp2);
				applyLayout(in, (VisualModel)model);
			}
			else
				throw new LayoutException("External process (dot) failed (code " + p.getReturnCode() +")\n\n"+new String(p.getOutputData())+"\n\n"+new String(p.getErrorData()) );
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (ModelValidationException e) {
			throw new RuntimeException(e);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} finally {
		//	cleanUp();
		}
	}

	public boolean isApplicableTo(Model model) {
		if (model instanceof VisualModel)
			return true;
		return false;
	}

	@Override
	public void processPlugins(PluginProvider pluginManager) {
		this.pluginProvider = pluginManager;
	}

	@Override
	public String getSection() {
		return "Layout";
	}
}