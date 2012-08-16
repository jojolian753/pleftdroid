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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * 
 * @author r.massera
 * 
 * Helper Class to show the EULA
 *
 */
public class EulaDialog extends Observable {

	private Activity mActivity;

	public EulaDialog(Activity context) {
		mActivity = context;
	}

	public void show() {
		
		// the eulaKey changes every time you increment the version number in the AndroidManifest.xml
		
		boolean hasBeenShownAndAccepted = AppPreferences.INSTANCE.getEulaAccepted();
		if(hasBeenShownAndAccepted == false) {

			// Show the Eula
			final String title = mActivity.getString(R.string.app_name) + " v" + PleftDroidActivity.APPVERSION;

			//Includes the updates as well so users know what changed ?
			final String eulastr = mActivity.getString(R.string.eula_title) + "\n\n" +
			                       mActivity.getString(R.string.eula);
			final SpannableString eula=new SpannableString(eulastr);
			// Set up the TextView
			final TextView eulatv = new TextView(mActivity);
			eulatv.setText(eula);
			eulatv.setPadding(10, 5, 10, 5);
			eulatv.setTextColor(eulatv.getTextColors().getDefaultColor());
			Linkify.addLinks(eulatv, Linkify.WEB_URLS);//Linkify.ALL);

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
			.setTitle(title)
			.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// Mark this version as read and accepted.
					AppPreferences.INSTANCE.setEulaAccepted();
					AppPreferences.INSTANCE.save();
					EulaDialog.this.setChanged();  
					EulaDialog.this.notifyObservers();
					dialogInterface.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Close the activity as they have declined the EULA
					mActivity.finish();
				}

			}).setCancelable(false).setView(eulatv);
			builder.create().show();
		} else {
			setChanged();  
			notifyObservers();
		}
	}

}

