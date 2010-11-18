package org.workcraft.plugins.circuit;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.Place;

public class VisualCircuitConnection extends VisualConnection {
	private Place referencedZeroPlace=null;
	private Place referencedOnePlace=null;

	public VisualCircuitConnection() {
		super();
	}

	public VisualCircuitConnection(MathConnection c) {
		super();
	}

	public VisualCircuitConnection(MathConnection con, VisualComponent c1,
			VisualComponent c2) {
		super(con, c1, c2);
	}

	public void setReferencedZeroPlace(Place referencedPlace) {
		this.referencedZeroPlace = referencedPlace;
	}

	@Override
	public double getLineWidth() {
		return CircuitSettings.getCircuitWireWidth();
	}

	public Place getReferencedZeroPlace() {
		return referencedZeroPlace;
	}

	public void setReferencedOnePlace(Place referencedPlace) {
		this.referencedOnePlace = referencedPlace;
	}

	public Place getReferencedOnePlace() {
		return referencedOnePlace;
	}

/*	@Override
	public Color getDrawColor()
	{
		if (referencedOnePlace==null||
			referencedZeroPlace==null) return super.getDrawColor();

		if (referencedOnePlace.getTokens()==1&&
				referencedZeroPlace.getTokens()==0) return Color.RED;
		if (referencedOnePlace.getTokens()==0&&
				referencedZeroPlace.getTokens()==1) return Color.BLUE;

		return super.getDrawColor();
	}*/

}
