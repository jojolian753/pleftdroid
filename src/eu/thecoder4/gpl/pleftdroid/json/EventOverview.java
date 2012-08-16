/*
 * Copyright (C) 2011, 2012 Riccardo Massera, r.massera@thecoder4.eu
 * 
 * This file is part of PleftDroid.
 * 
 * PleftDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PleftDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PleftDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.thecoder4.gpl.pleftdroid.json;

import java.util.ArrayList;
import java.util.Map;

public class EventOverview {
	
	private ArrayList<ADate> dates;
	private ArrayList<APerson> people;
	private int invitee;
	private AMeta meta;
 	private Map<String, Map<String, ArrayList<String> > >  availability;

	public ArrayList<ADate> getDates() {
		return dates;
	}

	public void setDates(ArrayList<ADate> dates) {
		this.dates = dates;
	}

	public ArrayList<APerson> getPeople() {
		return people;
	}

	public void setPeople(ArrayList<APerson> people) {
		this.people = people;
	}

	public int getInvitee() {
		return invitee;
	}

	public void setInvitee(int invitee) {
		this.invitee = invitee;
	}

	public AMeta getMeta() {
		return meta;
	}

	public void setMeta(AMeta meta) {
		this.meta = meta;
	}

	public Map<String, Map<String, ArrayList<String>>> getAvailability() {
		return availability;
	}

	public void setAvailability(
			Map<String, Map<String, ArrayList<String>>> availability) {
		this.availability = availability;
	}

	@Override
	public String toString() {
		return "Dates: "+dates+"\nPeople: "+people+"\nInv: "+invitee+"\nMeta: "+meta+"\nAVS: "+availability.toString();
	}
	
}
