package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.petri.Place;

class StgModelStgPlace implements StgPlace
{
	private final Place petriPlace;

	public StgModelStgPlace(Place petriPlace)
	{
		this.petriPlace = petriPlace;
	}

	public Place getPetriPlace() {
		return petriPlace;
	}
}
