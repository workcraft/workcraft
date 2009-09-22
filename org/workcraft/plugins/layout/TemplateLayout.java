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

import java.awt.Component;

import javax.swing.JFileChooser;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.LayoutFailedException;
import org.workcraft.gui.FileFilters;
import org.workcraft.layout.Layout;
import org.workcraft.plugins.stg.VisualSTG;

@DisplayName ("by Template")
public class TemplateLayout extends Component implements Layout{

	private static final long serialVersionUID = 1L;


	/*private void positionByTemplate(VisualSTG model, VisualSTG template) {
		for (HierarchyNode vc: model.getVisualComponents()) {
			if (vc instanceof VisualSignalTransition) {
				for (HierarchyNode vc2: template.getVisualComponents()) {
					if (vc2 instanceof VisualSignalTransition) {
						VisualSignalTransition st1 = (VisualSignalTransition) vc;
						VisualSignalTransition st2 = (VisualSignalTransition) vc2;

						if (st2.getSignalName().equals(st1.getSignalName())
								&& st2.getDirection()==st1.getDirection())
							st1.setTransform(st2.getTransform());

					}
				}
			}
		}
	}*/

	@Override
	public void doLayout(VisualModel model) throws LayoutFailedException {
		if (!(model instanceof VisualSTG)) return;

		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);

		/*
		try {
//			if (lastOpenPath != null)
//			fc.setCurrentDirectory(new File("D:/workspace/async_router"));

			fc.setMultiSelectionEnabled(false);
			fc.setDialogTitle("Template model");

			if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
					Model t_model = Framework.load(fc.getSelectedFile().getPath());
					if (t_model instanceof VisualSTG)
						positionByTemplate((VisualSTG)model, (VisualSTG)t_model);
			}

		} catch (LoadFromXMLException e) {
			throw new LayoutFailedException(e);
		} */
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Select template file");

	}

	@Override
	public boolean isApplicableTo(VisualModel model) {
		if (model instanceof VisualSTG) return true;
		return false;
	}

}
