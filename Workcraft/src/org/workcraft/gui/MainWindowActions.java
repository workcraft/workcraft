package org.workcraft.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

	public static final Action EDIT_SELECT_ALL_ACTION = new Action() {
		@Override
		public String getText() {
			return "Select all";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().selectAll();
		}
	};

	public static final Action EDIT_SELECT_INVERSE_ACTION = new Action() {
		@Override
		public String getText() {
			return "Inverse selection";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().selectInverse();
		}
	};

	public static final Action EDIT_SELECT_NONE_ACTION = new Action() {
		@Override
		public String getText() {
			return "Deselect";
		}
		@Override
		public void run(Framework f) {
			f.getMainWindow().selectNone();
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

	public static final Action HELP_ACTION = new Action() {
		@Override
		public void run(Framework f) {
			final String help = "file://" + System.getProperty("user.dir") + "/help/index.html";
			try {
				Desktop.getDesktop().browse(new URI(help));
			} catch(IOException e1) {
				System.out.println(e1);
			} catch (URISyntaxException e2) {
				System.out.println(e2);
			}
		}
		public String getText() {
			return "Help";
		};
	};
}