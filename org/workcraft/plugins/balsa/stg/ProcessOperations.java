package org.workcraft.plugins.balsa.stg;

import java.util.Collection;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;


public class ProcessOperations {
	private final StrictPetriBuilder builder;

	ProcessOperations(StrictPetriBuilder builder)
	{
		this.builder = builder;
	}

	public PassiveSync enclosure(PassiveSync enclosing, ActiveSync enclosed)
	{
		builder.connect(enclosing.go(), enclosed.go());
		builder.connect(enclosed.done(), enclosing.done());
		return enclosing;
	}

	public ActiveSync sequence(final Collection<ActiveSync> processes)
	{
		return sequence(processes.toArray(new ActiveSync[0]));
	}

	public ActiveSync sequence(final ActiveSync[] processes)
	{
		for(int i=1;i<processes.length;i++)
			builder.connect(processes[i-1].done(), processes[i].go());
		return new ActiveSync()
		{
			@Override
			public OutputEvent go() {
				return processes[0].go();
			}
			@Override
			public InputEvent done() {
				return processes[processes.length-1].done();
			}
		};
	}

}
