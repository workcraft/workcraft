package org.workcraft.plugins.balsa.stg;

import java.util.Collection;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;


public class ProcessOperations {
	private final StrictPetriBuilder builder;

	ProcessOperations(StrictPetriBuilder builder)
	{
		this.builder = builder;
	}

	public void enclosure(PassiveSync enclosing, ActiveSync enclosed)
	{
		enclosure(builder, enclosing, enclosed);
	}

	public static void enclosure(StrictPetriBuilder builder, PassiveSync enclosing, ActiveSync enclosed)
	{
		builder.connect(enclosing.go(), enclosed.go());
		builder.connect(enclosed.done(), enclosing.done());
	}

	public static ActiveSync sequence(StrictPetriBuilder builder, final Collection<ActiveSync> processes)
	{
		return sequence(builder, processes.toArray(new ActiveSync[0]));
	}

	public static ActiveSync sequence(StrictPetriBuilder builder, final ActiveSync[] processes)
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

	public static ActiveSync parallel(StrictPetriBuilder builder, Collection<ActiveSync> processes)
	{
		return parallel(builder, processes.toArray(new ActiveSync[0]));
	}

	public static ActiveSync parallel(StrictPetriBuilder builder, final ActiveSync[] processes)
	{
		final InputOutputEvent start = builder.buildTransition();
		final InputOutputEvent end = builder.buildTransition();
		for(int i=0;i<processes.length;i++)
		{
			builder.connect(start, processes[i].go());
			builder.connect(processes[i].done(), end);
		}
		return new ActiveSync(){
			@Override
			public OutputEvent go() {
				return start;
			}
			@Override
			public InputEvent done() {
				return end;
			}
		};
	}
}
