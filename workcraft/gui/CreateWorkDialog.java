package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.workcraft.framework.Framework;
import org.workcraft.framework.plugins.PluginInfo;

public class CreateWorkDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel optionsPane;
	private JPanel buttonsPane;

	private JList modelList;

	private JButton okButton;
	private JButton cancelButton;
	private JScrollPane modelScroll ;
	private JCheckBox chkVisual;
	private JCheckBox chkOpen;
	private JTextField txtTitle;

	private int modalResult = 0;
	private Framework framework;

	/**
	 * @param owner
	 */
	public CreateWorkDialog(MainWindow owner) {
		super(owner);

		this.framework = owner.framework;

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setModal(true);
		setTitle("New work");

		Dimension parentSize = owner.getSize();
		this.setSize(parentSize.width / 2, parentSize.height / 2);
		Dimension mySize = getSize();
		owner.getLocationOnScreen();

		this.setLocation (((parentSize.width - mySize.width)/2) + 0, ((parentSize.height - mySize.height)/2) + 0);

		initComponents();
	}

	private void initComponents() {
		this.contentPane = new JPanel(new BorderLayout());
		setContentPane(this.contentPane);

		this.modelScroll = new JScrollPane();
		DefaultListModel listModel = new DefaultListModel();


		this.modelList = new JList(listModel);
		this.modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.modelList.setLayoutOrientation(JList.VERTICAL_WRAP);

		this.modelList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				if (CreateWorkDialog.this.modelList.getSelectedIndex() == -1)
					CreateWorkDialog.this.okButton.setEnabled(false);
				else
					CreateWorkDialog.this.okButton.setEnabled(true);
			}
		}
		);

		this.modelList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2)
					if (CreateWorkDialog.this.modelList.getSelectedIndex() != -1)
						ok();
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseReleased(MouseEvent e) {
			}
		});

		PluginInfo[] modelsInfo = this.framework.getPluginManager().getModels();
		Arrays.sort(modelsInfo);
		for (PluginInfo info : modelsInfo)
			listModel.addElement(info);

		this.modelScroll.setViewportView(this.modelList);
		this.modelScroll.setBorder(BorderFactory.createTitledBorder("Type"));

		this.optionsPane = new JPanel();
		this.optionsPane.setBorder(BorderFactory.createTitledBorder("Creation options"));
		this.optionsPane.setLayout(new BoxLayout(this.optionsPane, BoxLayout.Y_AXIS));

		this.chkVisual = new JCheckBox("create visual model");

		this.chkVisual.setSelected(true);

		this.chkOpen = new JCheckBox("open in editor");
		this.chkOpen.setSelected(true);

		this.optionsPane.add(this.chkVisual);
		this.optionsPane.add(this.chkOpen);
		this.optionsPane.add(new JLabel("Title: "));
		this.txtTitle = new JTextField();
		//txtTitle.setMaximumSize(new Dimension(1000,20));
		this.optionsPane.add(this.txtTitle);

		JPanel dummy = new JPanel();
		dummy.setPreferredSize(new Dimension(200,1000));
		dummy.setMaximumSize(new Dimension(200,1000));

		this.optionsPane.add(dummy);


		this.buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));


		this.okButton = new JButton();
		this.okButton.setPreferredSize(new Dimension(100, 20));
		this.okButton.setEnabled(false);
		this.okButton.setText("OK");
		this.okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				ok();
			}
		});

		this.cancelButton = new JButton();
		this.cancelButton.setPreferredSize(new Dimension(100, 20));
		this.cancelButton.setText("Cancel");
		this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cancel();
			}
		});

		this.buttonsPane.add(this.okButton);
		this.buttonsPane.add(this.cancelButton);


		this.contentPane.add(this.modelScroll, BorderLayout.CENTER);
		this.contentPane.add(this.optionsPane, BorderLayout.WEST);
		this.contentPane.add(this.buttonsPane, BorderLayout.SOUTH);


		this.txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER)
					ok();
			}
		});

	}

	private void cancel() {
		this.modalResult = 0;
		setVisible(false);
	}

	private void ok() {
		this.modalResult = 1;
		setVisible(false);
	}

	public PluginInfo getSelectedModel() {
		return (PluginInfo)this.modelList.getSelectedValue();
	}

	public int getModalResult() {
		return this.modalResult;
	}

	public boolean createVisualSelected(){
		return this.chkVisual.isSelected();
	}

	public boolean openInEditorSelected() {
		return this.chkOpen.isSelected();
	}

	public String getModelTitle() {
		return this.txtTitle.getText();
	}
}