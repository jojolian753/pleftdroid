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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author r.massera
 * This Enum is a Singleton that manages the Preferences across the whole App
 * You MUST call save() to save preferences.
 */
public enum AppPreferences {
	INSTANCE;
	
	static Context appctx;
	static SharedPreferences prefs=null;
	static SharedPreferences.Editor editor=null;
	private static final String NAME_SUFFIX="name";
	private static final String EMAIL_SUFFIX="email";
	protected static final String PSERVER_SUFFIX="pleftserver";
	protected static final String USEDN_SUFFIX="usedname";
	protected static final String SHOWDETAILS_SUFFIX="showdetails";
	protected static final String USEPLEFTTHEME_SUFFIX="useplefttheme";
	protected static final String PSERVER_DEFAULT="http://your.pleft.server";
	private static final String EULA_PREFIX = "EULA_";
	protected static String PREFS_PREFIX="APP_";
	protected static String NAME;
	protected static String EMAIL;
	protected static String PSERVER;
	protected static String USEDN;
	protected static String SHOWDETAILS;
	protected static String USEPLEFTTHEME;
	// The EULA Key changes every time you increment the version number in the AndroidManifest.xml
	protected static String EULA;
	
	protected static final void init(Context c){
		appctx=c;

		PREFS_PREFIX = appctx.getString(R.string.app_name)+"_";
		NAME=PREFS_PREFIX+NAME_SUFFIX;
		EMAIL=PREFS_PREFIX+EMAIL_SUFFIX;
		PSERVER=PREFS_PREFIX+PSERVER_SUFFIX;
		USEDN=PREFS_PREFIX+USEDN_SUFFIX;
		SHOWDETAILS=PREFS_PREFIX+SHOWDETAILS_SUFFIX;
		USEPLEFTTHEME=PREFS_PREFIX+USEPLEFTTHEME_SUFFIX;
		// The EULA Key changes every time you increment the version number in the AndroidManifest.xml
		EULA=EULA_PREFIX + PleftDroidActivity.APPVERSIONCODE;
	}
	//Getters
	protected String getName() {
		getPrefs(); return prefs.getString(NAME, "");
	}
	protected String getEmail() {
		getPrefs(); return prefs.getString(EMAIL, "");
	}
	protected String getPleftServer() {
		getPrefs(); return prefs.getString(PSERVER, PSERVER_DEFAULT);
	}
	protected boolean getUseDisplayName() {
		getPrefs(); return prefs.getBoolean(USEDN, false);
	}
	protected boolean getShowDetails() {
		getPrefs(); return prefs.getBoolean(SHOWDETAILS, false);
	}
	protected boolean getUsePleftTheme() {
		getPrefs(); return prefs.getBoolean(USEPLEFTTHEME, false);
	}
	protected boolean getEulaAccepted() {
		getPrefs(); return prefs.getBoolean(EULA, false);
	}
	//Setters
	protected void setName(String s) {
		getEditor(); editor.putString(NAME, s);
	}
	protected void setEmail(String s) {
		getEditor(); editor.putString(EMAIL, s);
	}
	protected void setPleftServer(String s) {
		getEditor(); editor.putString(PSERVER, s);
	}
	protected void setUseDisplayName(boolean b) {
		getEditor(); editor.putBoolean(USEDN, b);
	}
	protected void setShowDetails(boolean b) {
		getEditor(); editor.putBoolean(SHOWDETAILS, b);
	}
	protected void setUsePleftTheme(boolean b) {
		getEditor(); editor.putBoolean(USEPLEFTTHEME, b);
	}
	protected void setEulaAccepted() {
		getEditor(); editor.putBoolean(EULA, true);
	}
	
	private void getPrefs() {
		if(prefs==null) prefs = appctx.getSharedPreferences(appctx.getClass().getName(), 0); //PleftDroidActivity.class.getName(), 0);
	}
	private void getEditor() {
		getPrefs();
		if(editor==null) editor=prefs.edit();
	}
	protected void save() {
		editor.commit();
		editor=null; prefs=null;
	}

}
