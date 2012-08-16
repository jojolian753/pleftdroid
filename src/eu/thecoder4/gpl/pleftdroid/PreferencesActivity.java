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

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
 * This is the Activity that lets the user to set its Preferences.
 * It uses the Singleton Enum AppPreferences to store the preferences.
 * @see AppPreferences
 *
 */
public class PreferencesActivity extends Activity {
	protected static final String URL_PATTERN = "^http://[a-z0-9-]+(\\.[a-z0-9-]+)+$";
	protected static final String EMAIL_PATTERN = "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if(AppPreferences.INSTANCE.getUsePleftTheme()) { setTheme(R.style.Theme_Pleft); }
        super.onCreate(savedInstanceState);
        
        // Set the View
        setContentView(R.layout.pleft_preferences);
        
        // Default Preferences
        ((EditText) findViewById(R.id.uname)).setText(AppPreferences.INSTANCE.getName().trim());
        ((EditText) findViewById(R.id.uemail)).setText(AppPreferences.INSTANCE.getEmail().trim());
        ((EditText) findViewById(R.id.pserver)).setText(AppPreferences.INSTANCE.getPleftServer().trim());
        ((CheckBox) findViewById(R.id.usedname)).setChecked(AppPreferences.INSTANCE.getUseDisplayName());
        ((CheckBox) findViewById(R.id.showdetails)).setChecked(AppPreferences.INSTANCE.getShowDetails());
        ((CheckBox) findViewById(R.id.useplefttheme)).setChecked(AppPreferences.INSTANCE.getUsePleftTheme());
        
        // Save Preferences Button
        ((Button) findViewById(R.id.saveprefs)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String uname = ((EditText) findViewById(R.id.uname)).getText().toString().trim();
				String uemail = ((EditText) findViewById(R.id.uemail)).getText().toString().trim();
				String pserver = ((EditText) findViewById(R.id.pserver)).getText().toString().trim();
				boolean usedname= ((CheckBox) findViewById(R.id.usedname)).isChecked();
				boolean showdetails= ((CheckBox) findViewById(R.id.showdetails)).isChecked();
				boolean useplefttheme= ((CheckBox) findViewById(R.id.useplefttheme)).isChecked();
				if(pserver.endsWith("/")) { 
					pserver = pserver.substring(0, pserver.lastIndexOf("/"));
				}
				if(uname==null || uname.length()==0 ||
				   uemail==null || uemail.length()==0 ||
				   pserver==null || pserver.length()==0) {
				   Toast.makeText(PreferencesActivity.this, R.string.dialog_msg_settingsreq, Toast.LENGTH_LONG).show();
				} else if (!isValidServerAddress(pserver)){
					Toast.makeText(PreferencesActivity.this, R.string.toast_invalidservaddr, Toast.LENGTH_LONG).show();
				} else if (!isValidEmailAddress(uemail)){
					Toast.makeText(PreferencesActivity.this, R.string.toast_invalidemailaddr, Toast.LENGTH_LONG).show();
				} else {
				// Save prefs
				
				AppPreferences.INSTANCE.setName(uname);
				AppPreferences.INSTANCE.setEmail(uemail);
				AppPreferences.INSTANCE.setPleftServer(pserver);
				AppPreferences.INSTANCE.setUseDisplayName(usedname);
				AppPreferences.INSTANCE.setShowDetails(showdetails);
				AppPreferences.INSTANCE.setUsePleftTheme(useplefttheme);
				AppPreferences.INSTANCE.setEulaAccepted();
				AppPreferences.INSTANCE.save();
				Intent ip = new Intent(PreferencesActivity.this, PleftDroidActivity.class);
				startActivity(ip);
				// Close activity
				finish();
				}
			}
        });
    }
    
    public static boolean isValidServerAddress(String servAddress){
    	if (servAddress == null) return false;
    	boolean result = true;
    	try {
        	final Pattern servp = Pattern.compile(URL_PATTERN);
        	if (!servp.matcher(servAddress).matches()) {
        	    return false;
        	}
          
        }
        catch (Exception ex){
          result = false;
        }
    	
    	return result;
    }
    public static boolean isValidEmailAddress(String emailAddress){
        if (emailAddress == null) return false;
        boolean result = true;
        try {
        	final Pattern rfc2822 = Pattern.compile(EMAIL_PATTERN);
        	if (!rfc2822.matcher(emailAddress).matches()) {
        	    return false;
        	}
          
        }
        catch (Exception ex){
          result = false;
        }
        return result;
      }


}
