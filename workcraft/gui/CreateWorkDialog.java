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

import org.workcraft.dom.math.MathModel;
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

		framework = owner.getFramework();

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
		contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		modelScroll = new JScrollPane();
		DefaultListModel listModel = new DefaultListModel();


		modelList = new JList(listModel);
		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelList.setLayoutOrientation(JList.VERTICAL_WRAP);

		modelList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				if (modelList.getSelectedIndex() == -1)
					okButton.setEnabled(false);
				else
					okButton.setEnabled(true);
			}
		}
		);

		modelList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2)
					if (modelList.getSelectedIndex() != -1)
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

		PluginInfo[] modelsInfo = framework.getPluginManager().getPluginsImplementing(MathModel.class.getName());
		Arrays.sort(modelsInfo);
		for (PluginInfo info : modelsInfo)
			listModel.addElement(info);

		modelScroll.setViewportView(modelList);
		modelScroll.setBorder(BorderFactory.createTitledBorder("Type"));

		optionsPane = new JPanel();
		optionsPane.setBorder(BorderFactory.createTitledBorder("Creation options"));
		optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));

		chkVisual = new JCheckBox("create visual model");

		chkVisual.setSelected(true);

		chkOpen = new JCheckBox("open in editor");
		chkOpen.setSelected(true);

		optionsPane.add(chkVisual);
		optionsPane.add(chkOpen);
		optionsPane.add(new JLabel("Title: "));
		txtTitle = new JTextField();
		//txtTitle.setMaximumSize(new Dimension(1000,20));
		optionsPane.add(txtTitle);

		JPanel dummy = new JPanel();
		dummy.setPreferredSize(new Dimension(200,1000));
		dummy.setMaximumSize(new Dimension(200,1000));

		optionsPane.add(dummy);


		buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));


		okButton = new JButton();
		okButton.setPreferredSize(new Dimension(100, 20));
		okButton.setEnabled(false);
		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (okButton.isEnabled())
					ok();
			}
		});

		cancelButton = new JButton();
		cancelButton.setPreferredSize(new Dimension(100, 20));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cancel();
			}
		});

		buttonsPane.add(okButton);
		buttonsPane.add(cancelButton);


		contentPane.add(modelScroll, BorderLayout.CENTER);
		contentPane.add(optionsPane, BorderLayout.WEST);
		contentPane.add(buttonsPane, BorderLayout.SOUTH);


		txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER)
					if (okButton.isEnabled())
						ok();
			}
		});

	}

	private void cancel() {
		modalResult = 0;
		setVisible(false);
	}

	private void ok() {
		modalResult = 1;
		setVisible(false);
	}

	public PluginInfo getSelectedModel() {
		return (PluginInfo)modelList.getSelectedValue();
	}

	public int getModalResult() {
		return modalResult;
	}

	public boolean createVisualSelected(){
		return chkVisual.isSelected();
	}

	public boolean openInEditorSelected() {
		return chkOpen.isSelected();
	}

	public String getModelTitle() {
		return txtTitle.getText();
	}
}