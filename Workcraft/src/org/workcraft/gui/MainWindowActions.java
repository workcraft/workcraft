package org.workcraft.gui;

import javax.swing.JOptionPane;

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
	public static final Action MERGE_WORK_ACTION = new Action() {
		@Override public void run(Framework f) {
			try { f.getMainWindow().mergeWork(); } catch (OperationCancelledException e) { }
		}
		@Override public String getText() {
			return "Merge work...";
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
		@Override
		public String getText() {
			return "Reconfigure plugins";
		}
		@Override
		public void run(Framework f) {
			try {
				f.getPluginManager().reconfigure();
			} catch (PluginInstantiationException e) {
				e.printStackTrace();
			}
		};
	};

	public static final Action IMPORT_ACTION = new Action() {
		@Override
		public String getText() {
			return "Import...";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().importFrom();
		}
	};

	public static final Action EDIT_UNDO_ACTION = new Action() {
		@Override
		public String getText() {
			return "Undo";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().undo();
		}
	};

	public static final Action EDIT_REDO_ACTION = new Action() {
		@Override
		public String getText() {
			return "Redo";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().redo();
		}
	};

	public static final Action EDIT_CUT_ACTION = new Action() {
		@Override
		public String getText() {
			return "Cut";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().cut();
		}
	};

	public static final Action EDIT_COPY_ACTION = new Action() {
		@Override
		public String getText() {
			return "Copy";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().copy();
		}
	};

	public static final Action EDIT_PASTE_ACTION = new Action() {
		@Override
		public String getText() {
			return "Paste";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().paste();
		}
	};

	public static final Action EDIT_DELETE_ACTION = new Action() {
		@Override
		public String getText() {
			return "Delete";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().delete();
		}
	};

	public static final Action EDIT_SETTINGS_ACTION = new Action() {
		@Override
		public String getText() {
			return "Preferences...";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().editSettings();
		}
	};

	public static final Action RESET_GUI_ACTION = new Action() {
		@Override
		public String getText() {
			return "Reset UI layout";
		}

		@Override
		public void run(Framework f) {
			f.getMainWindow().resetLayout();
		}

	};

	public static final Action HINTS_ACTION = new Action() {
		@Override
		public void run(Framework f) {
			String text =
				"<html><ul>" +
				"<li><b>Selection</b></li><ul>" +
					"<li>Hold <i>Shift</i> to include objects into a selection and <i>Ctrl</i> to exclude objects from a selection.</li>" +
					"<li>Outline a selection area <i>from-right-to-left</i> for adding fully covered objects, and <i>from-left-to-right</i> for adding any touched objects.</li>" +
					"<li>Use <i>left mouse button</i> or <i>arrow keys</i> to move selected components.</li>" +
					"<li>Selected components can be removed by pressing <i>Delete</i> key.</li>" +
				"</ul><li><b>Clipboard and History</b></li><ul>" +
					"<li>Clipboard operations are allowed between the models of the same type: <i>Ctrl+C</i> to copy, <i>Ctrl+X</i> to cut and <i>Ctrl+V</i> to insert.</li>" +
					"<li>History of modifications can be browsed: <i>Ctrl+Z</i> to undo and <i>Ctrl+Shift+Z</i> to redo.</li>" +
				"</ul><li><b>Navigation and Grouping</b></li><ul>" +
					"<li><i>Ctrl+G</i> combines selected objects into a group and <i>Ctrl+Shift+U</i> ungroups them.</li>" +
					"<li><i>PageDown</i> enters a group and <i>PageUp</i> leaves it.</li>" +
					"<li>Scroll mouse wheel <i>forward</i> to zooms in and <i>backward</i> to zoom out.</li>" +
					"<li>Alternatively press <i>'+'</i> to zoom in and <i>'-'</i> to zoom out.</li>" +
					"<li>Use <i>middle mouse button</i> or <i>Ctrl+arrow keys</i> to pan the view.</li>" +
				"</ul><li><b>Simulation</b></li><ul>" +
					"<li>Use <i>[</i> and <i>]</i> keys to navigate through the simulation trace.</li>" +
					"<li>In Signal-State table the values of excited signals are depicted in bold font.</li>" +
				"</ul><li><b>Settings</b></li><ul>" +
					"<li>Use punf with <i>-r</i> option to replicate Petri net places sensed by read-arcs and speed up the unfolding.</li>" +
					"<li>Use petrify with <i>-nosi</i> option to allow synthesis of non-speed-independent specifications.</li>" +
					"<li>Add <i>-lib tools/petrify.lib</i> to the petrify options to enable technology mapping into this library.</li>" +
				"</ul></ul></html>";
			JOptionPane.showMessageDialog(f.getMainWindow(), text);
		}
		public String getText() {
			return "Hints";
		};
	};
}