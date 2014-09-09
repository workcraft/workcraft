package org.workcraft.plugins.cpog.tools;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.expressions.CpogConnector;
import org.workcraft.plugins.cpog.expressions.CpogFormula;
import org.workcraft.plugins.cpog.expressions.javacc.CpogExpressionParser;
import org.workcraft.plugins.cpog.expressions.javacc.ParseException;
import org.workcraft.plugins.cpog.expressions.javacc.TokenMgrError;
import org.workcraft.util.Func;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogSelectionTool extends SelectionTool {

	final int margin = 4;
	final double minRadius = 2.0;
	final double expandRadius = 2.0;

	private JTextArea expressionText;

	public CpogSelectionTool() {
		super();
	}

	public CpogSelectionTool(boolean enablePages) {
		super(enablePages);
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);
		expressionText = new JTextArea();
		expressionText.setLineWrap(false);
		expressionText.setEditable(true);
		expressionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane expressionScroll = new JScrollPane(expressionText);

		JPanel buttonPanel = new JPanel();

		JButton btnInsert = new JButton("Insert");
		btnInsert.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				insertExpression(editor, expressionText.getText(), true);
			}
		});
		buttonPanel.add(btnInsert);

		JButton btnOverlay = new JButton("Overlay");
		btnOverlay.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				insertExpression(editor, expressionText.getText(), false);
			}
		});
		buttonPanel.add(btnOverlay);

		interfacePanel.add(expressionScroll, BorderLayout.CENTER);
		interfacePanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void insertExpression(final GraphEditor editor, final String text, final boolean createDuplicates) {
		WorkspaceEntry we = editor.getWorkspaceEntry();
		final VisualCPOG visualCpog = (VisualCPOG)we.getModelEntry().getVisualModel();
		we.captureMemento();

		final HashMap<String, VisualVertex> map = new HashMap<String, VisualVertex>();
		CpogFormula f = null;
		try {
			f = CpogExpressionParser.parse(text, new Func<String, CpogFormula>() {
				@Override
				public CpogFormula eval(String label) {
					if (map.containsKey(label)) return map.get(label);

					VisualVertex vertex = null;

					// TODO: Optimise!

					if (!createDuplicates)
						for(VisualVertex v : visualCpog.getVertices(visualCpog.getCurrentLevel()))
							if (v.getLabel().equals(label)) { vertex = v; break; }

					if (vertex == null)
					{
						vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
						vertex.setLabel(label);
						map.put(label, vertex);
					}
					return vertex;
				}
			});
		} catch (ParseException e) {
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (TokenMgrError e) {
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Lexical error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		visualCpog.selectNone();

		int n = map.size();
		int i = 0;
		for(VisualVertex v : map.values()) {
			double radius = Math.max(minRadius, expandRadius * n / Math.PI / 2.0);
			Point2D.Double pos = new Point2D.Double(radius * Math.cos(2.0 * Math.PI * i / n), radius * Math.sin(2.0 * Math.PI * i / n));
			v.setPosition(pos);
			visualCpog.addToSelection(v);
			i++;
		}
		CpogConnector cc = new CpogConnector(visualCpog);
		f.accept(cc);
		editor.requestFocus();

		// TODO: fix the bug after exception; find out if the line below is needed
		//       I think it is  fixed now (by not keeping a reference to the visualModel in the activated method)
		we.saveMemento();
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		boolean processed = false;

		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualModel model = e.getEditor().getModel();
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			if (node != null) {
				if(node instanceof VisualVariable) {
					VisualVariable var = (VisualVariable) node;
					var.toggle();
					processed = true;
				}
			}
		}

		if (!processed) {
			super.mouseClicked(e);

		}
	}

}
