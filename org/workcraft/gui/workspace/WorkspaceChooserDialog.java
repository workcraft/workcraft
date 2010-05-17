package org.workcraft.gui.workspace;

import info.clearthought.layout.TableLayout;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.workcraft.gui.WorkspaceTreeDecorator;
import org.workcraft.gui.trees.FilteredTreeSource;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class WorkspaceChooserDialog extends JDialog {
	private final Workspace workspace;
	private final Func<Path<String>, Boolean> filter;
	private TreeWindow<Path<String>> tree;
	private JPanel buttonsPanel;
	private JButton cancelButton;
	private JTextField nameFilter;
	private FilteredTreeSource<Path<String>> filteredSource;

	public WorkspaceChooserDialog(Window parent, String title, Workspace workspace, Func<Path<String>, Boolean> filter) {
		super(parent, title, ModalityType.APPLICATION_MODAL);
		this.workspace = workspace;
		this.filter = filter;

		this.setContentPane(createContents());
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		JButton runButton = new JButton ("OK");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//modalResult = 1;
				setVisible(false);
			}
		});

		cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//modalResult = 0;
				setVisible(false);
			}
		});

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	private void expand(Path<String> node) {
		if (filteredSource.isLeaf(node)) {
			 if (filter.eval(node))
				 tree.makeVisible(filteredSource.getPath(node));
		} else {
			for (Path<String> n : filteredSource.getChildren(node))
				expand(n);
		}
	}

	private void updateFilter() {
		filteredSource.setFilter(new Func<Path<String>, Boolean>() {
			@Override
			public Boolean eval(Path<String> arg) {
				return filter.eval(arg) && arg.getNode().contains(nameFilter.getText());
			}
		});

		expand(filteredSource.getRoot());
	}

	private Container createContents() {

		double[][] sizes = {
				{ TableLayout.FILL } ,
				{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED }
				};

		 JPanel contents = new JPanel(new TableLayout(sizes));

		 nameFilter = new JTextField();

		 nameFilter.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilter();
			}
		});

		 contents.add(GUI.createWideLabeledComponent(nameFilter, "Search:"), "0 0");

		 filteredSource = new FilteredTreeSource<Path<String>>(workspace.getTree(), filter);

		 tree = TreeWindow.create(filteredSource, new WorkspaceTreeDecorator(workspace), null);

		 expand(filteredSource.getRoot());

		 contents.add(tree, "0 1");

		 createButtonsPanel();

		 contents.add(buttonsPanel, "0 2");

		 return contents;
	}

	public List<WorkspaceEntry> choose() {
		LinkedList<WorkspaceEntry> result = new LinkedList<WorkspaceEntry>();

		setVisible(true);

		return result;
	}
}