package org.workcraft.plugins.cpog.expressions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.gui.graph.tools.ExpressionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.expressions.javacc.CpogExpressionParser;
import org.workcraft.plugins.cpog.expressions.javacc.ParseException;
import org.workcraft.plugins.cpog.expressions.javacc.TokenMgrError;
import org.workcraft.util.Func;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogExpressionTool extends ExpressionTool implements ActionListener {

	final int margin = 4;
	final double minRadius = 2.0;
	final double expandRadius = 2.0;

	private JScrollPane scrollExpression;
	private JTextArea txtExpression;
	private JPanel buttonPanel = new JPanel();

	private JButton btnInsert = new JButton("Insert");
	private JButton btnOverlay = new JButton("Overlay");
	private JButton btnClear = new JButton("Clear");

	private WorkspaceEntry workspaceEntry = null;
	private VisualCPOG visualCpog = null;

	private JPanel interfacePanel = new JPanel();

	public CpogExpressionTool () {
		txtExpression = new JTextArea();
		txtExpression.setLineWrap(false);
		txtExpression.setEditable(true);

		txtExpression.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		scrollExpression = new JScrollPane();
		scrollExpression.setViewportView(txtExpression);


		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		buttonPanel.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(btnInsert);
		buttonPanel.add(Box.createRigidArea(new Dimension(margin, 0)));
		buttonPanel.add(btnOverlay);
		buttonPanel.add(Box.createRigidArea(new Dimension(margin, 0)));
		buttonPanel.add(btnClear);


		interfacePanel.setLayout(new BorderLayout(0,0));
		interfacePanel.add(scrollExpression, BorderLayout.CENTER);
		interfacePanel.add(buttonPanel, BorderLayout.PAGE_END);

		btnClear.addActionListener(this);
		btnInsert.addActionListener(this);
		btnOverlay.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if (a.getSource()==btnClear) {
			txtExpression.setText("");
		}
		if (a.getSource()==btnInsert) {
			insertExpression(txtExpression.getText(), true);
		}
		if (a.getSource()==btnOverlay) {
			insertExpression(txtExpression.getText(), false);
		}
	}

	private void insertExpression(final String text, final boolean createDuplicates)
	{
		workspaceEntry.captureMemento();

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
			workspaceEntry.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (TokenMgrError e) {
			workspaceEntry.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Lexical error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		visualCpog.selectNone();

		int n = map.size();
		int i = 0;

		for(VisualVertex v : map.values())
		{
			double radius = Math.max(minRadius, expandRadius * n / Math.PI / 2.0);
			Point2D.Double pos = new Point2D.Double(radius * Math.cos(2.0 * Math.PI * i / n), radius * Math.sin(2.0 * Math.PI * i / n));
			v.setPosition(pos);
			visualCpog.addToSelection(v);
			i++;
		}

		CpogConnector cc = new CpogConnector(visualCpog);
		f.accept(cc);

		// TODO: fix the bug after exception; find out if the line below is needed
		workspaceEntry.setChanged(true);
		workspaceEntry.saveMemento();
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);

		workspaceEntry = editor.getWorkspaceEntry();
		visualCpog  = (VisualCPOG)editor.getModel();
	}
}