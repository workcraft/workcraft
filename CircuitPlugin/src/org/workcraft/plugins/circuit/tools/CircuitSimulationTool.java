package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.stg.SignalStg;
import org.workcraft.plugins.circuit.stg.StgGenerator;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CircuitSimulationTool extends StgSimulationTool {
	private JButton copyInitButton;
	private StgGenerator generator;

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		VisualCircuit visualCircuit = (VisualCircuit)model;
		generator = new StgGenerator(visualCircuit);
		VisualSTG visualStg = generator.getStg();
//		Framework framework = Framework.getInstance();
//		File workingDirectory = null;
//		try {
//			String title = visualCircuit.getTitle();
//			File envFile = visualCircuit.getEnvironmentFile();
//			boolean hasEnvironment = ((envFile != null) && envFile.exists());
//
//			workingDirectory = FileUtils.createTempDirectory(title + "-");
//
//			STG devStg = (STG)visualStg.getMathModel();
//			Exporter devStgExporter = Export.chooseBestExporter(framework.getPluginManager(), devStg, Format.STG);
//			if (devStgExporter == null) {
//				throw new RuntimeException ("Exporter not available: model class " + devStg.getClass().getName() + " to format STG.");
//			}
//
//			// Generating .g for the circuit
//			String devStgName = (hasEnvironment ? "dev.g" : "system.g");
//			File devStgFile =  new File(workingDirectory, devStgName);
//			ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getCanonicalPath());
//			Result<? extends Object> devExportResult = framework.getTaskManager().execute(
//					devExportTask, "Exporting circuit .g", null);
//
//			if (devExportResult.getOutcome() == Outcome.FINISHED) {
//				// Generating .g for the environment
//				STG stg;
//				File stgFile = null;
//				Result<? extends ExternalProcessResult>  pcompResult = null;
//				if ( !hasEnvironment ) {
//					stgFile = devStgFile;
//					stg = devStg;
//				} else {
//					File envStgFile = null;
//					if (envFile.getName().endsWith(".g")) {
//						envStgFile = envFile;
//					} else {
//						STG envStg = (STG)framework.loadFile(envFile).getMathModel();
//						Exporter envStgExporter = Export.chooseBestExporter(framework.getPluginManager(), envStg, Format.STG);
//						envStgFile = new File(workingDirectory, "env.g");
//						ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getCanonicalPath());
//						Result<? extends Object> envExportResult = framework.getTaskManager().execute(
//								envExportTask, "Exporting environment .g", null);
//
//						if (envExportResult.getOutcome() == Outcome.FINISHED) {
//
//							// Generating .g for the whole system (circuit and environment)
//							stgFile = new File(workingDirectory, "system.g");
//							PcompTask pcompTask = new PcompTask(new File[]{devStgFile, envStgFile}, ConversionMode.OUTPUT, true, false, workingDirectory);
//							pcompResult = framework.getTaskManager().execute(
//									pcompTask, "Running pcomp", null);
//
//							if (pcompResult.getOutcome() == Outcome.FINISHED) {
//								FileUtils.writeAllText(stgFile, new String(pcompResult.getReturnValue().getOutput()));
//								WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(stgFile, true);
//								stg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
//								visualStg = new VisualSTG(stg);
//							}
//						}
//					}
//				}
//			}
//		} catch (Throwable e) {
//		} finally {
//			if ((workingDirectory != null) && !MpsatUtilitySettings.getDebugTemporaryFiles()) {
//				FileUtils.deleteDirectoryTree(workingDirectory);
//			}
//		}
		return visualStg;
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);
		copyInitButton = new JButton("Copy init");
		copyInitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (VisualContact contact : Hierarchy.getDescendantsOfType(editor.getModel().getRoot(), VisualContact.class)) {
					SignalStg signalStg = generator.getSignalStg(contact);
					if (signalStg != null) {
						contact.getReferencedContact().setInitToOne(signalStg.P1.getReferencedPlace().getTokens() == 1);
					}
				}
			}
		});
		controlPanel.add(copyInitButton);
	}

	@Override
	public void initialiseSignalState() {
		super.initialiseSignalState();
		for (String signalName: stateMap.keySet()) {
			SignalState signalState = stateMap.get(signalName);
			Node zeroNode = net.getNodeByReference(signalName + "_0");
			if (zeroNode instanceof Place) {
				Place zeroPlace = (Place)zeroNode;
				signalState.value = ((zeroPlace.getTokens() > 0) ? 0 : 1);
			}
			Node oneNode= net.getNodeByReference(signalName + "_1");
			if (oneNode instanceof Place) {
				Place onePlace = (Place)oneNode;
				signalState.value = ((onePlace.getTokens() > 0) ? 1 : 0);
			}
		}
	}

	// return first enabled transition
	public SignalTransition isContactExcited(VisualContact contact) {
		SignalTransition result = null;
		boolean up = false;
		boolean down = false;
		if (generator.isDriver(contact)) {
			SignalStg signalStg = generator.getSignalStg(contact);
			for (VisualSignalTransition transition : signalStg.getAllTransitions()) {
				if (net.isEnabled(transition.getReferencedTransition())) {
					if (result == null) {
						result = transition.getReferencedTransition();
					}
					if (transition.getDirection() == Direction.MINUS) {
						down = true;
					}
					if (transition.getDirection() == Direction.PLUS) {
						up = true;
					}
					if (up && down) {
						break;
					}
				}
			}
			if (up && down) {
				result = null;
			}
		}
		return result;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return node instanceof VisualContact;
				}
			});

		if (node != null) {
			SignalTransition st = isContactExcited((VisualContact) node);
			if (st != null) {
				executeTransition(e.getEditor(), st);
			}
		}
	}

	@Override
	protected boolean isContainerExcited(Container container) {
		if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
		boolean ret = false;

		for (Node node: container.getChildren()) {

			if (node instanceof VisualContact) {
				ret=ret || isContactExcited((VisualContact)node) != null;
			}

			if (node instanceof Container) {
				ret = ret || isContainerExcited((Container)node);
			}

			if (ret) break;
		}

		excitedContainers.put(container, ret);
		return ret;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {

				if (node instanceof VisualContact) {
					VisualContact contact = (VisualContact) node;
					String transitionId = null;
					Node transition2 = null;
					if (branchTrace.canProgress()) {
						transitionId = branchTrace.getCurrent();
						transition2 = net.getNodeByReference(transitionId);
					} else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
						transitionId = mainTrace.getCurrent();
						transition2 = net.getNodeByReference(transitionId);
					}

					SignalStg signalStg = generator.getSignalStg(contact);
					if (signalStg != null) {
						if (signalStg.getAllTransitions().contains(transition2)) {
							return new Decoration() {
								@Override
								public Color getColorisation() {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								}
								@Override
								public Color getBackground() {
									return CommonSimulationSettings.getEnabledForegroundColor();
								}
							};
						}
						final boolean isOne = (signalStg.P1.getReferencedPlace().getTokens() == 1);
						final boolean isZero = (signalStg.P0.getReferencedPlace().getTokens() == 1);
						final boolean isExcited = (isContactExcited(contact) != null);
						return new Decoration() {
							@Override
							public Color getColorisation() {
								if (isExcited) {
									return CommonSimulationSettings.getEnabledForegroundColor();
								}
								return null;
							}
							@Override
							public Color getBackground() {
								if (isExcited) {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								} else {
									if (isOne && !isZero) {
										return CircuitSettings.getActiveWireColor();
									}
									if (!isOne && isZero) {
										return CircuitSettings.getInactiveWireColor();
									}
								}
								return null;
							}
						};
					}
				} else if ((node instanceof VisualJoint) || (node instanceof VisualCircuitConnection)) {
					SignalStg signalStg = generator.getSignalStg((VisualNode)node);
					if (signalStg != null) {
						final boolean isOne = (signalStg.P1.getReferencedPlace().getTokens() == 1);
						final boolean isZero = (signalStg.P0.getReferencedPlace().getTokens() == 1);
						return new Decoration() {
							@Override
							public Color getColorisation() {
								if (isOne && !isZero) {
									return CircuitSettings.getActiveWireColor();
								}
								if (!isOne && isZero) {
									return CircuitSettings.getInactiveWireColor();
								}
								return null;
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					}
				} else if (node instanceof VisualPage || node instanceof VisualGroup) {
					final boolean ret = isContainerExcited((Container)node);
					return new ContainerDecoration() {
						@Override
						public Color getColorisation() {
							return null;
						}
						@Override
						public Color getBackground() {
							return null;
						}
						@Override
						public boolean isContainerExcited() {
							return ret;
						}
					};

				}

				return null;
			}
		};
	}
}
