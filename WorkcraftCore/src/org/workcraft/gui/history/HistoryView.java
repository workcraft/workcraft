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

import org.workcraft.history.HistoryEvent;
import org.workcraft.history.HistoryListener;
import org.workcraft.history.HistoryProvider;

@SuppressWarnings("serial")
public class HistoryView extends JInternalFrame implements HistoryListener {
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
    public HistoryView() {
        super();
        initialize();
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
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getListHistory(), BorderLayout.CENTER);
            jContentPane.add(getPanelHistoryButtons(), BorderLayout.NORTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes listHistory
     *
     * @return javax.swing.JList
     */
    private JList getListHistory() {
        if (listHistory == null) {
            listHistory = new JList();
            listHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return listHistory;
    }

    public void setProvider(HistoryProvider provider) {
        // detach from previous provider
        if (this.provider != null)
            this.provider.removeHistoryListener(this);

        // populate list of existing events
        List <HistoryEvent> events = provider.getHistory();
        listModel = new HistoryListModel(events);
        getListHistory().setModel(listModel);

        // attach to new provider
        this.provider = provider;
        this.provider.addHistoryListener(this);
    }

    public HistoryProvider getProvider() {
        return provider;
    }

    public void eventAdded(HistoryEvent event) {
        listModel.events.add(event);
        getListHistory().setSelectedIndex(listModel.events.size()-1);
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
        if (panelHistoryButtons == null) {
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
            panelHistoryButtons = new JPanel();
            panelHistoryButtons.setLayout(new GridBagLayout());
            panelHistoryButtons.setPreferredSize(new Dimension(0, 25));
            panelHistoryButtons.add(getBtnUndo(), gridBagConstraints);
            panelHistoryButtons.add(getBtnRedo(), gridBagConstraints1);
        }
        return panelHistoryButtons;
    }

    /**
     * This method initializes btnUndo
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnUndo() {
        if (btnUndo == null) {
            btnUndo = new JButton();
            btnUndo.setText("< Undo");
            btnUndo.setPreferredSize(new Dimension(80, 18));
        }
        return btnUndo;
    }

    /**
     * This method initializes btnRedo
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnRedo() {
        if (btnRedo == null) {
            btnRedo = new JButton();
            btnRedo.setText("Redo >");
            btnRedo.setActionCommand("Redo >");
            btnRedo.setPreferredSize(new Dimension(80, 18));
        }
        return btnRedo;
    }

}
