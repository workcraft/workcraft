package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class DockableView extends JPanel {
	class DockableViewHeader extends JPanel {
		private JButton btnMin, btnMax, btnClose;
		private JPanel buttonPanel;
		private static final int BUTTON_SIZE = 20;

		private JButton createHeaderButton(Icon icon) {
			JButton button = new JButton();
			button.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
			button.setFocusable(false);
			button.setBorder(null);
			button.setIcon(icon);
			return button;
		}

		public DockableViewHeader(String title, int options) {
			super();
			setLayout(new BorderLayout());

			Color c = getBackground();
			Color darker = new Color( (int)(c.getRed() * 0.9), (int)(c.getGreen() * 0.9), (int)(c.getBlue() * 0.9) );
			setBackground(darker);

			if ((options & HEADER_BUTTONS) != 0) {
				buttonPanel = new JPanel();
				buttonPanel.setBackground(darker);
				buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

				btnMin = createHeaderButton(UIManager.getIcon("InternalFrame.minimizeIcon"));
				btnMax = createHeaderButton(UIManager.getIcon("InternalFrame.maximizeIcon"));
				btnClose = createHeaderButton(UIManager.getIcon("InternalFrame.closeIcon"));

				buttonPanel.add(btnMin, BorderLayout.EAST);
				buttonPanel.add(btnMax, BorderLayout.EAST);
				buttonPanel.add(btnClose, BorderLayout.EAST);
				buttonPanel.setFocusable(false);

				add(buttonPanel, BorderLayout.EAST);
			}

			JLabel label = new JLabel( " "+ title);
			label.setFont(label.getFont().deriveFont(Font.ITALIC | Font.BOLD));

			this.add(label, BorderLayout.WEST);
		}
	}

	public static final int HEADER_BUTTONS = 1;

	private String title;
	private JComponent content;
	private JPanel contentPane;
	private DockableViewHeader header;

	public DockableView (String title, JComponent content, int options) {
		super();
		setLayout(new BorderLayout(0, 0));

		this.title = title;

		header = new DockableViewHeader(title, options);

		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0,0));
		contentPane.add(content,BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createLineBorder(contentPane.getBackground(), 2));



		contentPane.add(header, BorderLayout.NORTH);

		add(contentPane, BorderLayout.CENTER);
	}

	public void setStandalone(boolean standalone) {

		if (standalone) {
			if (header.getParent() != contentPane)
				contentPane.add(header, BorderLayout.NORTH);
			contentPane.doLayout();
		}
		else
			contentPane.remove(header);

	}

	public String getTitle() {
		return title;
	}

	public JComponent getContent() {
		return content;
	}
	/*

	public boolean dock(Dockable dockable) {
		return DockingManager.dock(dockable, this);
	}


	public boolean dock(Dockable dockable, String relativeRegion) {
		return DockingManager.dock(dockable, this, relativeRegion);
	}


	public boolean dock(Dockable dockable, String relativeRegion, float ratio) {
		return DockingManager.dock(dockable, this, relativeRegion, ratio);
	}


	public Component getComponent() {
		return this;
	}


	public DockingPort getDockingPort() {
		return DockingManager.getDockingPort((Dockable)this);
	}


	public DockablePropertySet getDockingProperties() {
		return PropertyManager.getDockablePropertySet(this);
	}

	@SuppressWarnings("unchecked")

	public List getDragSources() {
		LinkedList q = new LinkedList();
		q.add(contentPane);
		return q;
	}

	@SuppressWarnings("unchecked")

	public Set getFrameDragSources() {
		return null;
	}


	public String getPersistentId() {
		return title;
	}


	public void dockingCanceled(DockingEvent evt) {
	}


	public void dockingComplete(DockingEvent evt) {
	}


	public void dragStarted(DockingEvent evt) {
	}


	public void dropStarted(DockingEvent evt) {
	}


	public void undockingComplete(DockingEvent evt) {
	}


	public void undockingStarted(DockingEvent evt) {
	}


	public void addDockingListener(DockingListener listener) {
		DockingEventHandler.addDockingListener(this, listener);
	}


	public DockingListener[] getDockingListeners() {
		return DockingEventHandler.getDockingListeners(this);
	}


	public void removeDockingListener(DockingListener listener) {
		DockingEventHandler.removeDockingListener(this, listener);
	}
	 */
}
