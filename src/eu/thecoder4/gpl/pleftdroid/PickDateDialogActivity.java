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
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class PickDateDialogActivity extends Activity {
	
	protected static final String DTLIST = "DTLIST";
	
    protected TimePicker mTP;
	protected DatePicker mDtP;
	private EditText mDTet;
	protected ArrayList<PDate> mDTlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.event_selectdates);
        mDtP = (DatePicker) findViewById(R.id.datePicker1);
		mTP = (TimePicker) findViewById(R.id.timePicker1);
		mTP.setIs24HourView(true);
		mTP.setCurrentHour(12); mTP.setCurrentMinute(0);
		mDTet = ((EditText) findViewById(R.id.datetimeslist));
		
		Bundle extras = getIntent().getExtras();
    	
		mDTlist = extras.getParcelableArrayList(DTLIST);//new ArrayList<PDate>();
		mDTet.setText(getDates());
        
		// Add Button
		((Button) findViewById(R.id.addtodates)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int[] seldate = new int[]{mDtP.getYear(),mDtP.getMonth(),mDtP.getDayOfMonth()};
				int[] seltime = new int[]{mTP.getCurrentHour(),mTP.getCurrentMinute()};
				PDate td = new PDate(seldate, seltime);
				if(PickDateDialogActivity.this.isDateYetPresent(td)) {
					Toast.makeText(PickDateDialogActivity.this, R.string.toast_dtyetpresent, Toast.LENGTH_SHORT).show();
				} else {
					Calendar today=Calendar.getInstance();
					today.add(Calendar.DATE, -1);
					if(td.dateIsAfter(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))) {
					mDTlist.add(td);
					((EditText) findViewById(R.id.datetimeslist)).setText(getDates());
					} else {
						Toast.makeText(PickDateDialogActivity.this, R.string.toast_dtnotbefore, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		// Remove last Button
		((Button) findViewById(R.id.remlastdate)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(!(mDTlist.isEmpty()) ) mDTlist.remove(mDTlist.size()-1);
				((EditText) findViewById(R.id.datetimeslist)).setText(getDates());
			}
		});
        // Done Button
        ((Button) findViewById(R.id.seldatedone)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent();

	            i.putParcelableArrayListExtra(DTLIST, mDTlist);
	            setResult(RESULT_OK, i);
	            // Close activity
	            finish();

			}
        });
    }
	private String getDates() {
		String s="";
		if(mDTlist.isEmpty()) return s;
		for(PDate p : mDTlist) {
			s+=p.getDateString()+",\n";
			
		}
		s=s.substring(0, (s.length()-2));
		return s;
	}
	
	private boolean isDateYetPresent(PDate p) {
		if(mDTlist.isEmpty()) return false;
		for(PDate pd : mDTlist) {
			if(pd.equals(p)) { return true; }			
		}
		return false;
		
	}
	

}
