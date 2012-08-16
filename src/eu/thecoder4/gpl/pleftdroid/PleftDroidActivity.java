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

import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * 
 * @author r.massera
 * 
 * This is the Main Activity of the app.
 *
 */
public class PleftDroidActivity extends ListActivity implements Observer {
	//Version
	public static int APPVERSIONCODE;
	public static String APPVERSION;

	//Activities Constants
	public static final int ACT_CREATE = 1;
	public static final int ACT_DETAIL = 2;

	protected static final String SC_CREATE = "HTTPSC";
	//Menus
	public static final int CREATE_ID = Menu.FIRST;
	public static final int PREFS_ID = Menu.FIRST + 1;
	public static final int ABOUT_ID = Menu.FIRST + 2;

	public static final int DELETE_ID = Menu.FIRST;
	// DB Adapter
	PleftDroidDbAdapter mDbAdapter;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	AppPreferences.init(this);

    	if(AppPreferences.INSTANCE.getUsePleftTheme()) { setTheme(R.style.Theme_Pleft); }

        super.onCreate(savedInstanceState);
        initVersion();
        
        setContentView(R.layout.main);
        this.setTitle("PleftDroid - "+APPVERSION);
        registerForContextMenu(getListView());
        //Check if Eula has been accepted and display it if needed
        
        checkPreferences();
        // If you want to show Eula uncomment the following 5 lines and comment the line above ( checkPreferences(); )
        /*if(!AppPreferences.INSTANCE.getEulaAccepted()) {
        	EulaDialog eulad = new EulaDialog(this);
        	eulad.addObserver(this);
        	eulad.show();
        }*/ //You can get rid of this if you do not want the EULA
        
        
        //Toast.makeText(this, "Locale: '"+Locale.getDefault().getLanguage()+"'", Toast.LENGTH_LONG).show();
        
