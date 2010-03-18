package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;

public class SimpleResult implements ConnectionResult {
	private final MathConnection con;

	public SimpleResult(MathConnection con) {
		this.con = con;
	}

	@Override
	public MathConnection getSimpleResult() {
		return con;
	}

	@Override
	public MathConnection getCon1() {
		return null;
	}

	@Override
	public MathConnection getCon2() {
		return null;
	}

	@Override
	public STGPlace getImplicitPlace() {
		return null;
	}
}
