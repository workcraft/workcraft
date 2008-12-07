package org.workcraft.gui.history;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.workcraft.framework.Framework;
import org.workcraft.framework.HistoryEvent;
import org.workcraft.framework.HistoryListener;
import org.workcraft.framework.HistoryProvider;

@SuppressWarnings("serial")
public class HistoryView extends JInternalFrame implements HistoryListener {
	Framework framework;
	HistoryProvider provider = null;
	HistoryListModel listModel = null;


	private JPanel jContentPane = null;
	private JList listHistory = null;
	private JPanel panelHistoryButtons = null;
	private JButton btnUndo = null;
	private JButton btnRedo = null;

	/**
	 * This is the xxx default constructor
	 */
	public HistoryView(Framework framework) {
		super();
		initialize();
		this.framework = framework;
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		setResizable(true);
		setTitle("History");
		setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (this.jContentPane == null) {
			this.jContentPane = new JPanel();
			this.jContentPane.setLayout(new BorderLayout());
			this.jContentPane.add(getListHistory(), BorderLayout.CENTER);
			this.jContentPane.add(getPanelHistoryButtons(), BorderLayout.NORTH);
		}
		return this.jContentPane;
	}

	/**
	 * This method initializes listHistory
	 *
	 * @return javax.swing.JList
	 */
	private JList getListHistory() {
		if (this.listHistory == null) {
			this.listHistory = new JList();
			this.listHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return this.listHistory;
	}

	public void setProvider(HistoryProvider provider) {
		// detach from previous provider
		if (this.provider != null)
			this.provider.removeHistoryListener(this);

		// populate list of existing events
		List <HistoryEvent> events = provider.getHistory();
		this.listModel = new HistoryListModel (events);
		getListHistory().setModel(this.listModel);

		// attach to new provider
		this.provider = provider;
		this.provider.addHistoryListener(this);
	}

	public HistoryProvider getProvider() {
		return this.provider;
	}

	public void eventAdded(HistoryEvent event) {
		this.listModel.events.add(event);
		getListHistory().setSelectedIndex(this.listModel.events.size()-1);
	}

	public void movedToState(int index) {

	}

	public void redoHistoryDiscarded() {

	}

	/**
	 * This method initializes panelHistoryButtons
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelHistoryButtons() {
		if (this.panelHistoryButtons == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.gridy = 0;
			this.panelHistoryButtons = new JPanel();
			this.panelHistoryButtons.setLayout(new GridBagLayout());
			this.panelHistoryButtons.setPreferredSize(new Dimension(0, 25));
			this.panelHistoryButtons.add(getBtnUndo(), gridBagConstraints);
			this.panelHistoryButtons.add(getBtnRedo(), gridBagConstraints1);
		}
		return this.panelHistoryButtons;
	}

	/**
	 * This method initializes btnUndo
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnUndo() {
		if (this.btnUndo == null) {
			this.btnUndo = new JButton();
			this.btnUndo.setText("< Undo");
			this.btnUndo.setPreferredSize(new Dimension(80, 18));
		}
		return this.btnUndo;
	}

	/**
	 * This method initializes btnRedo
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBtnRedo() {
		if (this.btnRedo == null) {
			this.btnRedo = new JButton();
			this.btnRedo.setText("Redo >");
			this.btnRedo.setActionCommand("Redo >");
			this.btnRedo.setPreferredSize(new Dimension(80, 18));
		}
		return this.btnRedo;
	}

}
