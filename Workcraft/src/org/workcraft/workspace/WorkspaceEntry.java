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

package org.workcraft.workspace;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.workspace.Path;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.observation.TransformChangedEvent;

public class WorkspaceEntry implements ObservableState
{
	private ModelEntry modelEntry = null;
	private boolean changed = true;
	private boolean temporary = true;
	private final Framework framework;
	private final Workspace workspace;
	private final MementoManager history = new MementoManager();
	private boolean canDo = true;
	private byte[] capturedMemento = null;
	private byte[] savedMemento = null;

	public WorkspaceEntry(Workspace workspace) {
		this.workspace = workspace;
		this.framework = workspace.getFramework();
	}

	public void setChanged(boolean changed) {
		if(this.changed != changed) {
			this.changed = changed;
			workspace.fireEntryChanged(this);
		}
	}

	public boolean isChanged() {
		return changed;
	}

	public ModelEntry getModelEntry()
	{
		return modelEntry;
	}

	private StateObserver modelObserver = new StateObserver(){
		@Override
		public void notify(StateEvent e) {
			observableState.sendNotification(e);
		}
	};

	private StateSupervisor modelSupervisor = new StateSupervisor() {
		@Override
		public void handleEvent(StateEvent e) {
			if (e instanceof PropertyChangedEvent || e instanceof TransformChangedEvent) {
				setChanged(true);
			}
		}
	};

	public void setModelEntry(ModelEntry modelEntry)
	{
		if(this.modelEntry != null) {
			this.modelEntry.getVisualModel().removeObserver(modelObserver);
			modelSupervisor.detach();
		}
		this.modelEntry = modelEntry;

		observableState.sendNotification(new StateEvent() {
			@Override
			public Object getSender() {
				return this;
			}
		});
		this.modelEntry.getVisualModel().addObserver(modelObserver);

		modelSupervisor.attach(this.modelEntry.getVisualModel().getRoot());
	}

	public boolean isWork() {
		return (modelEntry != null) || (getWorkspacePath().getNode().endsWith(".work"));
	}

	public String getTitle() {
		String res;
		String name = getWorkspacePath().getNode();
		if (isWork()) {
			int dot = name.lastIndexOf('.');
			if (dot == -1)
				res = name;
			else
				res = name.substring(0,dot);
		} else
			res = name;

		return res;
	}

	@Override
	public String toString() {
		String res = getTitle();

		if (modelEntry != null)
			if (modelEntry.isVisual())
				res = res + " [V]";

		if (changed)
			res = "* " + res;

		if (temporary)
			res = res + " (not in workspace)";

		return res;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	public Path<String> getWorkspacePath() {
		return workspace.getPath(this);
	}

	public File getFile() {
		return workspace.getFile(this);
	}


	ObservableStateImpl observableState = new ObservableStateImpl();

	@Override
	public void addObserver(StateObserver obs) {
		observableState.addObserver(obs);
	}

	@Override
	public void removeObserver(StateObserver obs) {
		observableState.removeObserver(obs);
	}

	public void updateDoState() {
		MainWindowActions.EDIT_UNDO_ACTION.setEnabled(canDo && history.canUndo());
		MainWindowActions.EDIT_REDO_ACTION.setEnabled(canDo && history.canRedo());
	}

	public void setCanDo(boolean canDo) {
		this.canDo = canDo;
		updateDoState();
	}

	public void captureMemento() {
		capturedMemento = framework.save(getModelEntry());
		if (changed == false) {
			savedMemento = capturedMemento;
		}
	}

	public void cancelMemento() {
		setModelEntry(framework.load(capturedMemento));
		setChanged(savedMemento != capturedMemento);
		capturedMemento = null;
	}

	public void saveMemento() {
		byte[] currentMemento = capturedMemento;
		capturedMemento = null;
		if (currentMemento == null) {
			currentMemento = framework.save(getModelEntry());
		}
		if (changed == false) {
			savedMemento = currentMemento;
		}
		history.pushUndo(currentMemento);
		history.clearRedo();
		updateDoState();
	}

	public void undo() {
		if (history.canUndo()) {
			byte[] undoMemento = history.pullUndo();
			if (undoMemento != null) {
				byte[] currentMemento = framework.save(getModelEntry());
				if (changed == false) {
					savedMemento = currentMemento;
				}
				history.pushRedo(currentMemento);
				setModelEntry(framework.load(undoMemento));
				setChanged(undoMemento != savedMemento);
			}
		}
		updateDoState();
	}

	public void redo() {
		if (history.canRedo()) {
			byte[] redoMemento = history.pullRedo();
			if (redoMemento != null) {
				byte[] currentMemento = framework.save(getModelEntry());
				if (changed == false) {
					savedMemento = currentMemento;
				}
				history.pushUndo(currentMemento);
				setModelEntry(framework.load(redoMemento));
				setChanged(redoMemento != savedMemento);
			}
		}
		updateDoState();
	}

}