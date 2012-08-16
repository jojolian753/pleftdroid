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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * @author rmassera
 * 
 * Singleton that manages the retrieving of the Contact Details
 */

public enum ContactDetails {
	INSTANCE;

 	private ContentResolver cr=null;
	
	public ArrayList<HashMap<String, String>> getDnameEmailList() {
		// The Display Name / Email ArrayList to return
		ArrayList<HashMap<String, String>> dneal = new ArrayList<HashMap<String, String>>();
		// The HashSet used to eliminate the Duplicate entries
		Set<HashMap<String, String>> no_dups = new HashSet<HashMap<String, String>>();
		
		// Ordering by Display Name
		String sortOrderDnE = "lower(" + ContactsContract.Data.DISPLAY_NAME + ") ASC";
		//TODO The current query returns ALL e-mail you ever got in contact with
		//TODO Hint:they seem collected by stuff like Gmail, Facebook, etc. (Accounts)
		//TODO Cross check with query in ContactsContract.Contacts which seems to have only contacts you see in Contacts
		String[] projectionDnE = new String[] {
		        ContactsContract.Data.DISPLAY_NAME,
		        ContactsContract.CommonDataKinds.Email.DATA };
		Cursor emailsc = this.cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projectionDnE,  
			      null, null, sortOrderDnE);
		try{
			if (emailsc.getCount() < 1) { return null; }
			emailsc.moveToFirst();
			int dnameColumn = emailsc.getColumnIndex(
		            ContactsContract.Data.DISPLAY_NAME);
		    int emailColumn = emailsc.getColumnIndex(
		                    ContactsContract.CommonDataKinds.Email.DATA);
		    String dname=null;
		    String email=null;
		    HashMap<String, String> map=null;
			do {
				email = emailsc.getString(emailColumn);
				if(this.isValidEmail(email)) {
					dname = emailsc.getString(dnameColumn);
					map = new HashMap<String, String>();
					map.put("dname", dname);
					map.put("email", email);
					//TODO add another map with lowercase dname and email so the check for dups is case insensitive
					if(no_dups.add(map)) { dneal.add( map ); }
				}
			} while (emailsc.moveToNext());
		} finally {
			emailsc.close();
		}
		
		return dneal;
	}
 	
 	public boolean isValidEmail(String email) {
 		email = email.trim();
 		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
 		 Matcher m = p.matcher(email);
 		 boolean matchFound = m.matches();
 		if(matchFound){
 		 return true;
 		 }else{
 		 return false;
 		 }
 	}
	
 	public void init(ContentResolver icr) {
 		if(cr==null) { cr = icr; }
 	}

}
