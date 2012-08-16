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


//import eu.thecoder4.gpl.pleftdroid.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog {

	private Activity mActivity;

	public AboutDialog(Activity context) {
		mActivity = context;
	}

	public void show() {

		// Show the About text
		final String title = mActivity.getString(R.string.app_name); // + " v" + PleftDroidActivity.APPVERSION;

		final String aboutstr = mActivity.getString(R.string.about);
		final SpannableString about=new SpannableString(aboutstr);
		// Set up the TextView
		final TextView abouttv = new TextView(mActivity);
		abouttv.setText(about);
		abouttv.setPadding(5, 5, 5, 5);
		abouttv.setTextColor(abouttv.getTextColors().getDefaultColor());

		Linkify.addLinks(abouttv, Linkify.ALL);

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
		.setTitle(title)
		.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// Do nothing.
				dialogInterface.dismiss();
			}
		}).setView(abouttv);
		builder.create().show();

	}

}

