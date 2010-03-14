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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.Framework;
import org.workcraft.PluginConsumer;
import org.workcraft.PluginProvider;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;

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

	private static String fileToString(File file) throws IOException {
		FileInputStream f = new FileInputStream(file);
		byte[] buf = new byte[f.available()];
		f.read(buf);
		return new String(buf);
	}

	private void applyLayout(String in, VisualModel model) {
		Pattern regexp = Pattern.compile("\\s*\\\"?(.+)\\\"?\\s+\\[width=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*height=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*pos=\\\"?(-?\\d+)\\s*,\\s*(-?\\d+)\\\"?\\];\\s*\\n");
		Matcher matcher = regexp.matcher(in);

		while(matcher.find()) {
			String id = matcher.group(1);

			Node comp = model.getNodeByReference(id);

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
				String in = fileToString(tmp2);
				applyLayout(in, (VisualModel)model);
			}
			else
				throw new RuntimeException("External process (dot) failed (code " + p.getReturnCode() +")\n\n"+new String(p.getOutputData())+"\n\n"+new String(p.getErrorData()) );
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (ModelValidationException e) {
			throw new RuntimeException(e);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} finally {
			cleanUp();
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