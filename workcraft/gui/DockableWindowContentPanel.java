package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionButton;
import org.workcraft.gui.actions.ScriptedActionListener;

@SuppressWarnings("serial")
public class DockableWindowContentPanel extends JPanel {
	class ViewAction extends ScriptedAction {
		public static final int CLOSE_ACTION = 1;
		public static final int MINIMIZE_ACTION = 2;
		public static final int MAXIMIZE_ACTION = 3;

		private int windowID;
		private int actionType;

		public ViewAction (int windowID, int actionType) {
			this.actionType = actionType;
			this.windowID = windowID;
		}

		public String getScript() {
			switch (actionType) {
			case CLOSE_ACTION:
				return "mainWindow.closeDockableWindow("+windowID+");";
			case MINIMIZE_ACTION:
				return "mainWindow.minimizeDockableWindow("+windowID+");";
			case MAXIMIZE_ACTION:
				return "mainWindow.maximizeDockableWindow("+windowID+");";
			}
			return null;
		}

		public String getText() {
			return null;
		}
	}

	class DockableViewHeader extends JPanel {
		private ScriptedActionButton btnMin, btnMax, btnClose;
		private JPanel buttonPanel;
		private static final int BUTTON_SIZE = 20;

		private ScriptedActionButton createHeaderButton(Icon icon, ScriptedAction action, ScriptedActionListener actionListener) {
			ScriptedActionButton button = new ScriptedActionButton(action);
			button.addScriptedActionListener(actionListener);
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

			if  (options != 0) {
				buttonPanel = new JPanel();
				buttonPanel.setBackground(darker);
				buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
				buttonPanel.setFocusable(false);
				add(buttonPanel, BorderLayout.EAST);
			}

			if ( (options & MINIMIZE_BUTTON) != 0) {
				btnMin = createHeaderButton(UIManager.getIcon("InternalFrame.minimizeIcon"),
						new ViewAction(ID, ViewAction.MINIMIZE_ACTION), mainWindow.getDefaultActionListener());
				btnMin.setToolTipText("Toggle minimized");
				buttonPanel.add(btnMin, BorderLayout.EAST);
			}

			if ( (options & MAXIMIZE_BUTTON) != 0) {
				btnMax = createHeaderButton(UIManager.getIcon("InternalFrame.maximizeIcon"),
						new ViewAction(ID, ViewAction.MAXIMIZE_ACTION), mainWindow.getDefaultActionListener());
				btnMax.setToolTipText("Toggle maximized");
				buttonPanel.add(btnMax, BorderLayout.EAST);
			}

			if ( (options & CLOSE_BUTTON) != 0) {
				btnClose = createHeaderButton(UIManager.getIcon("InternalFrame.closeIcon"),
						new ViewAction(ID, ViewAction.CLOSE_ACTION), mainWindow.getDefaultActionListener());
				btnClose.setToolTipText("Close");
				buttonPanel.add(btnClose, BorderLayout.EAST);
			}


			JLabel label = new JLabel(" "+ title);
			label.setFont(label.getFont().deriveFont(Font.BOLD));

			add(label, BorderLayout.WEST);
		}
	}

	public static final int CLOSE_BUTTON = 1;
	public static final int MINIMIZE_BUTTON = 2;
	public static final int MAXIMIZE_BUTTON = 4;

	private String title;
	private JComponent content;
	private JPanel contentPane;
	private DockableViewHeader header;
	private MainWindow mainWindow;
	private int ID;
	private Dockable dockable = null;

	public Dockable getDockable() {
		return dockable;
	}

	public void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	public DockableWindowContentPanel (MainWindow mainWindow, int ID, String title, JComponent content, int options) {
		super();
		setLayout(new BorderLayout(0, 0));

		this.title = title;
		this.mainWindow = mainWindow;
		this.ID = ID;

		header = new DockableViewHeader(title, options);

		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0,0));
		contentPane.add(content,BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createLineBorder(contentPane.getBackground(), 2));
		contentPane.add(header, BorderLayout.NORTH);

		add(contentPane, BorderLayout.CENTER);
		setFocusable(false);
	}

	public void setHeaderVisible(boolean headerVisible) {
		if (headerVisible) {
			if (header.getParent() != contentPane)
				contentPane.add(header, BorderLayout.NORTH);
		}
		else
			contentPane.remove(header);

		contentPane.doLayout();
	}

	public String getTitle() {
		return title;
	}

	public int getID() {
		return ID;
	}

	public JComponent getContent() {
		return content;
	}

}
