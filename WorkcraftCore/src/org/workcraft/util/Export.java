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

package org.workcraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;

public class Export {
	public static class ExportTask implements Task<Object> {
		Exporter exporter;
		Model model;
		File file;

		public ExportTask(Exporter exporter, Model model, String path) {
			this.exporter = exporter;
			this.model = model;
			this.file = new File(path);
		}

		@Override
		public Result<? extends Object> run(ProgressMonitor<? super Object> monitor) {
			FileOutputStream fos;

			String message = "Exporting model ";
			if (!model.getTitle().isEmpty()) {
				message += "\'" + model.getTitle() + "\' ";
			}
			message += "to file \'" + file.getAbsolutePath() + "\'.";
			System.out.println(message);
			try {
				file.createNewFile();
				fos = new FileOutputStream(file);
			} catch (IOException e) {
				return new Result<Boolean>(e);
			}

			boolean ok = false;

			try	{
				if (model instanceof VisualModel) {
					if (exporter.getCompatibility(model) == Exporter.NOT_COMPATIBLE) {
						if (exporter.getCompatibility(((VisualModel)model).getMathModel()) == Exporter.NOT_COMPATIBLE) {
								return new Result<Boolean>(new Exception(new RuntimeException("Exporter is not applicable to the model.")));
						} else {
							model = ((VisualModel)model).getMathModel();
						}
					}
				}
				exporter.export(model, fos);
				ok = true;
			} catch (Throwable e) {
				return new Result<Boolean>(e);
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					return new Result<Boolean>(e);
				}
				if(!ok) {
					file.delete();
				}
			}

			return new Result<Boolean>(Outcome.FINISHED);
		}
	}

	static public Exporter chooseBestExporter (PluginProvider provider, Model model, UUID targetFormat) {
		Iterable<PluginInfo<? extends Exporter>> plugins = provider.getPlugins(Exporter.class);

		Exporter best = null;
		int bestCompatibility = Exporter.NOT_COMPATIBLE;

		for (PluginInfo<? extends Exporter> info : plugins) {
			Exporter exporter = info.getSingleton();

			if (exporter.getTargetFormat().equals(targetFormat)) {
				int compatibility = exporter.getCompatibility(model);
				if (compatibility > bestCompatibility) {
					bestCompatibility = compatibility;
					best = exporter;
				}
			}
		}

		return best;
	}

	static public void exportToFile (Model model, File file, UUID targetFormat, PluginProvider provider) throws IOException, ModelValidationException, SerialisationException {
		Exporter exporter = chooseBestExporter(provider, model, targetFormat);
		if (exporter == null)
			throw new SerialisationException("No exporter available for model type " + model.getDisplayName() + " to produce format " + Format.getDescription(targetFormat));
		exportToFile(exporter, model, file);
	}

	static public ExportTask createExportTask(Model model, File file, UUID targetFormat, PluginProvider provider) throws SerialisationException {
		Exporter exporter = chooseBestExporter(provider, model, targetFormat);
		if (exporter == null)
			throw new SerialisationException("No exporter available for model type " + model.getDisplayName() + " to produce format " + Format.getDescription(targetFormat));
		return new ExportTask(exporter, model, file.getAbsolutePath());
	}

	static public void exportToFile(Exporter exporter, Model model, File file) throws IOException, ModelValidationException, SerialisationException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		boolean ok = false;
		try {
			if (model instanceof VisualModel) {
				if (exporter.getCompatibility(model) == Exporter.NOT_COMPATIBLE) {
					if (exporter.getCompatibility(((VisualModel)model).getMathModel()) == Exporter.NOT_COMPATIBLE) {
						throw new RuntimeException ("Exporter is not applicable to the model.");
					} else {
						model = ((VisualModel)model).getMathModel();
					}
				}
			}
			exporter.export(model, fos);
			ok = true;
		} finally {
			fos.close();
			if ( !ok ) {
				file.delete();
			}
		}
	}

	static public void exportToFile (Exporter exporter, Model model, String fileName) throws IOException, ModelValidationException, SerialisationException {
		exportToFile(exporter, model, new File(fileName));
	}
}
