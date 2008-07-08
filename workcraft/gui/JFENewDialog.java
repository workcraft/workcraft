package org.workcraft.gui;

import javax.swing.JPanel;

import java.awt.Container;
import java.awt.Frame;
import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.WindowConstants;

import java.awt.Point;

public class JFENewDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JList jList = null;  //  @jve:decl-index=0:visual-constraint="388,286"
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JScrollPane jScrollPane = null;
	private JList jList1 = null;

	/**
	 * @param owner
	 */
	public JFENewDialog(Frame owner) {
		super(owner);
		initialize();
		int x;
		int y;


		Container myParent = getParent();
		java.awt.Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();

		Dimension mySize = getSize();

		if (parentSize.width > mySize.width)
			x = ((parentSize.width - mySize.width)/2) + topLeft.x;
		else
			x = topLeft.x;

		if (parentSize.height > mySize.height)
			y = ((parentSize.height - mySize.height)/2) + topLeft.y;
		else
			y = topLeft.y;

		setLocation (x, y);
	}

	public int modalResult = 0;
	//public ModelWrapper choice = null;

	/**
	 * This method initializes this
	 *
	 * @return void
	 */

	private void doOK() {
		if (jButton.isEnabled())
		{
			modalResult = 1;
		//	choice = (ModelWrapper)jList1.getSelectedValue();
			setVisible(false);
		}
	}

	private void initialize() {
		this.setSize(325, 296);
		this.setLocation(new Point(200, 200));
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setModal(true);
		this.setTitle("New model");

		this.setContentPane(getJContentPane());
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER)
					doOK();
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 1;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridx = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.CENTER;
			gridBagConstraints5.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints5.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.gridy = 1;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getJPanel(), gridBagConstraints5);
			jContentPane.add(getJScrollPane(), gridBagConstraints7);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jList
	 *
	 * @return javax.swing.JList
	 */
	public JList getModelList() {
		return getJList1();
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.weightx = 0.0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTH;
			gridBagConstraints3.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.anchor = GridBagConstraints.NORTH;
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.gridx = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJButton(), gridBagConstraints2);
			jPanel.add(getJButton1(), gridBagConstraints3);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setPreferredSize(new Dimension(100, 20));
			jButton.setEnabled(false);
			jButton.setText("OK");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doOK();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setPreferredSize(new Dimension(100, 20));
			jButton1.setText("Cancel");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					modalResult = 0;
					setVisible(false);
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Model Type", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51));
			titledBorder.setBorder(null);
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJList1());
			jScrollPane.setBorder(titledBorder);
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jList1
	 *
	 * @return javax.swing.JList
	 */
	private JList getJList1() {
		if (jList1 == null) {
			jList1 = new JList(new DefaultListModel());
			jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					if (jList1.getSelectedIndex() == -1) {
					jButton.setEnabled(false);
				} else
					jButton.setEnabled(true);
					}
			}
				);

		}
		return jList1;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
