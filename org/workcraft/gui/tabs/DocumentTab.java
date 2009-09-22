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

package org.workcraft.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class DocumentTab extends JPanel {
	private static final long serialVersionUID = 1L;

	protected Object documentFrame = null;
	protected int tabIndex = -1;
	protected LinkedList<TabClosingListener> closingListeners = new LinkedList<TabClosingListener>();

	public DocumentTab(String title, int tabIndex) {
		super();
		setOpaque(false);
		setLayout(new BorderLayout());

		String trimmedTitle;

		if (title.length() > 32)
			trimmedTitle = title.substring(0, 15) + "..." + title.substring(title.length()-16, title.length()-1);
		else
			trimmedTitle = title;

		this.tabIndex = tabIndex;


		JLabel label = new JLabel(trimmedTitle);
		TabCloseButton close = new TabCloseButton();

		label.setOpaque(false);

		Dimension x = label.getPreferredSize();
		Dimension y = close.getPreferredSize();

		close.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1)
					for (TabClosingListener l : closingListeners)
						l.tabClosing(DocumentTab.this);
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

		this.add(label, BorderLayout.WEST);
		this.add(close, BorderLayout.EAST);



		setPreferredSize(new Dimension(x.width + y.width + 30, x.height));
	}

	public void setDocumentFrame(Object documentFrame) {
		this.documentFrame = documentFrame;
	}


	public Object getDocumentFrame() {
		return documentFrame;
	}

	public void addTabClosingListener(TabClosingListener listener) {
		closingListeners.add(listener);
	}

	public void removeTabClosingListener(TabClosingListener listener) {
		closingListeners.remove(listener);
	}

	public void clearTabClosingListeners() {
		closingListeners.clear();
	}

	public int getTabIndex() {
		return tabIndex;
	}
}