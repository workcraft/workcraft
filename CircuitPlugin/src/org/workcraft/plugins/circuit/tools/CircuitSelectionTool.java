package org.workcraft.plugins.circuit.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.util.Hierarchy;

public class CircuitSelectionTool extends SelectionTool {

	private HashMap<VisualConnection, ScaleMode> connectionToScaleModeMap = null;

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		boolean processed = false;
		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			VisualModel model = e.getEditor().getModel();
			Node node = HitMan.hitTestForSelection(e.getPosition(), model);
			JPopupMenu popup = createPopupMenu(node, e.getEditor());
			if (popup != null) {
				MouseEvent systemEvent = e.getSystemEvent();
				popup.show(systemEvent.getComponent(), systemEvent.getX(), systemEvent.getY());
			}
			processed = true;
		}
		if (!processed) {
			super.mouseClicked(e);
		}
	}

	private JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
		JPopupMenu popup = new JPopupMenu();

		if (node instanceof VisualFunctionComponent) {
			final VisualFunctionComponent component = (VisualFunctionComponent)node;

			popup.setFocusable(false);
			popup.add(new JLabel("Function component"));
			popup.addSeparator();
			{
				JMenuItem addOutput = new JMenuItem("Add output (EAST)");
				addOutput.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						VisualCircuit vcircuit = (VisualCircuit)editor.getModel();
						vcircuit.getOrCreateContact(component, null, IOType.OUTPUT);
						component.setContactsDefaultPosition();
					}
				});
				popup.add(addOutput);
			}
			{
				JMenuItem addInput = new JMenuItem("Add input (WEST)");
				addInput.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						VisualCircuit vcircuit = (VisualCircuit)editor.getModel();
						vcircuit.getOrCreateContact(component, null, IOType.INPUT);
						component.setContactsDefaultPosition();
					}
				});
				popup.add(addInput);
			}
			popup.addSeparator();
			{
				JMenuItem defaultContactPosition = new JMenuItem("Set contacts in default position");
				defaultContactPosition.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						component.setContactsDefaultPosition();
					}
				});
				popup.add(defaultContactPosition);
			}
			{
				JMenuItem centerPivotPoint = new JMenuItem("Center pivot point");
				centerPivotPoint.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						component.centerPivotPoint();
					}
				});
				popup.add(centerPivotPoint);
			}
			return popup;
		}

		return null;
	}

	@Override
	public void beforeSelectionModification(final GraphEditor editor) {
		super.beforeSelectionModification(editor);
		// FIXME: A hack to preserve the shape of selected connections on relocation of their adjacent components (intro).
		// Flipping/rotation of VisualContacts are processed after ControlPoints of VisualConnections.
		// Therefore the shape of connections may change (e.g. if LOCK_RELATIVELY scale mode is selected).
		// To prevent this, the connection scale mode is temporary changed to NONE, and then restored (in afterSelectionModification).
        VisualModel model = editor.getModel();
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class);
        Collection<VisualConnection> includedConnections = SelectionHelper.getIncludedConnections(model.getSelection(), connections);
        connectionToScaleModeMap = new HashMap<VisualConnection, ScaleMode>();
        for (VisualConnection vc: includedConnections) {
            connectionToScaleModeMap.put(vc, vc.getScaleMode());
            vc.setScaleMode(ScaleMode.NONE);
        }
	}

	@Override
	public void afterSelectionModification(final GraphEditor editor) {
		if (connectionToScaleModeMap != null) {
			for (Entry<VisualConnection, ScaleMode> entry: connectionToScaleModeMap.entrySet()) {
				VisualConnection vc = entry.getKey();
				ScaleMode scaleMode = entry.getValue();
				vc.setScaleMode(scaleMode);
			}
		}
		super.afterSelectionModification(editor);
	}

}
