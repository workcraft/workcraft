package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.LinkedList;

import javax.swing.JDesktopPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

		this.desktop = new JDesktopPane();
		this.desktop.setLayout(null);

		setLayout(new BorderLayout());
		this.add(this.desktop, BorderLayout.CENTER);

		this.tabs = new JTabbedPane();
		this.tabs.addChangeListener(this);

		this.tabPopupMenu = new JPopupMenu();
		this.menuRestore = new JMenuItem ("Restore");
		this.menuRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMin (MDIPane.this.popupOriginIndex);
			}
		});
		this.tabPopupMenu.add(this.menuRestore);
		this.tabPopupMenu.addSeparator();

		this.menuClose = new JMenuItem("Close");

		this.menuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClosing(((InternalWindow) ((DocumentTab)MDIPane.this.tabs.getTabComponentAt(MDIPane.this.popupOriginIndex)).getDocumentFrame()), MDIPane.this.popupOriginIndex);
			}
		});


		this.tabPopupMenu.add(this.menuClose);

		this.tabs.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				int tabIndex = MDIPane.this.tabs.indexAtLocation(e.getX(), e.getY());
				if (tabIndex == -1)
					return;

				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
					handleMin(tabIndex);

			}

			public void checkPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int tabIndex = MDIPane.this.tabs.indexAtLocation(e.getX(), e.getY());
					if (tabIndex == -1)
						return;
					MDIPane.this.popupOriginIndex = tabIndex;
					MDIPane.this.tabPopupMenu.show(MDIPane.this, e.getX(), e.getY());
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
		this.documentFrames.add(f);
		f.setLayer(2);
		this.desktop.add(f);
		f.addPropertyChangeListener("maximum", this);
		//f.ownerMDIPane = this;

	}

	public void addFrame(InternalFloaterWindow f) {
		this.floaterFrames.add(f);
		f.setLayer(3);
		this.desktop.add(f);
	}

	protected void handleMax(InternalWindow frame) {
		if (this.tabs.getTabCount() == 0) {
			this.remove(this.desktop);
			this.add(this.tabs, BorderLayout.CENTER);
		}

		JPanel p = new JPanel(new BorderLayout());
		p.add(this.desktop, BorderLayout.CENTER);
		p.setBorder(null);

		this.tabs.addTab("?", p);
		int tabIndex = this.tabs.getTabCount()-1;

		DocumentTab docTab = new DocumentTab(frame.getTitle(), tabIndex);
		docTab.setDocumentFrame(frame);

		docTab.addTabClosingListener(new TabClosingListener() {
			public void tabClosing(DocumentTab tab) {
				handleClosing ((InternalWindow)tab.getDocumentFrame(), -1);
			}
		});

		this.tabs.setTabComponentAt(tabIndex, docTab);

		frame.setLayer(1);
		frame.hideTitle();
		frame.hideBorder();

		this.tabs.setSelectedIndex(this.tabs.getTabCount()-1);

		frame.setVisible(true);
	}

	protected void handleClosing(InternalWindow frame, int tabIndex) {
		JOptionPane.showMessageDialog(MDIPane.this, "No. \"" + frame.getTitle() + "\" will stay open.");
	}

	protected void handleMin(int tabIndex) {
		InternalWindow frame = (InternalWindow)((DocumentTab)this.tabs.getTabComponentAt(tabIndex)).getDocumentFrame();

		frame.showBorder();
		frame.showTitle();

		frame.setLayer(2);
		try {
			frame.setMaximum(false);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		this.tabs.removeTabAt(tabIndex);


		if (this.tabs.getTabCount() == 0) {
			this.remove(this.tabs);
			this.add(this.desktop, BorderLayout.CENTER);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() != null && evt.getNewValue().equals(Boolean.TRUE))
			handleMax((InternalWindow)evt.getSource());
	}

	public void stateChanged(ChangeEvent e) {
		if (this.tabs.getSelectedIndex() == -1)
			return;

		DocumentTab tab = (DocumentTab)(this.tabs.getTabComponentAt(this.tabs.getSelectedIndex()));
		if (tab == null)
			return;

		((JPanel)this.tabs.getSelectedComponent()).add(this.desktop);

		for (InternalWindow f : this.documentFrames)
			if (f.isMaximum())
				if (f == tab.getDocumentFrame())
					f.setVisible(true);
				else
					f.setVisible(false);
	}
}