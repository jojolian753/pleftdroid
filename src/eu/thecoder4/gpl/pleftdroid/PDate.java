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


package eu.thecoder4.gpl.pleftdroid;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author r.massera
 * 
 * This Class represents a Pleft Date and is responsible for storing,
 * parceling and unparceling the dates.
 *
 */
public class PDate implements Parcelable{
	private int[] date;
	private int[] time;
	public PDate(int[] date, int[] time) {
		super();
		this.date = date;
		this.time = time;
	}
	public int[] getDate() {
		return date;
	}
	public void setDate(int[] date) {
		this.date = date;
	}
	public int[] getTime() {
		return time;
	}
	public void setTime(int[] time) {
		this.time = time;
	}

	// A Pleft String is like 2011-06-23T21:00:00
	public String getPleftDate() {
		String dt="";
		dt+=date[0]<10 ? "0"+date[0] : date[0]; dt+="-";
		dt+=date[1]<9 ? "0"+(date[1]+1) : (date[1]+1); dt+="-";
		dt+=date[2]<10 ? "0"+date[2] : date[2];
		dt+="T";
		dt+=time[0]<10 ? "0"+time[0] : time[0]; dt+=":";
		dt+=time[1]<10 ? "0"+time[1] : time[1]; dt+=":00";
		return dt;
	}

	public String getDateString() {
		String dt="";
		dt+=date[0]<10 ? "0"+date[0] : date[0]; dt+="-";
		dt+=date[1]<9 ? "0"+(date[1]+1) : (date[1]+1); dt+="-";
		dt+=date[2]<10 ? "0"+date[2] : date[2];
		dt+=" @ ";
		dt+=time[0]<10 ? "0"+time[0] : time[0]; dt+=":";
		dt+=time[1]<10 ? "0"+time[1] : time[1]; //dt+=":00";
		return dt;
	}
	
	/*
	 * Methods for Parceling (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeIntArray(date);
		out.writeIntArray(time);
	}
	public static final Parcelable.Creator<PDate> CREATOR
	= new Parcelable.Creator<PDate>() {
		public PDate createFromParcel(Parcel in) {
			return new PDate(in);
		}

		public PDate[] newArray(int size) {
			return new PDate[size];
		}


	};
	public PDate(Parcel in) {
		this.date = new int[3];
		this.time = new int[2];
		in.readIntArray(this.date);
		in.readIntArray(this.time);
	}
	@Override
	public boolean equals(Object o) {
		// A PDate is equal if it represents the same date & time
		if (this == o) // Are they exactly the same instance?
			return true;

		if (o == null) // Is the object being compared null?
			return false;

		if (!(o instanceof PDate)) // Is the object being compared the same Type?
			return false;

		PDate cdate = (PDate)o; // Convert the object to a PDate
		if(Arrays.equals(this.date,cdate.date) && Arrays.equals(this.time,cdate.time)) {
			return true;
		}
		return false;

	}
	/*
	 * Utility methods
	 */
	public boolean sameDate(Object o) {
		if (!(o instanceof PDate)) // Is the object being compared the same Type?
			return false;

		PDate cdate = (PDate)o; // Convert the object to a PDate
		return Arrays.equals(this.date,cdate.date);
	}
	public boolean dateIsAfter(int yyyy, int mm, int dd) {
		if(yyyy < this.date[0]) return true;
		if(this.date[0]==yyyy && mm < this.date[1]) return true;
		if(this.date[0]==yyyy && this.date[1] == mm && dd < this.date[2]) return true;
		return false;
	}

}
