package org.workcraft.testing.serialisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Import;

public class DotGSerialiserTests {
	//@Test
	//TODO: Make real test.
	public void test() throws IOException, ModelValidationException, SerialisationException, DeserialisationException
	{
		STG good = getModel(true);

		Set<Place> goodTokenized = getTokenizedPlaces(good);
		ArrayList<Integer> goodIds = getSortedIds(good, goodTokenized);
		print(goodIds);

		for(int i=0;i<50;i++)
		{
			STG bad = getModel(false);

			Set<Place> badTokenized = getTokenizedPlaces(bad);

			ArrayList<Integer> badIds = getSortedIds(bad, badTokenized);

			HashSet<Integer> missing = new HashSet<Integer>(goodIds);
			missing.removeAll(badIds);

			print(missing);
		}
	}

	private void print(Collection<Integer> badIds) {
		for(Integer i : badIds)
			System.out.print(i+" ");
		System.out.println();
	}

	private ArrayList<Integer> getSortedIds(STG stg, Set<Place> goodTokenized) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(Place place : goodTokenized)
			ids.add(stg.getNodeID(place));
		Collections.sort(ids);
		return ids;
	}

	private Set<Place> getTokenizedPlaces(STG stg) {
		HashSet<Place> places = new HashSet<Place>();
		for(Place place : stg.getPlaces())
			if(place.getTokens() == 1)
				places.add(place);
		return places;
	}

	private STG getModel(boolean good) throws IOException, DeserialisationException {
		int reimportedTokens;
		do
		{
			STG model = (STG)Import.importFromFile(new DotGImporter(), "D:\\Work\\Out\\fetchA-fetchAmB-varA-muxA.g.ren.g");

			reimportedTokens = getTokenCount(model);

			if((reimportedTokens == 17) == good)
				return model;
		} while(true);
	}

	private int getTokenCount(STG model) {
		int result = 0;
		for(Place place : model.getPlaces())
			result += place.getTokens();
		return result;
	}
}
