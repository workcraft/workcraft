package org.workcraft.parsers.breeze.splitting;

import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;

public class PartSplitResult
{

	public PartSplitResult(
			BreezeComponent data,
			BreezeComponent control,
			List<? extends BreezeConnection> connections) {
		super();
		this.data = data;
		this.control = control;
		this.connections = connections;
	}
	private final BreezeComponent data;
	private final BreezeComponent control;
	private final List<? extends BreezeConnection> connections;
	public BreezeComponent getControl() {
		return control;
	}
	public BreezeComponent getData() {
		return data;
	}
	public Collection<? extends BreezeConnection> getConnections() {
		return connections;
	}
}
