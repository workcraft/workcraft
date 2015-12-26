package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class TransformationResultHandler extends DummyProgressMonitor<TransformationResult> {
	private final WorkspaceEntry we;
	private final boolean convertResultStgToPetriNet;

	public TransformationResultHandler(WorkspaceEntry we) {
		this(we, true);
	}

	public TransformationResultHandler(WorkspaceEntry we, boolean convertResultStgToPetriNet) {
		this.we = we;
		this.convertResultStgToPetriNet = convertResultStgToPetriNet;
	}

	@Override
	public void finished(final Result<? extends TransformationResult> result, String description) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Framework framework = Framework.getInstance();
				Path<String> path = we.getWorkspacePath();
				if (result.getOutcome() == Outcome.FINISHED) {
					STGModel stgModel = result.getReturnValue().getResult();
					PetriNetModel model = (convertResultStgToPetriNet ? stgModel : convertStgToPetriNet(stgModel));
					final Workspace workspace = framework.getWorkspace();
					final Path<String> directory = path.getParent();
					final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
					final ModelDescriptor modelDescriptor = (convertResultStgToPetriNet ? new StgDescriptor() : new PetriNetDescriptor());
					final ModelEntry me = new ModelEntry(modelDescriptor, model);
					boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
					workspace.add(directory, name, me, true, openInEditor);
				} else {
					MainWindow mainWindow = framework.getMainWindow();
					if (result.getCause() == null) {
						Result<? extends ExternalProcessResult> petrifyResult = result.getReturnValue().getPetrifyResult();
						JOptionPane.showMessageDialog(mainWindow,
								"Petrify output: \n\n" + new String(petrifyResult.getReturnValue().getErrors()),
								"Transformation failed", JOptionPane.WARNING_MESSAGE);
					} else {
						ExceptionDialog.show(mainWindow, result.getCause());
					}
				}
			}
		});
	}

	private PetriNetModel convertStgToPetriNet(STGModel srcModel) {
		PetriNet dstModel = new PetriNet();
		HashMap<Node, Node> nodeMap = new HashMap<>();
		for (Place place: srcModel.getPlaces()) {
			Place newPlace = dstModel.createPlace(null, null);
			if (newPlace != null) {
				newPlace.setCapacity(place.getCapacity());
				newPlace.setTokens(place.getTokens());
				nodeMap.put(place, newPlace);
			}
		}

		for (Transition transition: srcModel.getTransitions()) {
			String srcName = srcModel.getName(transition);
			String dstName = convertName(srcModel, dstModel, srcName);
			Transition newTransition = dstModel.createTransition(dstName, null);
			nodeMap.put(transition, newTransition);
		}

		for (Connection connection: srcModel.getConnections()) {
			Node first = nodeMap.get(connection.getFirst());
			Node second = nodeMap.get(connection.getSecond());
			try {
				Connection newConnection = dstModel.connect(first, second);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
		return dstModel;
	}

	private String convertName(STGModel srcModel, PetriNet dstModel, String srcName) {
		String candidateName = LabelParser.getTransitionName(srcName);
		candidateName = candidateName.replace("+", "_PLUS").replace("-", "_MINUS").replace("~", "_TOGGLE");

		HierarchicalUniqueNameReferenceManager refManager
			= (HierarchicalUniqueNameReferenceManager)dstModel.getReferenceManager();

		NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(dstModel.getRoot());
		NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
		String name = nameManagerer.getDerivedName(null, candidateName);
		return name;
	}

}
