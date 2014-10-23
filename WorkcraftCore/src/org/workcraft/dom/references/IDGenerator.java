/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.references;

import java.util.Comparator;
import java.util.TreeSet;

import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.util.Pair;

public class IDGenerator
{
	private static final Comparator<? super Pair<Integer, Integer>> comparator = new Comparator<Pair<Integer, Integer>>()
			{
				@Override
				public int compare(Pair<Integer, Integer> o1,
						Pair<Integer, Integer> o2) {
					return o1.getFirst().compareTo(o2.getFirst());
				}
			};

	TreeSet<Pair<Integer, Integer>> takenRanges = new TreeSet<Pair<Integer, Integer>>(comparator);

	public void reserveID(int id) {
		final Pair<Integer, Integer> point = emptyRange(id);
		final Pair<Integer, Integer> floor = takenRanges.floor(point);
		final Pair<Integer, Integer> ceiling = takenRanges.ceiling(point);

		if(floor != null && id < floor.getSecond())
			throw new DuplicateIDException(id);
		if(ceiling != null && id == ceiling.getFirst().intValue())
			throw new DuplicateIDException(id);

		boolean mergeFirst = floor != null && id == floor.getSecond();
		boolean mergeSecond = ceiling != null && id == ceiling.getFirst()-1;

		if(mergeFirst)
			takenRanges.remove(floor);
		if(mergeSecond)
			takenRanges.remove(ceiling);

		int left = id;
		int right = id+1;

		if(mergeFirst)
			left = floor.getFirst();
		if(mergeSecond)
			right = ceiling.getSecond();

		takenRanges.add(Pair.of(left, right));
	}

	public void releaseID(int id) {
		final Pair<Integer, Integer> range = takenRanges.floor(emptyRange(id));

		if(range == null || range.getSecond() <= id)
			throw new RuntimeException("ID is above the range");
		if(range.getFirst() > id)
			throw new RuntimeException("ID is below the range");

		Pair<Integer, Integer> r1 = Pair.of(range.getFirst(), id);
		Pair<Integer, Integer> r2 = Pair.of(id+1, range.getSecond());

		takenRanges.remove(range);
		tryAddRange(r1);
		tryAddRange(r2);
	}

	private Pair<Integer, Integer> emptyRange(Integer id) {
		return Pair.of(id, id);
	}

	private void tryAddRange(Pair<Integer, Integer> r2) {
		if(takenRanges.floor(emptyRange(r2.getFirst())) != takenRanges.floor(emptyRange(r2.getSecond())))
			throw new RuntimeException("taken range error");

		if(!r2.getSecond().equals(r2.getFirst()))
			takenRanges.add(r2);
	}

	public int getNextID() {
		final int result = (takenRanges.size() > 0 && takenRanges.first().getFirst().intValue() == 0) ? takenRanges.first().getSecond().intValue() : 0;
		reserveID(result);
		return result;
	}

	public boolean isEmpty() {
		return takenRanges.isEmpty();
	}
}