		ContactDetails.INSTANCE.init(getContentResolver());
        mDbAdapter = new PleftDroidDbAdapter(this);
    }

	@Override
	protected void onStart() {
		
		super.onStart();
		fillList();
	}

	/**
	 * 
	 */
	private boolean preferencesComplete() {
		// Are Preferences complete?
        if( AppPreferences.INSTANCE.getName().length()==0 ||
        	AppPreferences.INSTANCE.getEmail().length()==0 ||
        	AppPreferences.INSTANCE.getPleftServer().length()==0) {
        	return false;
        }
        return true;        
	}
	
	/**
	 * 
	 */
	private void checkPreferences() {
		if(!preferencesComplete()) {
        	startPreferences();
        }
	}
	/**
	 * 
	 */
	private boolean isPrefDetailListSet() {
		// is preference "show appointments details" set?
        return AppPreferences.INSTANCE.getShowDetails();
	}

	/**
	 * Starts Preferences Activity
	 */
	private void startPreferences() {
		Intent ip = new Intent(this, PreferencesActivity.class);
		startActivity(ip);
		// Close activity
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CREATE_ID,0, R.string.menu_create).setIcon(android.R.drawable.ic_menu_my_calendar);
		menu.add(0, PREFS_ID,0, R.string.menu_prefs).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, ABOUT_ID,0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);

        return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("static-access")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case CREATE_ID:
        	if(preferencesComplete()) {
        		if( PleftBroker.INSTANCE.isInetConnAvailable(this) ) {
        			int SC=PleftBroker.INSTANCE.checkServer(AppPreferences.INSTANCE.getPleftServer());
        			if( SC!=HttpStatus.SC_OK ) {
        				int title=R.string.dialog_title_connproblem;
        				int msg=R.string.toast_createapptfailredir;
        				if(SC==PleftBroker.SC_TIMEOUT) { msg=R.string.dialog_msg_connproblem; }
        				new AlertDialog.Builder(this)
            			.setTitle(title)
            			.setMessage(msg)
            			.setPositiveButton("OK", new OnClickListener() {
            				public void onClick(DialogInterface dif, int arg1) {
            					// Do nothing
            					dif.dismiss();
            				}
            			})
            			.show();
        			
        			} else {
        				Intent ie = new Intent(this, EditEventActivity.class);
            			startActivityForResult(ie, ACT_CREATE);
        				
        			}
        		} else {
        			new AlertDialog.Builder(this)
        			.setTitle(R.string.dialog_title_nonet)
        			.setMessage(R.string.dialog_msg_nonet)
        			.setPositiveButton("OK", new OnClickListener() {
        				public void onClick(DialogInterface dif, int arg1) {
        					// Do nothing
        					dif.dismiss();
        				}
        			})
        			.show();
        		}
        	} else {
        		new AlertDialog.Builder(this)
        		.setTitle(R.string.dialog_title_settingsreq)
        		.setMessage(R.string.dialog_msg_settingsreq)
        		.setPositiveButton("OK", new OnClickListener() {
        			public void onClick(DialogInterface dif, int arg1) {
        				startPreferences();
        			}
        		})
        		.show();
        	}
            return true;
        case PREFS_ID:
        	startPreferences();
            return true;
        case ABOUT_ID:
        	new AboutDialog(this).show();
            return true;
        
        }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//TODO icon?
		menu.add(0, DELETE_ID, 0, R.string.menu_delete).setIcon(android.R.drawable.ic_menu_delete);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
        case DELETE_ID:
        	final long aid;
        	aid = info.id;
        	new AlertDialog.Builder(this)
        	.setTitle(R.string.dialog_title_deleteappt)
        	.setMessage(R.string.dialog_msg_deleteappt)
        	.setPositiveButton(android.R.string.ok, new OnClickListener() {
        	    public void onClick(DialogInterface arg0, int arg1) {
        	        // Proceed and delete this
        	    	mDbAdapter.open();
        	    	mDbAdapter.deleteAppointment(aid);
        	    	mDbAdapter.close();
        	    	fillList();
        	    }
        	})
        	.setNegativeButton(android.R.string.cancel, new OnClickListener() {
        	    public void onClick(DialogInterface arg0, int arg1) {
        	        // Do nothing
        	    }
        	})
        	.show();
        	
        	return true;
		}

		return super.onContextItemSelected(item);
	}

	@SuppressWarnings("static-access")
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mDbAdapter.open();
		int aid = mDbAdapter.getAid(id);
		mDbAdapter.close();
		if(aid>0) {
			if( PleftBroker.INSTANCE.isInetConnAvailable(this) ) {
				Intent i = new Intent(this, EventDetailActivity.class);
				i.putExtra(EventDetailActivity.AID, aid);
				startActivity(i);
	        	} else {
	        		new AlertDialog.Builder(this)
	            	.setTitle(R.string.dialog_title_nonet)
	            	.setMessage(R.string.dialog_msg_nonet)
	            	.setPositiveButton("OK", new OnClickListener() {
	            	    public void onClick(DialogInterface arg0, int arg1) {
	            	        // Do nothing
	            	    }
	            	})
	            	.show();
	        	}

		} else {
		new AlertDialog.Builder(this)
    	.setTitle(R.string.dialog_title_noapptdetails)
    	.setMessage(R.string.dialog_msg_noapptdetails)
    	.setPositiveButton("OK", new OnClickListener() {
    	    public void onClick(DialogInterface arg0, int arg1) {
    	        
    	    }
    	}).show();
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && data!=null) {
			super.onActivityResult(requestCode, resultCode, data);
			Bundle extras = data.getExtras();
			switch(requestCode) {
			case ACT_CREATE:
				int SC = extras.getInt(SC_CREATE, 500);
				switch (SC) {
				case HttpStatus.SC_OK:
					new AlertDialog.Builder(this)
			    	.setTitle(R.string.dialog_title_apptcreated)
			    	.setMessage(R.string.dialog_msg_apptcreated)
			    	.setPositiveButton("OK", new OnClickListener() {
			    	    public void onClick(DialogInterface arg0, int arg1) {
			    	        
			    	    }
			    	}).show();
					//Toast.makeText(this, R.string.toast_apptcreated, Toast.LENGTH_SHORT).show();
					break;
				case PleftBroker.SC_CLIREDIR:
					Toast.makeText(this, R.string.toast_createapptfailredir, Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(this, R.string.toast_createapptfail, Toast.LENGTH_LONG).show();
				}
				
				break;
			}
		}
	}
	
	private void fillList() {
		mDbAdapter.open();
        // Get all of the appointments from the database and create the item list
        Cursor c = mDbAdapter.fetchAllAppointmentsList();
        startManagingCursor(c);
        if( c.getCount()>0 ) {

        String[] from = null; int[] to = null;
        SimpleCursorAdapter apts = null;
        if(isPrefDetailListSet()) {
        	from = new String[] { PleftDroidDbAdapter.COL_DESC, PleftDroidDbAdapter.COL_ROLE, PleftDroidDbAdapter.COL_STATUS,
        			              PleftDroidDbAdapter.COL_PSERVER, PleftDroidDbAdapter.COL_USER };
        	to = new int[] { R.id.adesc, R.id.arole, R.id.astatus, R.id.apserver, R.id.auser };
        	// Now create an array adapter and set it to display using our row
        	apts = new SimpleCursorAdapter(this, R.layout.event_row_details, c, from, to);
        } else {
        	from = new String[] { PleftDroidDbAdapter.COL_DESC, PleftDroidDbAdapter.COL_ROLE, PleftDroidDbAdapter.COL_STATUS };
        	to = new int[] { R.id.adesc, R.id.arole, R.id.astatus };
        	// Now create an array adapter and set it to display using our row
        	apts = new SimpleCursorAdapter(this, R.layout.event_row, c, from, to);
        }
        this.getListView().destroyDrawingCache();
        this.getListView().setVisibility(ListView.INVISIBLE);
        this.getListView().setVisibility(ListView.VISIBLE);
        this.getWindow().getDecorView().invalidate();

        setListAdapter(apts);
        apts.notifyDataSetChanged();
        }
        mDbAdapter.close();
    }
	private void initVersion() {
		PackageInfo pi = null;
		try {
			pi = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
			APPVERSION=pi.versionName;
			APPVERSIONCODE=pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		checkPreferences();
	}
	
}