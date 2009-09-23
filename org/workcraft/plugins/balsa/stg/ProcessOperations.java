package org.workcraft.plugins.balsa.stg;

import java.util.Collection;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;


public class ProcessOperations {
	private final StgBuilder builder;

	ProcessOperations(StgBuilder builder)
	{
		this.builder = builder;
	}

	public PassiveProcess enclosure(PassiveProcess enclosing, ActiveProcess enclosed)
	{
		builder.connect(enclosing.go(), enclosed.go());
		builder.connect(enclosed.done(), enclosing.done());
		return enclosing;
	}

	public ActiveProcess sequence(final Collection<ActiveProcess> processes)
	{
		return sequence(processes.toArray(new ActiveProcess[0]));
	}

	public ActiveProcess sequence(final ActiveProcess[] processes)
	{
		for(int i=1;i<processes.length;i++)
			builder.connect(processes[i-1].done(), processes[i].go());
		return new ActiveProcess()
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

	public void multiple(final PassiveProcess process)
	{
		builder.connect(process.done(), process.go());
	}
}
