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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author r.massera
 * 
 * Class to manage the Editing of comments
 *
 */
public class EditCommentActivity extends Activity {
	
	private int dtid;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        dtid=extras.getInt(EventDetailActivity.AR_DTID);
        
        // The View
        setContentView(R.layout.add_comment);
        
        // Save Preferences Button
        ((Button) findViewById(R.id.addc_butok)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText comment = (EditText) findViewById(R.id.addc_comment);
				Intent i = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt(EventDetailActivity.AR_DTID, dtid);
	            bundle.putString(EventDetailActivity.AR_COMMENT, comment.getText().toString().trim());
	            i.putExtras(bundle);
	            setResult(RESULT_OK, i);
	            // Close activity
	            finish();
			}
        });
        ((Button) findViewById(R.id.addc_butcanc)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Close activity
				finish();
			}
        });
        this.setTitle("Add comment");
        
    }

}
