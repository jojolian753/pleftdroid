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

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author r.massera
 * 
 * Class that manages the Editing of an Event
 * 
 */
public class EditEventActivity extends Activity {
	protected static final int ACT_INVITEE=1;
	protected static final int ACT_ADDDATE=2;
	protected static final int RMINVITEE_ID=1;
	protected static final int RMDATE_ID=2;
	public static final String DN_EMAIL="THEEMAIL";
	public static final String DN_NAME="THENAME";
	public static final String DT_DATE="THEDATE";
	public static final String DT_TIME="THETIME";
	
	private EditEventActivity mTHISEVENT;
	protected ArrayList<PDate> mDates;
	private ArrayList<String> mEmails;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if(AppPreferences.INSTANCE.getUsePleftTheme()) { setTheme(R.style.Theme_Pleft); }
		
    	super.onCreate(savedInstanceState);
        // Initialiazations
        mTHISEVENT=this;
        mDates = new ArrayList<PDate>();
        mEmails = new ArrayList<String>();
        // The View
        setContentView(R.layout.edit_event);
        // Add Invitee Button
        ((Button) findViewById(R.id.addinvitee)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent iai = new Intent(mTHISEVENT, SelectContactsActivity.class);
	        	startActivityForResult(iai, ACT_INVITEE);
			}
        });
        
        // Add Date Button
        ((Button) findViewById(R.id.adddate)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent iad = new Intent(mTHISEVENT, PickDateDialogActivity.class);
				// This would not work: you need a Parcelable
				//iad.putExtra(PickDateDialogActivity.DTLIST, mDates);
				iad.putParcelableArrayListExtra(PickDateDialogActivity.DTLIST, mDates);
	        	startActivityForResult(iad, ACT_ADDDATE);
			}
        });
			
        // Send Invitations Button
        ((Button) findViewById(R.id.sendinvite)).setOnClickListener(new OnClickListener() {
			private int SC;

			@SuppressWarnings("static-access")
			public void onClick(View v) {
				String desc=((EditText) findViewById(R.id.edescription)).getText().toString();
				String invitees=((EditText) findViewById(R.id.einvitees)).getText().toString();
				String dates = getPleftDates();//"2011-06-23T21:00:00\n2011-06-24T21:00:00\n2011-06-25T21:00:00";
				boolean proposemore = ((CheckBox) findViewById(R.id.mayproposedate)).isChecked();
				if(desc==null || desc.length()==0 ||
				   invitees==null || invitees.length()==0 ||
				   dates==null || dates.length()==0) {
					Toast.makeText(EditEventActivity.this, R.string.event_completeform, Toast.LENGTH_LONG).show();
				} else {
					// Get Preferences
					
					//Toast.makeText(EditEventActivity.this, "invitees: "+invitees+"\nDesc="+desc+"\npserver="+pserver, Toast.LENGTH_LONG).show();
					
					SC=PleftBroker.INSTANCE.createAppointment(desc, invitees, dates,
							AppPreferences.INSTANCE.getPleftServer().trim(), //pserver,
							AppPreferences.INSTANCE.getName().trim(), //uname,
							AppPreferences.INSTANCE.getEmail().trim(), //uemail,
							proposemore);

					if(SC==HttpStatus.SC_OK) {
						PleftDroidDbAdapter mDbAdapter = new PleftDroidDbAdapter(EditEventActivity.this);
						mDbAdapter.open();
						mDbAdapter.createAppointmentAsInvitor(
								0, desc,
								AppPreferences.INSTANCE.getPleftServer().trim(),
								AppPreferences.INSTANCE.getEmail().trim());
						mDbAdapter.close();
					}
					
					Bundle bundle = new Bundle();
					bundle.putInt(PleftDroidActivity.SC_CREATE, SC);

					Intent i = new Intent();
					i.putExtras(bundle);
					setResult(RESULT_OK, i);
					// Close activity
					finish();
				}
			}	    

        });

    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, RMINVITEE_ID,0, R.string.menu_rminvitee).setIcon(android.R.drawable.ic_input_delete);
		menu.add(0, RMDATE_ID,0, R.string.menu_rmdate).setIcon(android.R.drawable.ic_menu_day);
        return super.onCreateOptionsMenu(menu);
	}
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case RMINVITEE_ID:
        	if(mEmails.size()>0) {
        		mEmails.remove(mEmails.size()-1);
        		((EditText) findViewById(R.id.einvitees)).setText(getInvitees());
        		}
        	return true;
        case RMDATE_ID:
        	if(mDates.size()>0) {
        		mDates.remove(mDates.size()-1);
        		((EditText) findViewById(R.id.edatetimes)).setText(getDates());
        	}
        	return true;
		}
		return super.onOptionsItemSelected(item);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data!=null) {
			Bundle extras = data.getExtras();

			switch(requestCode) {
			case ACT_INVITEE:
				String email=extras.getString(DN_EMAIL);
				String dname=extras.getString(DN_NAME);
				if(isUseDispNameSet()) { email = dname+" <"+email+">"; }
				mEmails.add(email);
				((EditText) findViewById(R.id.einvitees)).setText(getInvitees());
				break;
			case ACT_ADDDATE:
				mDates = data.getParcelableArrayListExtra(PickDateDialogActivity.DTLIST);
				((EditText) findViewById(R.id.edatetimes)).setText(getDates());
				break;

			}
		}

	}

	/**
	 * @return
	 */
	private String getInvitees() {
		String es="";
		if(mEmails.isEmpty()) return es;
		for(String e : mEmails) {
			es+=e+", ";
		}
		return es.substring(0, (es.length()-2));
	}
	
	/**
	 * 
	 */
	private boolean isUseDispNameSet() {
		// is preference "show appointments details" set?
        return AppPreferences.INSTANCE.getUseDisplayName();        
	}
	
	private String getPleftDates() {
		String s="";
		if(mDates.isEmpty()) return s;
		for(PDate p : mDates) {
			s+=p.getPleftDate()+"\n";
		}
		s=s.substring(0, (s.length()-1));
		return s;
	}
	private String getDates() {
		String s="";
		if(mDates.isEmpty()) return s;
		for(PDate p : mDates) {
			s+=p.getDateString()+", ";
			
		}
		s=s.substring(0, (s.length()-2));
		return s;
	}
}
