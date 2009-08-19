package org.workcraft.plugins.layout;

import java.awt.Component;

import javax.swing.JFileChooser;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.gui.FileFilters;
import org.workcraft.layout.Layout;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

@DisplayName ("by Template")
public class TemplateLayout extends Component implements Layout{

	private static final long serialVersionUID = 1L;


	private void positionByTemplate(VisualSTG model, VisualSTG template) {
		for (VisualComponent vc: model.getVisualComponents()) {
			if (vc instanceof VisualSignalTransition) {
				for (VisualComponent vc2: template.getVisualComponents()) {
					if (vc2 instanceof VisualSignalTransition) {
						if (((VisualSignalTransition) vc2).getSignalName().equals(((VisualSignalTransition) vc).getSignalName())
								&&((VisualSignalTransition) vc2).getDirection()==((VisualSignalTransition) vc).getDirection())
						{
							vc.setX(vc2.getX());
							vc.setY(vc2.getY());
						}

					}
				}
			}
		}
	}

	@Override
	public void doLayout(VisualModel model) throws LayoutFailedException {
		if (!(model instanceof VisualSTG)) return;

		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);

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
		}
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Select template file");

	}

	@Override
	public boolean isApplicableTo(VisualModel model) {
		if (model instanceof VisualSTG) return true;
		return false;
	}

}
