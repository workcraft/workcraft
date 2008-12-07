package org.workcraft.gui.history;

import java.util.List;

import javax.swing.AbstractListModel;

import org.workcraft.framework.HistoryEvent;


@SuppressWarnings("serial")
public class HistoryListModel extends AbstractListModel {
	List <HistoryEvent> events;

	public HistoryListModel (List<HistoryEvent> events) {
		super();
	}

	public Object getElementAt(int index) {
		return this.events.get(index).getEventDescription();
	}

	public int getSize() {
		return this.events.size();
	}
}
