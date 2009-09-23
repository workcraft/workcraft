package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.InputPlace;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.petri.Place;

class StgModelStgPlace implements OutputPlace, InputPlace
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
