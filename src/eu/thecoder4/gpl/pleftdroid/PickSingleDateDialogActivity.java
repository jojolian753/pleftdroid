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

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

public class PickSingleDateDialogActivity extends Activity {


	protected TimePicker mTP;
	protected DatePicker mDtP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.event_datedialog);
		mDtP = (DatePicker) findViewById(R.id.datePicker1);
		mTP = (TimePicker) findViewById(R.id.timePicker1);
		mTP.setIs24HourView(true);
		mTP.setCurrentHour(12); mTP.setCurrentMinute(0);

		// Done Button
		((Button) findViewById(R.id.datedone)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Bundle bundle = new Bundle();
				int[] seldate = new int[]{mDtP.getYear(),mDtP.getMonth(),mDtP.getDayOfMonth()};
				int[] seltime = new int[]{mTP.getCurrentHour(),mTP.getCurrentMinute()};
				PDate td = new PDate(seldate, seltime);

				Calendar today=Calendar.getInstance();
				today.add(Calendar.DATE, -1);
				if(td.dateIsAfter(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))) {


					bundle.putIntArray(EditEventActivity.DT_DATE, seldate);
					bundle.putIntArray(EditEventActivity.DT_TIME, seltime);

					Intent i = new Intent();
					i.putExtras(bundle);
					setResult(RESULT_OK, i);
					// Close activity
					finish();
				} else {
					Toast.makeText(PickSingleDateDialogActivity.this, "The Date can't be\nbefore today!", Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

}
