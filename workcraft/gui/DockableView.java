package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DockableView extends JPanel {
	String title;
	JComponent content;
	JPanel contentPane;
	boolean standalone = true;

	public DockableView (String title, JComponent content) {
		super();
		this.title = title;
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0,0));
		this.setLayout(new BorderLayout(0, 0));
		add(contentPane, BorderLayout.CENTER);
		contentPane.add(content,BorderLayout.CENTER);

		contentPane.setBorder(BorderFactory.createTitledBorder(title));
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;

		if (standalone)
			contentPane.setBorder(BorderFactory.createTitledBorder(title));
		else
			contentPane.setBorder(null);
	}

	public String getTitle() {
		return title;
	}

	public JComponent getContent() {
		return content;
	}
/*
	@Override
	public boolean dock(Dockable dockable) {
		return DockingManager.dock(dockable, this);
	}

	@Override
	public boolean dock(Dockable dockable, String relativeRegion) {
		return DockingManager.dock(dockable, this, relativeRegion);
	}

	@Override
	public boolean dock(Dockable dockable, String relativeRegion, float ratio) {
		return DockingManager.dock(dockable, this, relativeRegion, ratio);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public DockingPort getDockingPort() {
		return DockingManager.getDockingPort((Dockable)this);
	}

	@Override
	public DockablePropertySet getDockingProperties() {
		return PropertyManager.getDockablePropertySet(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List getDragSources() {
		LinkedList q = new LinkedList();
		q.add(contentPane);
		return q;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set getFrameDragSources() {
		return null;
	}

	@Override
	public String getPersistentId() {
		return title;
	}

	@Override
	public void dockingCanceled(DockingEvent evt) {
	}

	@Override
	public void dockingComplete(DockingEvent evt) {
	}

	@Override
	public void dragStarted(DockingEvent evt) {
	}

	@Override
	public void dropStarted(DockingEvent evt) {
	}

	@Override
	public void undockingComplete(DockingEvent evt) {
	}

	@Override
	public void undockingStarted(DockingEvent evt) {
	}

	@Override
	public void addDockingListener(DockingListener listener) {
		DockingEventHandler.addDockingListener(this, listener);
	}

	@Override
	public DockingListener[] getDockingListeners() {
		return DockingEventHandler.getDockingListeners(this);
	}

	@Override
	public void removeDockingListener(DockingListener listener) {
		DockingEventHandler.removeDockingListener(this, listener);
	}
*/
}
