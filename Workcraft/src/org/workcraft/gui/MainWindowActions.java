package org.workcraft.gui;

import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.gui.actions.Action;

public class MainWindowActions {
	public static final Action CREATE_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().createWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Create work...";
		};
	};
	public static final Action OPEN_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().openWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Open work...";
		};
	};
	public static final Action SAVE_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().saveWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Save work";
		};
	};
	public static final Action SAVE_WORK_AS_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().saveWorkAs(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Save work as...";
		};
	};
	public static final Action CLOSE_ACTIVE_EDITOR_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().closeActiveEditor(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Close active work";
		};
	};

	public static final Action CLOSE_ALL_EDITORS_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().closeEditorWindows(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Close all works";
		};
	};
	public static final Action EXIT_ACTION = new Action() {
		@Override public void run(Framework f) {
			f.shutdown();
		}
		public String getText() {
			return "Exit";
		};
	};
	public static final Action SHUTDOWN_GUI_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.shutdownGUI(); } catch (OperationCancelledException e) { }
		}
		public String getText() {
			return "Switch to console mode";
		};
	};

	public static final Action RECONFIGURE_PLUGINS_ACTION = new Action() {
		public String getText() {
			return "Reconfigure plugins";
		}
		@Override
		public void run(Framework framework) {
			try {
				framework.getPluginManager().reconfigure();
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}
		};
	};

	public static final Action IMPORT_ACTION = new Action() {
		public String getText() {
			return "Import...";
		}
		@Override
		public void run(Framework framework) {
			framework.getMainWindow().importFrom();
		}
	};

	public static final Action EDIT_SETTINGS_ACTION = new Action() {
		public String getText() {
			return "Preferences...";
		}
		@Override
		public void run(Framework framework) {
			framework.getMainWindow().editSettings();
		}
	};

	public static final Action RESET_GUI_ACTION = new Action() {
		@Override
		public String getText() {
			return "Reset UI layout";
		}

		@Override
		public void run(Framework framework) {
			framework.getMainWindow().resetLayout();
		}

	};

	public static final Action HINTS_ACTION = new Action() {
		@Override public void run(Framework f) {
			String text =
				"<html><il>" +
				"<li>Hold <i>Shift</i> to include objects into a selection and <i>Ctrl</i> to exclude objects from a selection.</li>" +
				"<li>Outline a selection area from-right-to-left for adding fully covered objects, and from-left-to-right for adding any touched objects.</li>" +
				"<li><i>Ctrl+G</i> groups objects into a cluster and <i>Ctrl+U</i> ungroups it.</li>" +
				"<li><i>PageDown</i> enters a group anf <i>PageUp</i> leaves it.</li>" +
				"<li>Scroll mouse wheel <i>forward</i> to zooms in and <i>backward</i> to zoom out. Alternatively press <i>'+'</i> or <i>'='</i> to zoom in and <i>'-'</i> or <i>'_'</i> to zoom out.</li>" +
				"<li>Use <i>left mouse button</i> or <i>arrow keys</i> to move components.</li>" +
				"<li>Use <i>middle mouse button</i> or <i>Ctrl+arrow keys</i> to pan the view.</li>" +
				"</il></html>";
			JOptionPane.showMessageDialog(f.getMainWindow(), text);
		}
		public String getText() {
			return "Hints";
		};
	};
}