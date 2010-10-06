package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;

public interface ConnectionResult {
	public MathConnection getSimpleResult();
	public MathConnection getCon1();
	public MathConnection getCon2();
	public STGPlace getImplicitPlace();
}