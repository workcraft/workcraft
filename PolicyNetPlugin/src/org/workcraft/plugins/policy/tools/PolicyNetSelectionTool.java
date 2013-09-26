package org.workcraft.plugins.policy.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.tools.PetriNetSelectionTool;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.WorkspaceEntry;

public class PolicyNetSelectionTool extends PetriNetSelectionTool {
	private VisualPolicyNet visualModel;
	private PolicyNet model;
	protected JTable bundleTable;
	protected Map<String, String> bundleMap;

	public PolicyNetSelectionTool() {
		super();
	}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		visualModel = (VisualPolicyNet)editor.getModel();
		model = (PolicyNet)visualModel.getMathModel();
		createInterface();
	}


	@SuppressWarnings("serial")
	private final class BundleTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column==0) return "Bundle";
			return "Transitions";
		}

		@Override
		public int getRowCount() {
			return model.getBundles().size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object[] bundles = model.getBundles().toArray();
			return bundles[row];
		}
	}

	private final class BundleTableCellRendererImplementation implements	TableCellRenderer {
		JLabel label = new JLabel() {
			@Override
			public void paint(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
				super.paint(g);
			}
		};

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus,	int row, int column) {
			if (!(value instanceof Bundle)) return null;
			Bundle bundle = (Bundle)value;
			if (column == 0) {
				label.setText(model.getNodeReference(bundle));
				label.setForeground(Color.BLACK);
				label.setFont(label.getFont().deriveFont(Font.PLAIN));
			} else {
				label.setText(model.getBundleTransitionsAsString(bundle));
				label.setForeground(Color.BLACK);
				label.setFont(label.getFont().deriveFont(Font.PLAIN));
			}
			return label;
		}
	}

	private void createInterface() {
		bundleTable = new JTable(new BundleTableModel());
		bundleTable.setFillsViewportHeight(true);
		bundleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bundleTable.setDefaultRenderer(Object.class, new BundleTableCellRendererImplementation());
		infoPanel.setViewportView(bundleTable);
		infoPanel.setPreferredSize(new Dimension(1, 1));
		editor.getWorkspaceEntry().addObserver(new StateObserver() {
			@Override
			public void notify(StateEvent e) {
				bundleTable.tableChanged(new TableModelEvent(bundleTable.getModel()));
			}
		});
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}


	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		super.keyPressed(e);
		WorkspaceEntry we = e.getEditor().getWorkspaceEntry();

		if (e.isCtrlDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				Set<BundledTransition> transitions = new HashSet<BundledTransition>();
				VisualPolicyNet visualModel = (VisualPolicyNet)editor.getModel();
				for (Node node : visualModel.getSelection()) {
					if (node instanceof VisualBundledTransition) {
						transitions.add(((VisualBundledTransition)node).getReferencedTransition());
					}
				}
				if (!transitions.isEmpty()) {
					we.saveMemento();
					if (e.isShiftDown()) {
						((PolicyNet)visualModel.getMathModel()).unbundle(transitions);
						bundleTable.tableChanged(new TableModelEvent(bundleTable.getModel()));
					} else {
						((PolicyNet)visualModel.getMathModel()).bundle(transitions);
						bundleTable.tableChanged(new TableModelEvent(bundleTable.getModel()));
					}
				}
				break;
			}
		}
		editor.repaint();
	}

}
