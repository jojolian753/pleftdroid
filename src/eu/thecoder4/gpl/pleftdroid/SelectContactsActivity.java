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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SelectContactsActivity extends Activity implements OnItemClickListener {
	
	//Menus
	public static final int ADD_ID = Menu.FIRST;
	//Options
	public static final String ASK_CONFIRM = "ASKOK";

	private ArrayList<HashMap<String, String>> fillMaps;
	private boolean confirm=false;
	private int selectedpos;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_contact);
		
		Bundle extras = getIntent().getExtras();
    	if(extras!=null) confirm = extras.getBoolean(ASK_CONFIRM, false);
		
		// get the listview
		ListView eclv = (ListView) findViewById(R.id.emailcontactslist);
		eclv.setFastScrollEnabled(true);
		eclv.setOnItemClickListener(this);

		// prepare the list of all records
		fillMaps = ContactDetails.INSTANCE.getDnameEmailList();

		if(fillMaps==null){
			new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_title_nocontactsemail)
			.setMessage(R.string.dialog_msg_nocontactsemail)
			.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					finish();
				}
			}).show();
		} else {
			EmailContactsIndexerAdapter<HashMap<String, String>> ecadapter = new EmailContactsIndexerAdapter<HashMap<String, String>>(
					this,
					R.layout.contacts_row,
					fillMaps);
			eclv.setAdapter(ecadapter);
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_ID,0, "Add Contact").setIcon(android.R.drawable.ic_menu_add);

        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case ADD_ID:
        	
        	Intent i=new Intent();
        	i.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.fromParts("tel", "+", null));
            i.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
            i.putExtra(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK); 
            i.putExtra(ContactsContract.Intents.Insert.EMAIL, "type@email");

            startActivity(i);
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class EmailContactsIndexerAdapter<T> extends ArrayAdapter<T> implements SectionIndexer {

		ArrayList<HashMap<String, String>> mContacts;
		HashMap<String, Integer> alphaIndexer;

		String[] sections;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.contacts_row, null);
			}
			HashMap<String, String> ec = mContacts.get(position);
			if(ec!=null) {
				TextView dname = (TextView) v.findViewById(R.id.displayname);
				TextView email = (TextView) v.findViewById(R.id.email);
				dname.setText(ec.get("dname"));
				email.setText(ec.get("email"));
			}

			return v;
		}

		@SuppressWarnings("unchecked")
		public EmailContactsIndexerAdapter(Context context, int textViewResourceId, List<T> objects) {
			super(context, textViewResourceId, objects);
			mContacts = (ArrayList<HashMap<String, String>>) objects;
			// here is the tricky stuff
			alphaIndexer = new HashMap<String, Integer>(); 
			// in this hashmap we will store here the positions for
			// the sections

			int size = mContacts.size();
			for (int i = size - 1; i >= 0; i--) {
				String dname = mContacts.get(i).get("dname");
				alphaIndexer.put(dname.substring(0, 1).toUpperCase(), i); 
				//We store the first letter of the word, and its index.
				//The Hashmap will replace the value for identical keys are putted in
			} 

			// now we have an hashmap containing for each first-letter
			// sections(key), the index(value) in where this sections begins

			// we have now to build the sections(letters to be displayed)
			// array : it must contain the keys, which must be
			// ordered alphabetically

			Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
			// cannot be sorted...

			Iterator<String> it = keys.iterator();
			ArrayList<String> keyList = new ArrayList<String>(); // list can be sorted

			while (it.hasNext()) {
				String key = it.next();
				keyList.add(key);
			}

			Collections.sort(keyList);

			sections = new String[keyList.size()]; // simple conversion to an
			// array of objects
			keyList.toArray(sections);

		}

		@Override
		public int getPositionForSection(int section) {
			String letter = sections[section];

			return alphaIndexer.get(letter);
		}

		@Override
		public int getSectionForPosition(int position) {

			// you will notice it will be never called (right?)
			return 0;
		}

		@Override
		public Object[] getSections() {

			return sections; // to string will be called each object, to display
			// the letter
		}

	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		selectedpos=position;
		if(confirm) {
			new AlertDialog.Builder(this)
        	.setTitle("Confirm")
        	.setMessage("Use the address "+fillMaps.get(position).get("email")+" ?")
        	.setPositiveButton(android.R.string.ok, new OnClickListener() {
        	    public void onClick(DialogInterface arg0, int arg1) {
        	        // Proceed and return Result
        	    	returnResult(selectedpos);
        	    }
        	})
        	.setNegativeButton(android.R.string.cancel, new OnClickListener() {
        	    public void onClick(DialogInterface arg0, int arg1) {
        	        // Do nothing
        	    }
        	})
        	.show();
		} else returnResult(position);
	}

	/**
	 * @param position
	 */
	protected void returnResult(int position) {
		String email="-"; String dname="-";
		HashMap<String, String> ec = fillMaps.get(position);
		if(ec!=null) {
			dname=ec.get("dname");
			email=ec.get("email");
		}
		Bundle bundle = new Bundle();
		bundle.putString(EditEventActivity.DN_EMAIL, email);
		bundle.putString(EditEventActivity.DN_NAME, dname);

		Intent i = new Intent();
		i.putExtras(bundle);
		setResult(RESULT_OK, i);
		// Close activity
		finish();
	}

}
