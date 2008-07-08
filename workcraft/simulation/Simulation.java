package org.workcraft.simulation;

public interface Simulation {
	public void simStarting();
	public void simStopped();
	public void simStep();
	public void simUpdate(float time);
}