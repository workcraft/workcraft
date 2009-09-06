package org.workcraft.plugins.sdfs;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

@DisplayName ("Static Data Flow Structure")
@VisualClass ("org.workcraft.plugins.sdfs.VisualSDFS")
public class SDFS extends AbstractMathModel {

	public class Listener implements HierarchyObserver {
		@Override
		public void notify(HierarchyEvent e) {
			if(e instanceof NodesAddedEvent)
			{
				for(Node node : e.getAffectedNodes())
				{
					if (node instanceof Register)
						registers.add((Register)node);
					else if (node instanceof Logic)
						logic.add((Logic)node);
				}
			}
			if(e instanceof NodesDeletedEvent)
			{
				for(Node node : e.getAffectedNodes())
				{
					if (node instanceof Register)
						registers.remove(node);
					else if (node instanceof Logic)
						logic.remove(node);
				}
			}
		}
	}

	private HashSet<Register> registers = new HashSet<Register>();
	private HashSet<Logic> logic = new HashSet<Logic>();

	public SDFS() {
		super();
		addObserver(new Listener());
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	final public Register createRegister() {
		Register newRegister = new Register();
		add(newRegister);
		return newRegister;
	}

	final public Logic createLogic() {
		Logic newLogic = new Logic();
		add(newLogic);
		return newLogic;
	}

	final public Set<Register> getRegisters() {
		return new HashSet<Register>(registers);
	}

	final public Set<Logic> getLogic() {
		return new HashSet<Logic>(logic);
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first == second)
			throw new InvalidConnectionException ("Self-loops are not allowed");
	}
}
