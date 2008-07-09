package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.workcraft.gui.tabs.DocumentTab;
import org.workcraft.gui.tabs.TabClosingListener;



public class MDIPane extends JPanel implements  PropertyChangeListener, ChangeListener {
	private static final long serialVersionUID = 1L;

	protected LinkedList<InternalWindow> documentFrames = new LinkedList<InternalWindow>();
	protected LinkedList<InternalFloaterWindow> floaterFrames = new LinkedList<InternalFloaterWindow>();

	JDesktopPane desktop;
	JTabbedPane tabs;
	JPopupMenu tabPopupMenu;
	JMenuItem menuRestore;
	JMenuItem menuClose;

	int popupOriginIndex = -1;

	public MDIPane() {
		super();

		desktop = new JDesktopPane();
		desktop.setLayout(null);

		this.setLayout(new BorderLayout());
		this.add(desktop, BorderLayout.CENTER);

		tabs = new JTabbedPane();
		tabs.addChangeListener(this);

		tabPopupMenu = new JPopupMenu();
		menuRestore = new JMenuItem ("Restore");
		menuRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMin (popupOriginIndex);
			}
		});
		tabPopupMenu.add(menuRestore);
		tabPopupMenu.addSeparator();

		menuClose = new JMenuItem("Close");

		menuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClosing(((InternalWindow) ((DocumentTab)tabs.getTabComponentAt(popupOriginIndex)).getDocumentFrame()), popupOriginIndex);
			}
		});


		tabPopupMenu.add(menuClose);

		tabs.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				int tabIndex = tabs.indexAtLocation(e.getX(), e.getY());
				if (tabIndex == -1)
					return;

				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
							handleMin(tabIndex);
				}

			}

			public void checkPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int tabIndex = tabs.indexAtLocation(e.getX(), e.getY());
					if (tabIndex == -1)
						return;
					popupOriginIndex = tabIndex;
					tabPopupMenu.show(MDIPane.this, e.getX(), e.getY());
				}
			}


			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mousePressed(MouseEvent e) {
				checkPopup(e);

			}

			public void mouseReleased(MouseEvent e) {
				checkPopup(e);
			}

		});
	}

	public void addFrame(InternalWindow f) {
		documentFrames.add(f);
		f.setLayer(2);
		desktop.add(f);
		f.addPropertyChangeListener("maximum", this);
		//f.ownerMDIPane = this;

	}

	public void addFrame(InternalFloaterWindow f) {
		floaterFrames.add(f);
		f.setLayer(3);
		desktop.add(f);
	}

	protected void handleMax(InternalWindow frame) {
		if (tabs.getTabCount() == 0) {
			this.remove(desktop);
			this.add(tabs, BorderLayout.CENTER);
		}

		JPanel p = new JPanel(new BorderLayout());
		p.add(desktop, BorderLayout.CENTER);
		p.setBorder(null);

		tabs.addTab("?", p);
		int tabIndex = tabs.getTabCount()-1;

		DocumentTab docTab = new DocumentTab(frame.getTitle(), tabIndex);
		docTab.setDocumentFrame(frame);

		docTab.addTabClosingListener(new TabClosingListener() {
			public void tabClosing(DocumentTab tab) {
				handleClosing ((InternalWindow)tab.getDocumentFrame(), -1);
			}
		});

		tabs.setTabComponentAt(tabIndex, docTab);

		frame.setLayer(1);
		frame.hideTitle();
		frame.hideBorder();

		tabs.setSelectedIndex(tabs.getTabCount()-1);

		frame.setVisible(true);
	}

	protected void handleClosing(InternalWindow frame, int tabIndex) {
		JOptionPane.showMessageDialog(MDIPane.this, "No. \"" + frame.getTitle() + "\" will stay open.");
	}

	protected void handleMin(int tabIndex) {
		InternalWindow frame = (InternalWindow)((DocumentTab)tabs.getTabComponentAt(tabIndex)).getDocumentFrame();

		frame.showBorder();
		frame.showTitle();

		frame.setLayer(2);
		try {
			frame.setMaximum(false);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		tabs.removeTabAt(tabIndex);


		if (tabs.getTabCount() == 0) {
			this.remove(tabs);
			this.add(desktop, BorderLayout.CENTER);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() != null && evt.getNewValue().equals(Boolean.TRUE)) {
			handleMax((InternalWindow)evt.getSource());
		}
	}

	public void stateChanged(ChangeEvent e) {
		if (tabs.getSelectedIndex() == -1)
			return;

		DocumentTab tab = (DocumentTab)(tabs.getTabComponentAt(tabs.getSelectedIndex()));
		if (tab == null)
			return;

		((JPanel)tabs.getSelectedComponent()).add(desktop);

		for (InternalWindow f : documentFrames) {
			if (f.isMaximum()) {

				if (f == tab.getDocumentFrame())
					f.setVisible(true);
				else
					f.setVisible(false);
			}
		}
	}
}